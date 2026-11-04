package br.com.valtrea.volta.objeto;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.valtrea.volta.auth.Segredos;
import br.com.valtrea.volta.config.ValtreaProperties;
import br.com.valtrea.volta.email.Email;
import br.com.valtrea.volta.erro.NegocioException;
import br.com.valtrea.volta.objeto.ObjetoDtos.AvisoPublicoRequest;
import br.com.valtrea.volta.objeto.ObjetoDtos.AvisoResponse;
import br.com.valtrea.volta.objeto.ObjetoDtos.EtiquetaResponse;
import br.com.valtrea.volta.objeto.ObjetoDtos.ObjetoPublicoResponse;
import br.com.valtrea.volta.objeto.ObjetoDtos.ObjetoRequest;
import br.com.valtrea.volta.objeto.ObjetoDtos.ObjetoResponse;
import br.com.valtrea.volta.seguranca.Cifra;
import br.com.valtrea.volta.usuario.Usuario;
import br.com.valtrea.volta.usuario.UsuarioRepository;

/**
 * Objetos, etiquetas e avisos. Toda operação do dono confere se o objeto é dele (o id do dono vem do token, nunca do
 * pedido). Objeto de outra pessoa responde 404, como se não existisse.
 */
@Service
public class ObjetoService {

	static final int MAX_OBJETOS = 100;
	static final int MAX_ETIQUETAS_ATIVAS = 5;
	static final int MAX_AVISOS_NAO_LIDOS = 20;

	/** 256 bits em Base64 URL sem "=": 43 caracteres. */
	private static final Pattern FORMATO_CODIGO = Pattern.compile("[A-Za-z0-9_-]{43}");
	private static final SecureRandom ALEATORIO = new SecureRandom();

	private final ObjetoRepository objetos;
	private final EtiquetaRepository etiquetas;
	private final AvisoRepository avisos;
	private final UsuarioRepository usuarios;
	private final Segredos segredos;
	private final Cifra cifra;
	private final ApplicationEventPublisher eventos;
	private final ValtreaProperties.Etiquetas config;

	public ObjetoService(ObjetoRepository objetos, EtiquetaRepository etiquetas, AvisoRepository avisos,
			UsuarioRepository usuarios, Segredos segredos, Cifra cifra, ApplicationEventPublisher eventos,
			ValtreaProperties propriedades) {
		this.objetos = objetos;
		this.etiquetas = etiquetas;
		this.avisos = avisos;
		this.usuarios = usuarios;
		this.segredos = segredos;
		this.cifra = cifra;
		this.eventos = eventos;
		this.config = propriedades.etiquetas();
	}

	// ---------- objetos do dono ----------

	@Transactional(readOnly = true)
	public List<ObjetoResponse> listar(UUID donoId) {
		return objetos.findByDonoIdOrderByCriadoEmDesc(donoId).stream().map(this::resposta).toList();
	}

	@Transactional(readOnly = true)
	public ObjetoResponse buscar(UUID donoId, UUID objetoId) {
		return resposta(doDono(donoId, objetoId));
	}

	@Transactional
	public ObjetoResponse criar(UUID donoId, ObjetoRequest pedido) {
		if (objetos.countByDonoId(donoId) >= MAX_OBJETOS) {
			throw new NegocioException(HttpStatus.CONFLICT, "Você chegou ao limite de " + MAX_OBJETOS + " objetos.");
		}
		Usuario dono = usuarios.findById(donoId)
				.orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, "Conta não encontrada."));
		Objeto objeto = objetos.save(new Objeto(dono, pedido.nome().strip(), pedido.categoria(),
				vazioParaNulo(pedido.descricao()), vazioParaNulo(pedido.mensagemPublica())));
		return resposta(objeto);
	}

	@Transactional
	public ObjetoResponse atualizar(UUID donoId, UUID objetoId, ObjetoRequest pedido) {
		Objeto objeto = doDono(donoId, objetoId);
		objeto.atualizar(pedido.nome().strip(), pedido.categoria(), vazioParaNulo(pedido.descricao()),
				vazioParaNulo(pedido.mensagemPublica()));
		return resposta(objeto);
	}

	@Transactional
	public ObjetoResponse marcarPerdido(UUID donoId, UUID objetoId, boolean perdido) {
		Objeto objeto = doDono(donoId, objetoId);
		objeto.marcarPerdido(perdido);
		return resposta(objeto);
	}

	/** Apaga o objeto, as etiquetas (os QR Codes param de funcionar) e os avisos dele. */
	@Transactional
	public void excluir(UUID donoId, UUID objetoId) {
		objetos.delete(doDono(donoId, objetoId));
	}

	// ---------- etiquetas ----------

	@Transactional(readOnly = true)
	public List<EtiquetaResponse> etiquetas(UUID donoId, UUID objetoId) {
		doDono(donoId, objetoId);
		return etiquetas.findByObjetoIdOrderByCriadaEmDesc(objetoId).stream().map(this::resposta).toList();
	}

	@Transactional
	public EtiquetaResponse criarEtiqueta(UUID donoId, UUID objetoId) {
		Objeto objeto = doDono(donoId, objetoId);
		if (!objeto.getDono().isEmailConfirmado()) {
			throw new NegocioException(HttpStatus.FORBIDDEN,
					"Confirme seu e-mail antes de gerar etiquetas: é por ele que avisamos quando alguém encontrar.");
		}
		if (etiquetas.countByObjetoIdAndDesativadaEmIsNull(objetoId) >= MAX_ETIQUETAS_ATIVAS) {
			throw new NegocioException(HttpStatus.CONFLICT,
					"Este objeto já tem " + MAX_ETIQUETAS_ATIVAS + " etiquetas ativas. Desative uma antes.");
		}
		byte[] bytes = new byte[32];
		ALEATORIO.nextBytes(bytes);
		String codigo = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
		Etiqueta etiqueta = etiquetas.save(new Etiqueta(objeto, segredos.hashToken(codigo), cifra.cifrar(codigo)));
		return resposta(etiqueta);
	}

	@Transactional
	public EtiquetaResponse desativarEtiqueta(UUID donoId, UUID etiquetaId) {
		Etiqueta etiqueta = etiquetas.findById(etiquetaId)
				.filter(e -> e.getObjeto().pertenceA(donoId))
				.orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, "Etiqueta não encontrada."));
		etiqueta.desativar();
		return resposta(etiqueta);
	}

	// ---------- quem encontrou (sem login) ----------

	@Transactional(readOnly = true)
	public ObjetoPublicoResponse publico(String codigo) {
		Objeto o = etiquetaAtiva(codigo).getObjeto();
		return new ObjetoPublicoResponse(o.getNome(), o.getCategoria(), o.getMensagemPublica(), o.isPerdido());
	}

	@Transactional
	public void avisar(String codigo, AvisoPublicoRequest pedido) {
		Objeto objeto = etiquetaAtiva(codigo).getObjeto();
		// Se alguém usar a etiqueta para mandar spam, a caixa do dono não enche: até 20 avisos sem ler por objeto.
		if (avisos.countByObjetoIdAndStatus(objeto.getId(), Aviso.Status.NOVO) >= MAX_AVISOS_NAO_LIDOS) {
			throw new NegocioException(HttpStatus.TOO_MANY_REQUESTS,
					"O dono já recebeu vários avisos e ainda não leu. Tente de novo mais tarde.");
		}
		avisos.save(new Aviso(objeto, pedido.mensagem().strip(), vazioParaNulo(pedido.contato())));
		Usuario dono = objeto.getDono();
		eventos.publishEvent(Email.novoAviso(dono.getEmail(), dono.getNome(), objeto.getNome(), config.urlDosAvisos()));
	}

	// ---------- avisos do dono ----------

	@Transactional(readOnly = true)
	public List<AvisoResponse> avisos(UUID donoId) {
		return avisos.findByObjetoDonoIdOrderByCriadoEmDesc(donoId).stream().map(ObjetoService::resposta).toList();
	}

	@Transactional
	public AvisoResponse marcarAviso(UUID donoId, UUID avisoId, Aviso.Status status) {
		Aviso aviso = avisos.findById(avisoId)
				.filter(a -> a.getObjeto().pertenceA(donoId))
				.orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, "Aviso não encontrado."));
		aviso.marcar(status);
		return resposta(aviso);
	}

	@Transactional
	public void excluirAviso(UUID donoId, UUID avisoId) {
		Aviso aviso = avisos.findById(avisoId)
				.filter(a -> a.getObjeto().pertenceA(donoId))
				.orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, "Aviso não encontrado."));
		avisos.delete(aviso);
	}

	// ---------- auxiliares ----------

	/** Código com formato errado, inexistente ou de etiqueta desativada: a mesma resposta, sem dar pistas. */
	private Etiqueta etiquetaAtiva(String codigo) {
		Optional<Etiqueta> etiqueta = codigo != null && FORMATO_CODIGO.matcher(codigo).matches()
				? etiquetas.findByTokenHash(segredos.hashToken(codigo))
				: Optional.empty();
		return etiqueta.filter(Etiqueta::isAtiva)
				.orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND,
						"Esta etiqueta não está ativa. Se você encontrou um objeto, entregue em um local de achados e perdidos."));
	}

	private Objeto doDono(UUID donoId, UUID objetoId) {
		return objetos.findById(objetoId)
				.filter(o -> o.pertenceA(donoId))
				.orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, "Objeto não encontrado."));
	}

	private ObjetoResponse resposta(Objeto o) {
		long ativas = etiquetas.countByObjetoIdAndDesativadaEmIsNull(o.getId());
		long novos = avisos.countByObjetoIdAndStatus(o.getId(), Aviso.Status.NOVO);
		return new ObjetoResponse(o.getId(), o.getNome(), o.getCategoria(), o.getDescricao(), o.getMensagemPublica(),
				o.isPerdido(), o.getCriadoEm(), ativas, novos);
	}

	private EtiquetaResponse resposta(Etiqueta e) {
		String url = e.isAtiva() ? config.urlDoCodigo(cifra.decifrar(e.getTokenCifrado())) : null;
		return new EtiquetaResponse(e.getId(), url, e.isAtiva(), e.getCriadaEm(), e.getDesativadaEm());
	}

	private static AvisoResponse resposta(Aviso a) {
		return new AvisoResponse(a.getId(), a.getObjeto().getId(), a.getObjeto().getNome(), a.getMensagem(),
				a.getContato(), a.getStatus(), a.getCriadoEm());
	}

	private static String vazioParaNulo(String texto) {
		return texto == null || texto.isBlank() ? null : texto.strip();
	}

}
