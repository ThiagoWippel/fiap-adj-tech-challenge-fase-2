package br.com.fiap.restaurante.interfaceadapter.presenter;

import java.util.List;

/**
 * Página de resultados na resposta da API: o conteúdo e os dados da paginação,
 * no mesmo formato da busca v2 da Fase 1.
 */
public record PaginaResponse<T>(List<T> conteudo, int pagina, int tamanho, long totalElementos, int totalPaginas,
                                boolean ultima) {
}
