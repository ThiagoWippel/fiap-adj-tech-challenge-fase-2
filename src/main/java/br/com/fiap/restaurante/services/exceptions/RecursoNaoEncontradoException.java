package br.com.fiap.restaurante.services.exceptions;

/**
 * Recurso solicitado não existe. Mapeada para HTTP 404.
 *
 * Estende RuntimeException por opção deliberada: a ausência de um registro não
 * é condição que o chamador possa tratar e prosseguir. Obrigar cada camada
 * intermediária a declarar ou capturar só acrescentaria ruído.
 */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
