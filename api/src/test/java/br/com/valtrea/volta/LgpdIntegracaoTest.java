package br.com.valtrea.volta;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

import br.com.valtrea.volta.retencao.RetencaoService;
import br.com.valtrea.volta.usuario.Termos;

/** Senha forte, "Baixar meus dados", aceite dos Termos, exclusão de conta e rotina de retenção. */
class LgpdIntegracaoTest extends IntegracaoBase {

	@Autowired
	JdbcTemplate jdbc;

	@Autowired
	RetencaoService retencao;

	private ResponseEntity<Void> excluir(String token, String senha) {
		return http.method(HttpMethod.DELETE).uri("/api/v1/eu").header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON).body(Map.of("senha", senha)).retrieve().toBodilessEntity();
	}

	private boolean existe(String tabela, UUID id) {
		return jdbc.queryForObject("SELECT count(*) FROM " + tabela + " WHERE id = ?", Integer.class, id) == 1;
	}

	@Test
	void cadastroPedeSoNomeEmailESenhaComLetrasENumeros() {
		var semNumero = http.post().uri("/api/v1/auth/cadastro").contentType(MediaType.APPLICATION_JSON)
				.body(Map.of("nome", "Senha Fraca", "email", emailUnico("fraca"), "senha", "somenteletras")).retrieve()
				.toBodilessEntity();
		assertThat(semNumero.getStatusCode().value()).isEqualTo(400);

		String email = emailUnico("dupla");
		cadastrar("Primeira", email);
		var repetido = http.post().uri("/api/v1/auth/cadastro").contentType(MediaType.APPLICATION_JSON)
				.body(Map.of("nome", "Segunda", "email", email.toUpperCase(), "senha", "senha-forte-123")).retrieve()
				.toBodilessEntity();
		assertThat(repetido.getStatusCode().value()).isEqualTo(409);
	}

	@Test
	void baixarMeusDadosTrazTudoMenosSenhaECodigos() {
		String email = emailUnico("olga");
		String olga = cadastrarConfirmado("Olga Dados", email);
		Objeto objeto = criarObjeto(olga, "Carteira marrom");
		Etiqueta etiqueta = criarEtiqueta(olga, objeto.id());
		avisar(etiqueta.codigo(), "Achei na padaria", "99999-0000");

		ResponseEntity<String> resposta = http.get().uri("/api/v1/eu/dados").header("Authorization", "Bearer " + olga)
				.retrieve().toEntity(String.class);
		assertThat(resposta.getStatusCode().value()).isEqualTo(200);
		assertThat(resposta.getHeaders().getFirst("Content-Disposition")).contains("valtrea-volta-meus-dados-");
		assertThat(resposta.getBody()).contains(email, "Carteira marrom", "Achei na padaria", etiqueta.url(),
				"termosVersao");
		assertThat(resposta.getBody()).doesNotContain("senha", "$2a$", "codigo_hash", "token_cifrado");
	}

	@Test
	void contaNovaJaAceitaOsTermosEAntigaPrecisaAceitar() {
		String token = cadastrar("Quela Termos", emailUnico("quela")).accessToken();
		Perfil perfil = perfil(token);
		assertThat(perfil.termosAceitos()).isTrue();

		jdbc.update("UPDATE usuarios SET termos_versao = NULL, termos_aceitos_em = NULL WHERE id = ?", perfil.id());
		assertThat(perfil(token).termosAceitos()).isFalse();
		assertThat(post("/api/v1/eu/termos", token, Map.of("versao", "2000-01-01")).getStatusCode().value())
				.isEqualTo(400);
		assertThat(post("/api/v1/eu/termos", token, Map.of("versao", Termos.VERSAO)).getStatusCode().value())
				.isEqualTo(200);
		assertThat(perfil(token).termosAceitos()).isTrue();
	}

	@Test
	void excluirContaApagaTudoNaHoraEDesligaAsEtiquetas() {
		String email = emailUnico("rita");
		String rita = cadastrarConfirmado("Rita Sai", email);
		UUID idRita = perfil(rita).id();
		Objeto objeto = criarObjeto(rita, "Chaves de casa");
		Etiqueta etiqueta = criarEtiqueta(rita, objeto.id());
		avisar(etiqueta.codigo(), "Achei suas chaves", null);

		assertThat(excluir(rita, "senha-errada-1").getStatusCode().value()).isEqualTo(403);
		assertThat(excluir(rita, "senha-forte-123").getStatusCode().value()).isEqualTo(204);

		assertThat(existe("usuarios", idRita)).isFalse();
		assertThat(existe("objetos", objeto.id())).isFalse();
		assertThat(existe("etiquetas", etiqueta.id())).isFalse();
		assertThat(jdbc.queryForObject("SELECT count(*) FROM avisos WHERE objeto_id = ?", Integer.class, objeto.id()))
				.isZero();
		assertThat(publico(etiqueta.codigo()).getStatusCode().value()).isEqualTo(404);
		assertThat(emails.para(email).getLast().getSubject()).contains("excluída");
		assertThat(login(email, "senha-forte-123").getStatusCode().value()).isEqualTo(401);
	}

	@Test
	void retencaoApagaSoOQueNaoPrecisaMaisSerGuardado() {
		UUID parado = perfil(cadastrar("Rui Parado", emailUnico("rui")).accessToken()).id();
		String comObjetoToken = cadastrar("Sara Objeto", emailUnico("sara")).accessToken();
		UUID comObjeto = perfil(comObjetoToken).id();
		criarObjeto(comObjetoToken, "Óculos");
		UUID recente = perfil(cadastrar("Tais Recente", emailUnico("tais")).accessToken()).id();
		jdbc.update("UPDATE usuarios SET criado_em = now() - interval '31 days' WHERE id IN (?, ?)", parado, comObjeto);

		String vera = cadastrarConfirmado("Vera Avisos", emailUnico("vera"));
		Objeto objeto = criarObjeto(vera, "Bicicleta");
		Etiqueta velha = criarEtiqueta(vera, objeto.id());
		Etiqueta nova = criarEtiqueta(vera, objeto.id());
		avisar(nova.codigo(), "Aviso antigo", null);
		avisar(nova.codigo(), "Aviso recente", null);
		post("/api/v1/etiquetas/" + velha.id() + "/desativar", vera, Map.of());
		jdbc.update("UPDATE etiquetas SET desativada_em = now() - interval '31 days' WHERE id = ?", velha.id());
		jdbc.update("UPDATE avisos SET criado_em = now() - interval '91 days' WHERE mensagem = 'Aviso antigo'");

		retencao.executar(Instant.now());

		assertThat(existe("usuarios", parado)).isFalse();
		assertThat(existe("usuarios", comObjeto)).isTrue();
		assertThat(existe("usuarios", recente)).isTrue();
		assertThat(existe("etiquetas", velha.id())).isFalse();
		assertThat(existe("etiquetas", nova.id())).isTrue();
		assertThat(jdbc.queryForList("SELECT mensagem FROM avisos WHERE objeto_id = ?", String.class, objeto.id()))
				.containsExactly("Aviso recente");
	}

}
