package br.com.fiap.restaurante.domain.exception;

/**
 * Dado inválido em uma entidade ou objeto de valor (nome vazio, CPF inválido etc.).
 *
 * <p>Estende {@link IllegalArgumentException}, como as validações do material do
 * curso. Sendo uma classe própria, o tratador de erros consegue diferenciá-la de
 * uma {@code IllegalArgumentException} causada por bug: esta vira 400, aquela 500.
 */
public class ValidacaoDeDominioException extends IllegalArgumentException {

    public ValidacaoDeDominioException(String mensagem) {
        super(mensagem);
    }
}
