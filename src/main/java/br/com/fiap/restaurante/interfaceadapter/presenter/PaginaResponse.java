package br.com.fiap.restaurante.interfaceadapter.presenter;

import java.util.List;

public record PaginaResponse<T>(List<T> conteudo, int pagina, int tamanho, long totalElementos, int totalPaginas,
                                boolean ultima) {
}
