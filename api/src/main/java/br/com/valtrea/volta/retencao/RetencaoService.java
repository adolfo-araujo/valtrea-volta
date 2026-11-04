package br.com.valtrea.volta.retencao;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Rotina de retenção (LGPD, princípio da necessidade e arts. 15 e 16): apaga o que não precisa mais ser guardado.
 * <ul>
 * <li>Cadastros que nunca confirmaram o e-mail e não têm objetos, 30 dias depois de criados.</li>
 * <li>Avisos de quem encontrou um objeto (mensagem e contato), 90 dias depois de recebidos.</li>
 * <li>Etiquetas desativadas há mais de 30 dias.</li>
 * <li>Códigos de e-mail e sessões vencidos há mais de um dia.</li>
 * </ul>
 */
@Service
public class RetencaoService {

	public static final Duration CADASTRO_SEM_CONFIRMACAO = Duration.ofDays(30);
	public static final Duration AVISOS = Duration.ofDays(90);
	public static final Duration ETIQUETAS_DESATIVADAS = Duration.ofDays(30);
	private static final Duration FOLGA = Duration.ofDays(1);

	private static final Logger log = LoggerFactory.getLogger(RetencaoService.class);

	public record Resultado(int cadastrosNaoConfirmados, int avisos, int etiquetas, int codigos, int sessoes) {

		int total() {
			return cadastrosNaoConfirmados + avisos + etiquetas + codigos + sessoes;
		}

	}

	private final JdbcTemplate jdbc;

	public RetencaoService(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	@Transactional
	public Resultado executar(Instant agora) {
		int cadastros = jdbc.update("""
				DELETE FROM usuarios u
				 WHERE u.email_confirmado_em IS NULL
				   AND u.criado_em < ?
				   AND NOT EXISTS (SELECT 1 FROM objetos o WHERE o.dono_id = u.id)
				""", Timestamp.from(agora.minus(CADASTRO_SEM_CONFIRMACAO)));
		int avisos = jdbc.update("DELETE FROM avisos WHERE criado_em < ?", Timestamp.from(agora.minus(AVISOS)));
		int etiquetas = jdbc.update("DELETE FROM etiquetas WHERE desativada_em IS NOT NULL AND desativada_em < ?",
				Timestamp.from(agora.minus(ETIQUETAS_DESATIVADAS)));
		Timestamp vencidos = Timestamp.from(agora.minus(FOLGA));
		int codigos = jdbc.update("DELETE FROM codigos_email WHERE expira_em < ?", vencidos);
		int sessoes = jdbc.update("DELETE FROM sessoes WHERE expira_em < ?", vencidos);
		return new Resultado(cadastros, avisos, etiquetas, codigos, sessoes);
	}

	/** Roda um minuto depois de a API subir e depois a cada 12 horas (os testes desligam o início automático). */
	@Scheduled(initialDelayString = "${valtrea.retencao.atraso-inicial-ms:60000}",
			fixedDelayString = "${valtrea.retencao.intervalo-ms:43200000}")
	public void agendada() {
		try {
			Resultado r = executar(Instant.now());
			if (r.total() > 0) {
				log.info("[retenção] cadastros não confirmados: {}; avisos antigos: {}; etiquetas desativadas: {}; "
						+ "códigos e sessões vencidos: {}", r.cadastrosNaoConfirmados(), r.avisos(), r.etiquetas(),
						r.codigos() + r.sessoes());
			}
		} catch (RuntimeException e) {
			log.error("[retenção] falhou", e);
		}
	}

}
