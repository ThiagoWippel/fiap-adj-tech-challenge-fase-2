package br.com.fiap.restaurante.application.exception;

/**
 * Lançada por um caso de uso quando o recurso pedido não existe ou foi removido.
 */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
