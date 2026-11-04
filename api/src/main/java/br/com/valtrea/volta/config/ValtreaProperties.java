package br.com.valtrea.volta.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Validated
@ConfigurationProperties(prefix = "valtrea")
public record ValtreaProperties(@Valid @NotNull Jwt jwt, @Valid @NotNull Login login, @Valid @NotNull Codigo codigo,
		@Valid @NotNull Email email, @Valid @NotNull Etiquetas etiquetas, @Valid @NotNull Limites limites) {

	/**
	 * @param validade        quanto dura o token de acesso (curto)
	 * @param validadeSessao  quanto dura a sessão do app, renovada sozinha enquanto a pessoa usa
	 */
	public record Jwt(@NotNull @Size(min = 32, message = "precisa ter pelo menos 32 caracteres") String segredo,
			@NotNull Duration validade, @NotNull Duration validadeSessao) {
	}

	public record Login(@Min(1) int tentativasMaximas, @NotNull Duration bloqueio) {
	}

	/** Códigos de 6 dígitos enviados por e-mail. */
	public record Codigo(@NotNull Duration validade, @Min(1) int tentativasMaximas, @NotNull Duration intervaloReenvio) {
	}

	/** @param resendApiKey preenchida, os e-mails saem pela API HTTP do Resend; vazia, por SMTP */
	public record Email(@NotBlank String remetente, String resendApiKey) {

		public boolean usaResend() {
			return resendApiKey != null && !resendApiKey.isBlank();
		}

	}

	/**
	 * Etiquetas com QR Code.
	 *
	 * @param urlSite     endereço do site (ex.: https://volta.valtrea.com.br); o QR Code leva {urlSite}/v/{código}
	 * @param chaveCifra  32 bytes em Base64; cifra o código de cada etiqueta, para o dono baixar o QR Code de novo
	 */
	public record Etiquetas(@NotBlank String urlSite, @NotBlank String chaveCifra) {

		public String urlDoCodigo(String codigo) {
			return urlSite.replaceAll("/+$", "") + "/v/" + codigo;
		}

		public String urlDosAvisos() {
			return urlSite.replaceAll("/+$", "") + "/avisos";
		}

	}

	/**
	 * Proteção contra abuso, por endereço IP.
	 *
	 * @param geral                quantos pedidos cada IP pode fazer na janela geral
	 * @param autenticacao         tentativas de login e renovação de sessão
	 * @param cadastroEEmails      cadastros e pedidos que mandam e-mail (esqueci a senha, reenviar código)
	 * @param avisos               avisos de "encontrei seu objeto" (rota aberta, sem conta)
	 * @param tamanhoMaximoKb      tamanho máximo do corpo de um pedido
	 * @param respostaLenta        acima disso, a resposta vira um aviso "[lento]" no log
	 */
	public record Limites(@Min(1) int geral, @NotNull Duration janelaGeral, @Min(1) int autenticacao,
			@NotNull Duration janelaAutenticacao, @Min(1) int cadastroEEmails, @NotNull Duration janelaCadastro,
			@Min(1) int avisos, @NotNull Duration janelaAvisos, @Min(1) int tamanhoMaximoKb,
			@NotNull Duration respostaLenta) {
	}

}
