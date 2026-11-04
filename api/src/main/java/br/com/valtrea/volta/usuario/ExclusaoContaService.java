package br.com.valtrea.volta.usuario;

import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.valtrea.volta.email.Email;
import br.com.valtrea.volta.erro.NegocioException;

/**
 * Exclusão de conta pedida pela própria pessoa (LGPD e Google Play). Apaga tudo na hora: o banco remove em cascata os
 * objetos, as etiquetas (os QR Codes impressos param de funcionar), os avisos recebidos, as sessões e os códigos.
 */
@Service
public class ExclusaoContaService {

	private final UsuarioRepository usuarios;
	private final PasswordEncoder passwordEncoder;
	private final ApplicationEventPublisher eventos;

	public ExclusaoContaService(UsuarioRepository usuarios, PasswordEncoder passwordEncoder,
			ApplicationEventPublisher eventos) {
		this.usuarios = usuarios;
		this.passwordEncoder = passwordEncoder;
		this.eventos = eventos;
	}

	@Transactional
	public void excluir(UUID usuarioId, String senha) {
		Usuario usuario = usuarios.findById(usuarioId)
				.orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, "Conta não encontrada."));
		if (!passwordEncoder.matches(senha, usuario.getSenhaHash())) {
			throw new NegocioException(HttpStatus.FORBIDDEN, "Senha incorreta.");
		}
		Email aviso = Email.contaExcluida(usuario.getEmail(), usuario.getNome());
		usuarios.delete(usuario);
		eventos.publishEvent(aviso);
	}

}
