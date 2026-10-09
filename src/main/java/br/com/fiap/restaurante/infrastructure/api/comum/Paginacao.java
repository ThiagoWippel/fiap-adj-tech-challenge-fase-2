package br.com.fiap.restaurante.infrastructure.api.comum;

import br.com.fiap.restaurante.application.dto.PedidoDePagina;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Converte o {@link Pageable} do Spring no pedido de página dos casos de uso. O
 * tamanho máximo (50) vem de spring.data.web.pageable.max-page-size.
 */
public final class Paginacao {

    private Paginacao() {
    }

    /**
     * Sem a lista de campos, um sort inválido só falharia na consulta ao banco, como
     * erro 500, e daria para ordenar por colunas que a API não mostra, como a senha.
     */
    public static PedidoDePagina pedido(Pageable paginacao, Set<String> camposOrdenaveis) {
        garantirQueOBancoAlcanca(paginacao);
        List<PedidoDePagina.Ordem> ordenacao = paginacao.getSort().stream()
                .map(ordem -> ordem(ordem, camposOrdenaveis))
                .toList();
        return new PedidoDePagina(paginacao.getPageNumber(), paginacao.getPageSize(), ordenacao);
    }

    // O banco pula no máximo Integer.MAX_VALUE registros; acima disso a consulta
    // falharia como erro 500.
    private static void garantirQueOBancoAlcanca(Pageable paginacao) {
        long deslocamento = (long) paginacao.getPageNumber() * paginacao.getPageSize();
        if (deslocamento > Integer.MAX_VALUE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A página " + paginacao.getPageNumber()
                    + " passa do limite: com " + paginacao.getPageSize() + " itens por página, a última página "
                    + "possível é " + Integer.MAX_VALUE / paginacao.getPageSize() + ".");
        }
    }

    private static PedidoDePagina.Ordem ordem(Sort.Order ordem, Set<String> camposOrdenaveis) {
        if (!camposOrdenaveis.contains(ordem.getProperty())) {
            String aceitos = String.join(", ", new TreeSet<>(camposOrdenaveis));
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Não é possível ordenar por " + ordem.getProperty() + ". Campos aceitos: " + aceitos + ".");
        }
        return new PedidoDePagina.Ordem(ordem.getProperty(), ordem.isAscending());
    }
}
