package br.com.fiap.restaurante.domain.exception;

/**
 * Combinação de dados que o negócio não aceita, como um documento incompatível
 * com o tipo do usuário.
 */
public class RegraDeNegocioException extends RuntimeException {

    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}
