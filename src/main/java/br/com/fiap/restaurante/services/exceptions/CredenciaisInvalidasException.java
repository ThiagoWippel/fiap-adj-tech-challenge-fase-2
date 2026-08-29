package br.com.fiap.restaurante.services.exceptions;

/**
 * Credenciais não conferem. Mapeada para HTTP 401.
 *
 * A mensagem é deliberadamente genérica e idêntica tanto para login inexistente
 * quanto para senha incorreta. Diferenciá-las permitiria descobrir quais logins
 * existem no sistema por tentativa e erro.
 */
public class CredenciaisInvalidasException extends RuntimeException {

    public CredenciaisInvalidasException(String mensagem) {
        super(mensagem);
    }
}
