package br.com.valtrea.volta.objeto;

import java.time.Instant;
import java.util.UUID;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class ObjetoDtos {

	private ObjetoDtos() {
	}

	public record ObjetoRequest(
			@NotBlank @Size(max = 80) String nome,
			@NotNull Categoria categoria,
			@Size(max = 500) String descricao,
			@Size(max = 200) String mensagemPublica) {
	}

	public record PerdidoRequest(boolean perdido) {
	}

	/**
	 * @param etiquetasAtivas quantas etiquetas desse objeto estão funcionando
	 * @param avisosNovos     avisos ainda não lidos
	 */
	public record ObjetoResponse(UUID id, String nome, Categoria categoria, String descricao, String mensagemPublica,
			boolean perdido, Instant criadoEm, long etiquetasAtivas, long avisosNovos) {
	}

	/** @param url o endereço que vai no QR Code (o app e o site desenham o QR Code a partir dele) */
	public record EtiquetaResponse(UUID id, String url, boolean ativa, Instant criadaEm, Instant desativadaEm) {
	}

	/** O que aparece para quem leu o QR Code. Nunca inclui nada sobre o dono. */
	public record ObjetoPublicoResponse(String nome, Categoria categoria, String mensagemPublica, boolean perdido) {
	}

	public record AvisoPublicoRequest(
			@NotBlank @Size(min = 5, max = 1000, message = "escreva entre 5 e 1000 caracteres") String mensagem,
			@Size(max = 120) String contato,
			boolean aceite,
			/** Campo invisível para pessoas; robôs que preenchem tudo caem aqui. */
			String website) {

		@AssertTrue(message = "marque que concorda em enviar a mensagem ao dono")
		public boolean isAceiteMarcado() {
			return aceite;
		}

	}

	public record AvisoResponse(UUID id, UUID objetoId, String objetoNome, String mensagem, String contato,
			Aviso.Status status, Instant criadoEm) {
	}

	public record StatusAvisoRequest(@NotNull Aviso.Status status) {
	}

}
