package br.com.fiap.restaurante.application.exception;

/**
 * O recurso pedido não existe ou foi removido.
 */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
