package br.com.fiap.restaurante.application.dto;

import java.util.List;

/**
 * Página pedida: número (a partir de 0), tamanho e ordenação.
 */
public record PedidoDePagina(int numero, int tamanho, List<Ordem> ordenacao) {

    public record Ordem(String campo, boolean crescente) {
    }
}
