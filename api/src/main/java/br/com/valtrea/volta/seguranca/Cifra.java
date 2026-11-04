package br.com.valtrea.volta.seguranca;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Component;

import br.com.valtrea.volta.config.ValtreaProperties;

/**
 * Cifra o código de cada etiqueta antes de ir para o banco (AES-256-GCM). A busca pelo QR Code usa só o hash; a cópia
 * cifrada existe para o dono conseguir baixar o mesmo QR Code de novo, sem precisar trocar a etiqueta.
 */
@Component
public class Cifra {

	private static final int TAMANHO_IV = 12;
	private static final int TAMANHO_TAG_BITS = 128;
	private static final SecureRandom ALEATORIO = new SecureRandom();

	private final SecretKey chave;

	public Cifra(ValtreaProperties propriedades) {
		byte[] bytes = Base64.getDecoder().decode(propriedades.etiquetas().chaveCifra());
		if (bytes.length != 32) {
			throw new IllegalStateException("valtrea.etiquetas.chave-cifra precisa ter 32 bytes em Base64.");
		}
		this.chave = new SecretKeySpec(bytes, "AES");
	}

	public String cifrar(String texto) {
		try {
			byte[] iv = new byte[TAMANHO_IV];
			ALEATORIO.nextBytes(iv);
			Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
			cipher.init(Cipher.ENCRYPT_MODE, chave, new GCMParameterSpec(TAMANHO_TAG_BITS, iv));
			byte[] cifrado = cipher.doFinal(texto.getBytes(StandardCharsets.UTF_8));
			return Base64.getEncoder()
					.encodeToString(ByteBuffer.allocate(iv.length + cifrado.length).put(iv).put(cifrado).array());
		} catch (GeneralSecurityException e) {
			throw new IllegalStateException(e);
		}
	}

	public String decifrar(String base64) {
		try {
			byte[] tudo = Base64.getDecoder().decode(base64);
			Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
			cipher.init(Cipher.DECRYPT_MODE, chave, new GCMParameterSpec(TAMANHO_TAG_BITS, tudo, 0, TAMANHO_IV));
			return new String(cipher.doFinal(tudo, TAMANHO_IV, tudo.length - TAMANHO_IV), StandardCharsets.UTF_8);
		} catch (GeneralSecurityException e) {
			throw new IllegalStateException("Não foi possível decifrar o código da etiqueta.", e);
		}
	}

}
