package br.com.fiap.restaurante.application.exception;

/**
 * Lançada quando login e senha não conferem, no login e na troca de senha.
 *
 * <p>A mensagem é a mesma para login inexistente e senha errada, para não revelar
 * quais logins existem no sistema.
 */
public class CredenciaisInvalidasException extends RuntimeException {

    public CredenciaisInvalidasException(String mensagem) {
        super(mensagem);
    }
}
