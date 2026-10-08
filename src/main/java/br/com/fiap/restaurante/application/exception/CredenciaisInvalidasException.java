package br.com.fiap.restaurante.application.exception;

/**
 * Login e senha não conferem. A mensagem é a mesma para login inexistente e
 * senha errada, para não revelar quais logins existem.
 */
public class CredenciaisInvalidasException extends RuntimeException {

    public CredenciaisInvalidasException(String mensagem) {
        super(mensagem);
    }
}
