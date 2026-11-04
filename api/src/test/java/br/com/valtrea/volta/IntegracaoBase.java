package br.com.valtrea.volta;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.client.RestClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Sobe a API com um PostgreSQL real e fala com ela por HTTP, como o app faria. O banco é um só para todas as classes
 * de teste (cada teste usa e-mails e CPFs próprios), e os e-mails ficam numa caixa falsa em vez de sair por SMTP.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
		"valtrea.jwt.segredo=segredo-de-teste-com-pelo-menos-32-caracteres",
		"valtrea.etiquetas.chave-cifra=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
		"valtrea.etiquetas.url-site=https://volta.exemplo.com", "valtrea.limites.geral=1000000",
		"valtrea.limites.autenticacao=1000000", "valtrea.limites.cadastro-e-emails=1000000",
		"valtrea.limites.avisos=1000000", "valtrea.retencao.atraso-inicial-ms=86400000",
		"valtrea.admin-emails=Chefe-Volta@Exemplo.com" })
@Import(IntegracaoBase.Emails.class)
abstract class IntegracaoBase {

	@ServiceConnection
	static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18-alpine");

	@TestConfiguration
	static class Emails {
		@Bean
		CaixaDeEmails caixaDeEmails() {
			return new CaixaDeEmails();
		}
	}

	/** Guarda os e-mails em memória. Substitui o JavaMailSender de verdade. */
	static class CaixaDeEmails extends JavaMailSenderImpl {
		final List<SimpleMailMessage> enviados = new CopyOnWriteArrayList<>();

		@Override
		public void send(SimpleMailMessage... mensagens) {
			enviados.addAll(List.of(mensagens));
		}

		List<SimpleMailMessage> para(String email) {
			return enviados.stream().filter(m -> List.of(m.getTo()).contains(email)).toList();
		}

		String ultimoCodigo(String email) {
			List<SimpleMailMessage> recebidos = para(email);
			assertThat(recebidos).as("e-mails para " + email).isNotEmpty();
			Matcher m = Pattern.compile("\\b(\\d{6})\\b").matcher(recebidos.getLast().getText());
			assertThat(m.find()).isTrue();
			return m.group(1);
		}
	}

	record Tokens(String accessToken, String refreshToken, Instant expiraEm) {
	}

	record Perfil(UUID id, String nome, String email, boolean emailConfirmado, boolean termosAceitos, boolean admin) {
	}

	record Objeto(UUID id, String nome, String categoria, String descricao, String mensagemPublica, boolean perdido,
			long etiquetasAtivas, long avisosNovos) {
	}

	record Etiqueta(UUID id, String url, boolean ativa) {

		String codigo() {
			return url.substring(url.lastIndexOf('/') + 1);
		}

	}

	record Aviso(UUID id, UUID objetoId, String objetoNome, String mensagem, String contato, String status) {
	}

	@Value("${local.server.port}")
	int porta;

	@Autowired
	CaixaDeEmails emails;

	RestClient http;

	@BeforeEach
	void configurarHttp() {
		http = RestClient.builder().baseUrl("http://localhost:" + porta)
				.defaultStatusHandler(HttpStatusCode::isError, (req, res) -> {
				}).build();
	}

	static String emailUnico(String nome) {
		return nome + "-" + UUID.randomUUID().toString().substring(0, 8) + "@exemplo.com";
	}

	Tokens cadastrar(String nome, String email) {
		return http.post().uri("/api/v1/auth/cadastro").contentType(MediaType.APPLICATION_JSON)
				.body(Map.of("nome", nome, "email", email, "senha", "senha-forte-123"))
				.retrieve().body(Tokens.class);
	}

	/** Cadastra e confirma o e-mail com o código que chegou na caixa. Devolve o token de acesso. */
	String cadastrarConfirmado(String nome, String email) {
		String token = cadastrar(nome, email).accessToken();
		assertThat(confirmarEmail(token, emails.ultimoCodigo(email)).getStatusCode().value()).isEqualTo(204);
		return token;
	}

	ResponseEntity<Void> confirmarEmail(String token, String codigo) {
		return post("/api/v1/auth/confirmar-email", token, Map.of("codigo", codigo));
	}

	ResponseEntity<Tokens> login(String email, String senha) {
		return http.post().uri("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
				.body(Map.of("email", email, "senha", senha)).retrieve().toEntity(Tokens.class);
	}

	Perfil perfil(String token) {
		return http.get().uri("/api/v1/eu").header("Authorization", "Bearer " + token).retrieve().body(Perfil.class);
	}

	Objeto criarObjeto(String token, String nome) {
		return http.post().uri("/api/v1/objetos").header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.body(Map.of("nome", nome, "categoria", "MOCHILA_BOLSA", "descricao", "Tem um chaveiro azul",
						"mensagemPublica", "Obrigado por avisar!"))
				.retrieve().body(Objeto.class);
	}

	Etiqueta criarEtiqueta(String token, UUID objetoId) {
		return http.post().uri("/api/v1/objetos/{id}/etiquetas", objetoId).header("Authorization", "Bearer " + token)
				.retrieve().body(Etiqueta.class);
	}

	ResponseEntity<String> publico(String codigo) {
		return http.get().uri("/api/v1/publico/{c}", codigo).retrieve().toEntity(String.class);
	}

	ResponseEntity<String> avisar(String codigo, String mensagem, String contato) {
		Map<String, Object> corpo = new java.util.HashMap<>(Map.of("mensagem", mensagem, "aceite", true));
		if (contato != null) {
			corpo.put("contato", contato);
		}
		return http.post().uri("/api/v1/publico/{c}/avisos", codigo).contentType(MediaType.APPLICATION_JSON)
				.body(corpo).retrieve().toEntity(String.class);
	}

	<T> T get(String rota, String token, Class<T> tipo) {
		return http.get().uri(rota).header("Authorization", "Bearer " + token).retrieve().body(tipo);
	}

	ResponseEntity<Void> post(String rota, String token, Object corpo) {
		var pedido = http.post().uri(rota).contentType(MediaType.APPLICATION_JSON).body(corpo);
		if (token != null) {
			pedido = pedido.header("Authorization", "Bearer " + token);
		}
		return pedido.retrieve().toBodilessEntity();
	}

}
