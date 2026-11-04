package br.com.valtrea.volta.admin;

import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.valtrea.volta.erro.NegocioException;
import br.com.valtrea.volta.usuario.UsuarioRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Painel de administração: só números agregados. Nenhum nome, e-mail, objeto ou aviso individual sai daqui (LGPD,
 * princípio da necessidade).
 */
@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Administração")
public class AdminController {

	static final int SEMANAS = 8;

	public record Metricas(Instant geradoEm, Contas contas, Uso uso, List<Semana> cadastrosPorSemana) {
	}

	/** @param confirmadas com e-mail confirmado */
	public record Contas(long total, long confirmadas, long novas7Dias, long novas30Dias, long comSessaoAtiva) {
	}

	/**
	 * @param contasComObjetos  contas com pelo menos um objeto cadastrado
	 * @param etiquetasAtivas   QR Codes funcionando
	 * @param objetosPerdidos   marcados como perdidos agora
	 * @param avisos30Dias      avisos de "encontrei" nos últimos 30 dias (o número que mostra se o Volta funciona)
	 * @param avisosResolvidos  dos avisos guardados, quantos o dono marcou como resolvidos
	 */
	public record Uso(long objetos, long contasComObjetos, long etiquetasAtivas, long objetosPerdidos, long avisos7Dias,
			long avisos30Dias, long avisosResolvidos) {
	}

	/** @param semana segunda-feira (UTC) da semana */
	public record Semana(LocalDate semana, long cadastros) {
	}

	private final JdbcTemplate jdbc;
	private final UsuarioRepository usuarios;
	private final Administradores administradores;

	public AdminController(JdbcTemplate jdbc, UsuarioRepository usuarios, Administradores administradores) {
		this.jdbc = jdbc;
		this.usuarios = usuarios;
		this.administradores = administradores;
	}

	@GetMapping("/metricas")
	@Transactional(readOnly = true)
	@Operation(summary = "Números de uso agregados (só para administradores)")
	public Metricas metricas(@AuthenticationPrincipal Jwt jwt) {
		boolean admin = usuarios.findById(UUID.fromString(jwt.getSubject())).map(administradores::ehAdmin)
				.orElse(false);
		if (!admin) {
			throw new NegocioException(HttpStatus.FORBIDDEN, "Acesso restrito à administração.");
		}
		return calcular(Instant.now());
	}

	Metricas calcular(Instant agora) {
		Timestamp agoraTs = Timestamp.from(agora);
		Timestamp d7 = Timestamp.from(agora.minus(7, ChronoUnit.DAYS));
		Timestamp d30 = Timestamp.from(agora.minus(30, ChronoUnit.DAYS));

		Contas contas = jdbc.queryForObject("""
				SELECT count(*),
				       count(*) FILTER (WHERE email_confirmado_em IS NOT NULL),
				       count(*) FILTER (WHERE criado_em >= ?),
				       count(*) FILTER (WHERE criado_em >= ?),
				       (SELECT count(DISTINCT usuario_id) FROM sessoes WHERE revogado_em IS NULL AND expira_em > ?)
				  FROM usuarios""", (rs, i) -> new Contas(rs.getLong(1), rs.getLong(2), rs.getLong(3), rs.getLong(4),
				rs.getLong(5)), d7, d30, agoraTs);

		Uso uso = jdbc.queryForObject("""
				SELECT (SELECT count(*) FROM objetos),
				       (SELECT count(DISTINCT dono_id) FROM objetos),
				       (SELECT count(*) FROM etiquetas WHERE desativada_em IS NULL),
				       (SELECT count(*) FROM objetos WHERE perdido),
				       (SELECT count(*) FROM avisos WHERE criado_em >= ?),
				       (SELECT count(*) FROM avisos WHERE criado_em >= ?),
				       (SELECT count(*) FROM avisos WHERE status = 'RESOLVIDO')""",
				(rs, i) -> new Uso(rs.getLong(1), rs.getLong(2), rs.getLong(3), rs.getLong(4), rs.getLong(5),
						rs.getLong(6), rs.getLong(7)),
				d7, d30);

		return new Metricas(agora, contas, uso, cadastrosPorSemana(agora));
	}

	private List<Semana> cadastrosPorSemana(Instant agora) {
		LocalDate estaSemana = agora.atOffset(ZoneOffset.UTC).toLocalDate()
				.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
		LocalDate primeira = estaSemana.minusWeeks(SEMANAS - 1);
		Map<LocalDate, Long> porSemana = new HashMap<>();
		jdbc.query("""
				SELECT (date_trunc('week', criado_em AT TIME ZONE 'UTC'))::date AS semana, count(*)
				  FROM usuarios
				 WHERE criado_em >= ?
				 GROUP BY 1""", (RowCallbackHandler) rs -> {
			porSemana.put(rs.getObject(1, LocalDate.class), rs.getLong(2));
		}, Timestamp.from(primeira.atStartOfDay(ZoneOffset.UTC).toInstant()));
		List<Semana> semanas = new ArrayList<>(SEMANAS);
		for (int i = 0; i < SEMANAS; i++) {
			LocalDate semana = primeira.plusWeeks(i);
			semanas.add(new Semana(semana, porSemana.getOrDefault(semana, 0L)));
		}
		return semanas;
	}

}
