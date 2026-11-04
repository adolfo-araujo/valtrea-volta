package br.com.valtrea.volta;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/** O fluxo principal: cadastrar o objeto, gerar o QR Code, quem encontra avisa, o dono lê e resolve. */
class VoltaIntegracaoTest extends IntegracaoBase {

	private List<Aviso> avisos(String token) {
		return http.get().uri("/api/v1/avisos").header("Authorization", "Bearer " + token).retrieve()
				.body(new ParameterizedTypeReference<List<Aviso>>() {
				});
	}

	@Test
	void quemEncontraAvisaODonoSemVerNadaDele() {
		String email = emailUnico("ana");
		String ana = cadastrarConfirmado("Ana Souza", email);
		Objeto mochila = criarObjeto(ana, "Mochila preta");
		Etiqueta etiqueta = criarEtiqueta(ana, mochila.id());

		// O QR Code leva só o endereço do site e um código aleatório de 256 bits.
		assertThat(etiqueta.url()).startsWith("https://volta.exemplo.com/v/");
		assertThat(etiqueta.codigo()).matches("[A-Za-z0-9_-]{43}");

		// Quem lê o QR Code vê o nome do objeto e o recado, nada sobre a dona.
		ResponseEntity<String> pagina = publico(etiqueta.codigo());
		assertThat(pagina.getStatusCode().value()).isEqualTo(200);
		assertThat(pagina.getBody()).contains("Mochila preta", "Obrigado por avisar!")
				.doesNotContain("Ana", email, "chaveiro azul", mochila.id().toString());

		// Sem aceite, não envia.
		ResponseEntity<String> semAceite = http.post().uri("/api/v1/publico/{c}/avisos", etiqueta.codigo())
				.contentType(MediaType.APPLICATION_JSON).body(Map.of("mensagem", "Achei sua mochila")).retrieve()
				.toEntity(String.class);
		assertThat(semAceite.getStatusCode().value()).isEqualTo(400);

		assertThat(avisar(etiqueta.codigo(), "Achei sua mochila no ônibus 485", "bruno@exemplo.com").getStatusCode()
				.value()).isEqualTo(202);

		// A dona recebe um e-mail sem a mensagem nem o contato, e lê tudo no app.
		var aviso = emails.para(email).getLast();
		assertThat(aviso.getSubject()).isEqualTo("Alguém encontrou: Mochila preta");
		assertThat(aviso.getText()).contains("https://volta.exemplo.com/avisos").doesNotContain("bruno@exemplo.com");

		List<Aviso> lista = avisos(ana);
		assertThat(lista).hasSize(1);
		assertThat(lista.getFirst().mensagem()).isEqualTo("Achei sua mochila no ônibus 485");
		assertThat(lista.getFirst().contato()).isEqualTo("bruno@exemplo.com");
		assertThat(lista.getFirst().status()).isEqualTo("NOVO");
		assertThat(get("/api/v1/objetos/" + mochila.id(), ana, Objeto.class).avisosNovos()).isEqualTo(1);

		assertThat(post("/api/v1/avisos/" + lista.getFirst().id() + "/status", ana, Map.of("status", "RESOLVIDO"))
				.getStatusCode().value()).isEqualTo(200);
		assertThat(avisos(ana).getFirst().status()).isEqualTo("RESOLVIDO");
	}

	@Test
	void etiquetaDesativadaParaDeFuncionarNaHora() {
		String dono = cadastrarConfirmado("Caio Lima", emailUnico("caio"));
		Objeto chaves = criarObjeto(dono, "Chaves");
		Etiqueta etiqueta = criarEtiqueta(dono, chaves.id());
		assertThat(publico(etiqueta.codigo()).getStatusCode().value()).isEqualTo(200);

		assertThat(post("/api/v1/etiquetas/" + etiqueta.id() + "/desativar", dono, Map.of()).getStatusCode().value())
				.isEqualTo(200);
		assertThat(publico(etiqueta.codigo()).getStatusCode().value()).isEqualTo(404);
		assertThat(avisar(etiqueta.codigo(), "Achei suas chaves", null).getStatusCode().value()).isEqualTo(404);

		// O dono continua podendo baixar o QR Code das etiquetas ativas, nunca o das desativadas.
		Etiqueta outra = criarEtiqueta(dono, chaves.id());
		List<Etiqueta> etiquetas = http.get().uri("/api/v1/objetos/{id}/etiquetas", chaves.id())
				.header("Authorization", "Bearer " + dono).retrieve()
				.body(new ParameterizedTypeReference<List<Etiqueta>>() {
				});
		assertThat(etiquetas).extracting(Etiqueta::url).containsExactly(outra.url(), null);
	}

	@Test
	void codigosInventadosNaoRevelamNada() {
		for (String codigo : new String[] { "abc", "a".repeat(43), "../../etc/passwd", "a".repeat(200) }) {
			assertThat(publico(codigo).getStatusCode().value()).as(codigo).isIn(400, 404);
		}
	}

	@Test
	void ninguemMexeNoObjetoDeOutraPessoa() {
		String dona = cadastrarConfirmado("Dora Dona", emailUnico("dora"));
		String outro = cadastrarConfirmado("Edu Outro", emailUnico("edu"));
		Objeto bolsa = criarObjeto(dona, "Bolsa vermelha");
		Etiqueta etiqueta = criarEtiqueta(dona, bolsa.id());
		avisar(etiqueta.codigo(), "Achei sua bolsa", null);
		Aviso aviso = avisos(dona).getFirst();

		assertThat(http.get().uri("/api/v1/objetos/{id}", bolsa.id()).header("Authorization", "Bearer " + outro)
				.retrieve().toBodilessEntity().getStatusCode().value()).isEqualTo(404);
		assertThat(post("/api/v1/objetos/" + bolsa.id() + "/etiquetas", outro, Map.of()).getStatusCode().value())
				.isEqualTo(404);
		assertThat(post("/api/v1/etiquetas/" + etiqueta.id() + "/desativar", outro, Map.of()).getStatusCode().value())
				.isEqualTo(404);
		assertThat(post("/api/v1/avisos/" + aviso.id() + "/status", outro, Map.of("status", "LIDO")).getStatusCode()
				.value()).isEqualTo(404);
		assertThat(avisos(outro)).isEmpty();
		assertThat(http.get().uri("/api/v1/objetos").retrieve().toBodilessEntity().getStatusCode().value())
				.isEqualTo(401);
	}

	@Test
	void roboNoCampoInvisivelNaoGravaNada() {
		String dono = cadastrarConfirmado("Fabio Robo", emailUnico("fabio"));
		Etiqueta etiqueta = criarEtiqueta(dono, criarObjeto(dono, "Fone").id());
		ResponseEntity<String> robo = http.post().uri("/api/v1/publico/{c}/avisos", etiqueta.codigo())
				.contentType(MediaType.APPLICATION_JSON)
				.body(Map.of("mensagem", "Compre agora!", "aceite", true, "website", "http://spam.exemplo")).retrieve()
				.toEntity(String.class);
		assertThat(robo.getStatusCode().value()).isEqualTo(202);
		assertThat(avisos(dono)).isEmpty();
	}

	@Test
	void marcarPerdidoAparecePraQuemEncontra() {
		String dono = cadastrarConfirmado("Gabi Perde", emailUnico("gabi"));
		Objeto oculos = criarObjeto(dono, "Óculos de grau");
		Etiqueta etiqueta = criarEtiqueta(dono, oculos.id());
		assertThat(publico(etiqueta.codigo()).getBody()).contains("\"perdido\":false");
		post("/api/v1/objetos/" + oculos.id() + "/perdido", dono, Map.of("perdido", true));
		assertThat(publico(etiqueta.codigo()).getBody()).contains("\"perdido\":true");
	}

}
