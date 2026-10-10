package br.com.fiap.restaurante.infrastructure.api.comum;

import java.io.IOException;

/**
 * O corpo enviado em partes passou do limite de {@link LimiteDoCorpo} durante a
 * leitura. É uma {@link IOException} porque surge de dentro da leitura do corpo.
 */
public class CorpoGrandeDemaisException extends IOException {

    public CorpoGrandeDemaisException() {
        super("O corpo da requisição passa do limite de 1 MB.");
    }
}
