package br.com.valtrea.volta.objeto;

import java.time.Instant;
import java.util.UUID;

import br.com.valtrea.volta.usuario.Usuario;
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
 * Um pertence cadastrado pelo dono. {@code descricao} é privada (só o dono vê); quem encontra o objeto vê apenas o
 * nome, a categoria e a {@code mensagemPublica}, se o dono escrever uma.
 */
@Entity
@Table(name = "objetos")
public class Objeto {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "dono_id")
	private Usuario dono;

	@Column(nullable = false, length = 80)
	private String nome;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private Categoria categoria;

	@Column(length = 500)
	private String descricao;

	@Column(name = "mensagem_publica", length = 200)
	private String mensagemPublica;

	/** Marcado pelo dono quando perde o objeto: a página pública mostra um destaque. */
	@Column(nullable = false)
	private boolean perdido;

	@Column(name = "criado_em", nullable = false)
	private Instant criadoEm;

	@Column(name = "atualizado_em", nullable = false)
	private Instant atualizadoEm;

	protected Objeto() {
	}

	public Objeto(Usuario dono, String nome, Categoria categoria, String descricao, String mensagemPublica) {
		this.dono = dono;
		this.criadoEm = Instant.now();
		atualizar(nome, categoria, descricao, mensagemPublica);
	}

	public void atualizar(String nome, Categoria categoria, String descricao, String mensagemPublica) {
		this.nome = nome;
		this.categoria = categoria;
		this.descricao = descricao;
		this.mensagemPublica = mensagemPublica;
		this.atualizadoEm = Instant.now();
	}

	public void marcarPerdido(boolean perdido) {
		this.perdido = perdido;
		this.atualizadoEm = Instant.now();
	}

	public boolean pertenceA(UUID usuarioId) {
		return dono.getId().equals(usuarioId);
	}

	public UUID getId() {
		return id;
	}

	public Usuario getDono() {
		return dono;
	}

	public String getNome() {
		return nome;
	}

	public Categoria getCategoria() {
		return categoria;
	}

	public String getDescricao() {
		return descricao;
	}

	public String getMensagemPublica() {
		return mensagemPublica;
	}

	public boolean isPerdido() {
		return perdido;
	}

	public Instant getCriadoEm() {
		return criadoEm;
	}

}
