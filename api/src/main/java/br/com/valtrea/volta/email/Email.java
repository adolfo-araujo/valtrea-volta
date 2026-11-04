package br.com.valtrea.volta.email;

/** E-mail pronto para envio. Publicado como evento e enviado só depois que a transação do banco é gravada. */
public record Email(String para, String assunto, String texto) {

	public static Email confirmarEmail(String para, String nome, String codigo, long minutos) {
		return new Email(para, "Seu código Valtrea: " + codigo, """
				Olá, %s!

				Use este código no Valtrea Volta para confirmar seu e-mail:

				    %s

				Ele vale por %d minutos. Se você não criou uma conta na Valtrea, ignore este e-mail.

				— Equipe Valtrea
				""".formatted(primeiroNome(nome), codigo, minutos));
	}

	public static Email redefinirSenha(String para, String nome, String codigo, long minutos) {
		return new Email(para, "Código para redefinir sua senha Valtrea", """
				Olá, %s!

				Recebemos um pedido para redefinir a senha da sua conta no Valtrea Volta. Use este código:

				    %s

				Ele vale por %d minutos. Se não foi você, ignore este e-mail: sua senha continua a mesma.

				— Equipe Valtrea
				""".formatted(primeiroNome(nome), codigo, minutos));
	}

	public static Email contaBloqueada(String para, String nome, long minutos) {
		return new Email(para, "Tentativas de acesso à sua conta Valtrea", """
				Olá, %s!

				Houve várias tentativas de entrar na sua conta com a senha errada, então bloqueamos o login por %d minutos.

				Se foi você, é só esperar e tentar de novo, ou usar "Esqueci minha senha" no app.
				Se não foi você, recomendamos redefinir sua senha.

				— Equipe Valtrea
				""".formatted(primeiroNome(nome), minutos));
	}

	public static Email senhaAlterada(String para, String nome) {
		return new Email(para, "Sua senha Valtrea foi alterada", """
				Olá, %s!

				A senha da sua conta acabou de ser alterada e todos os aparelhos foram desconectados.

				Se não foi você, fale com a gente imediatamente: contato@valtrea.com.br

				— Equipe Valtrea
				""".formatted(primeiroNome(nome)));
	}

	public static Email contaExcluida(String para, String nome) {
		return new Email(para, "Sua conta Valtrea Volta foi excluída", """
				Olá, %s!

				Sua conta no Valtrea Volta foi excluída, como você pediu. Apagamos seus dados, seus objetos e os
				avisos recebidos, e as etiquetas com QR Code pararam de funcionar.

				Se não foi você, fale com a gente imediatamente: contato@valtrea.com.br

				— Equipe Valtrea
				""".formatted(primeiroNome(nome)));
	}

	/** Sem a mensagem nem o contato de quem encontrou: o dono lê tudo no app, depois de entrar. */
	public static Email novoAviso(String para, String nome, String objeto, String urlApp) {
		return new Email(para, "Alguém encontrou: " + objeto, """
				Olá, %s!

				Alguém leu o QR Code do seu objeto "%s" e deixou uma mensagem para você.

				Para ler e combinar a devolução, entre no Valtrea Volta: %s

				Dica: para a devolução, prefira um lugar público e movimentado.

				— Equipe Valtrea
				""".formatted(primeiroNome(nome), objeto, urlApp));
	}

	private static String primeiroNome(String nome) {
		return nome.strip().split("\\s+")[0];
	}

}
