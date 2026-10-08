package br.com.fiap.restaurante.application.dto;

import java.util.List;
import java.util.function.Function;

/**
 * Uma página de resultados, sem depender da paginação do Spring Data.
 */
public record Pagina<T>(List<T> conteudo, int numero, int tamanho, long totalElementos, int totalPaginas) {

    public boolean ultima() {
        return numero + 1 >= totalPaginas;
    }

    public <R> Pagina<R> map(Function<T, R> conversao) {
        return new Pagina<>(conteudo.stream().map(conversao).toList(), numero, tamanho, totalElementos, totalPaginas);
    }
}
