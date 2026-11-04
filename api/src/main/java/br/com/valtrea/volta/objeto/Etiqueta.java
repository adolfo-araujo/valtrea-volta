package br.com.valtrea.volta.objeto;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * O QR Code colado no objeto. O QR Code leva só um código aleatório de 256 bits (nada sobre o dono). O banco guarda o
 * hash desse código, para achar a etiqueta, e uma cópia cifrada, para o dono baixar o mesmo QR Code de novo. Uma
 * etiqueta desativada para de funcionar na hora.
 */
@Entity
@Table(name = "etiquetas")
public class Etiqueta {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "objeto_id")
	private Objeto objeto;

	@Column(name = "token_hash", nullable = false, length = 64)
	private String tokenHash;

	@Column(name = "token_cifrado", nullable = false)
	private String tokenCifrado;

	@Column(name = "criada_em", nullable = false)
	private Instant criadaEm;

	@Column(name = "desativada_em")
	private Instant desativadaEm;

	protected Etiqueta() {
	}

	public Etiqueta(Objeto objeto, String tokenHash, String tokenCifrado) {
		this.objeto = objeto;
		this.tokenHash = tokenHash;
		this.tokenCifrado = tokenCifrado;
		this.criadaEm = Instant.now();
	}

	public void desativar() {
		if (desativadaEm == null) {
			desativadaEm = Instant.now();
		}
	}

	public boolean isAtiva() {
		return desativadaEm == null;
	}

	public UUID getId() {
		return id;
	}

	public Objeto getObjeto() {
		return objeto;
	}

	public String getTokenCifrado() {
		return tokenCifrado;
	}

	public Instant getCriadaEm() {
		return criadaEm;
	}

	public Instant getDesativadaEm() {
		return desativadaEm;
	}

}
