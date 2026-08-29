package br.com.fiap.restaurante.services.exceptions;

/**
 * Violação de regra de negócio que não é expressável de forma declarativa nas
 * anotações de validação. Mapeada para HTTP 400.
 *
 * Exemplo típico: a exigência de CPF para clientes e CNPJ para donos de
 * restaurante, que depende do valor de outro campo da mesma requisição.
 */
public class RegraDeNegocioException extends RuntimeException {

    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}
