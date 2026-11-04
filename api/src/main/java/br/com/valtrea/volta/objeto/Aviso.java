package br.com.valtrea.volta.objeto;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * "Encontrei seu objeto": mensagem de quem leu o QR Code, sem precisar de conta. O contato é opcional e só aparece para
 * o dono, se a pessoa quiser deixar. Avisos são apagados 90 dias depois de criados (RetencaoService).
 */
@Entity
@Table(name = "avisos")
public class Aviso {

	public enum Status {
		NOVO, LIDO, RESOLVIDO
	}

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "objeto_id")
	private Objeto objeto;

	@Column(nullable = false, length = 1000)
	private String mensagem;

	@Column(length = 120)
	private String contato;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Status status;

	@Column(name = "criado_em", nullable = false)
	private Instant criadoEm;

	@Column(name = "atualizado_em", nullable = false)
	private Instant atualizadoEm;

	protected Aviso() {
	}

	public Aviso(Objeto objeto, String mensagem, String contato) {
		this.objeto = objeto;
		this.mensagem = mensagem;
		this.contato = contato;
		this.status = Status.NOVO;
		this.criadoEm = Instant.now();
		this.atualizadoEm = criadoEm;
	}

	public void marcar(Status novo) {
		this.status = novo;
		this.atualizadoEm = Instant.now();
	}

	public UUID getId() {
		return id;
	}

	public Objeto getObjeto() {
		return objeto;
	}

	public String getMensagem() {
		return mensagem;
	}

	public String getContato() {
		return contato;
	}

	public Status getStatus() {
		return status;
	}

	public Instant getCriadoEm() {
		return criadoEm;
	}

}
