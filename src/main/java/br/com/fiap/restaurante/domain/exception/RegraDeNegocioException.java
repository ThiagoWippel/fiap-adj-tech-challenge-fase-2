package br.com.fiap.restaurante.domain.exception;

/**
 * Lançada quando a operação viola uma regra de negócio que envolve mais de um
 * dado, como o documento exigido pelo tipo do usuário.
 *
 * <p>Separada de {@link ValidacaoDeDominioException} porque a resposta é outra: o
 * cliente não enviou um dado malformado, enviou uma combinação que o negócio não
 * aceita. A distinção aparece no título do erro devolvido pela API.
 */
public class RegraDeNegocioException extends RuntimeException {

    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}
