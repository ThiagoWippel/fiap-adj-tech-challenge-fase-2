package br.com.fiap.restaurante.domain.exception;

/**
 * Lançada quando um dado viola uma regra de formato ou de consistência de uma
 * entidade ou de um objeto de valor: nome vazio, CPF inválido, horário que se
 * sobrepõe a outro.
 *
 * <p>Estende {@link IllegalArgumentException}, como as validações do material do
 * curso, de modo que um teste que espera essa exceção continua valendo. Por ser
 * uma classe própria, o tratador de erros a distingue de uma
 * {@code IllegalArgumentException} lançada por engano em outra parte do código:
 * esta vira 400, aquela vira 500.
 */
public class ValidacaoDeDominioException extends IllegalArgumentException {

    public ValidacaoDeDominioException(String mensagem) {
        super(mensagem);
    }
}
