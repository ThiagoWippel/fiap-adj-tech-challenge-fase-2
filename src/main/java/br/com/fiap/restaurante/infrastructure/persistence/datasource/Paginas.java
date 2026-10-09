package br.com.fiap.restaurante.infrastructure.persistence.datasource;

import br.com.fiap.restaurante.application.dto.Pagina;
import br.com.fiap.restaurante.application.dto.PedidoDePagina;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Conversões entre a paginação dos casos de uso e a do Spring Data. Os campos de
 * ordenação já chegam conferidos pela API.
 */
final class Paginas {

    private Paginas() {
    }

    /**
     * O id entra no fim como desempate. Sem ele, registros com o mesmo valor no
     * campo de ordenação saem em ordem qualquer, e um deles pode aparecer em duas
     * páginas e sumir de outra.
     */
    static PageRequest paraPageRequest(PedidoDePagina pedido) {
        List<Sort.Order> ordens = new ArrayList<>(pedido.ordenacao().stream()
                .map(ordem -> ordem.crescente() ? Sort.Order.asc(ordem.campo()) : Sort.Order.desc(ordem.campo()))
                .toList());
        if (ordens.stream().noneMatch(ordem -> ordem.getProperty().equals("id"))) {
            ordens.add(Sort.Order.asc("id"));
        }
        return PageRequest.of(pedido.numero(), pedido.tamanho(), Sort.by(ordens));
    }

    static <E, D> Pagina<D> paraPagina(Page<E> pagina, Function<E, D> conversao) {
        return new Pagina<>(pagina.getContent().stream().map(conversao).toList(), pagina.getNumber(),
                pagina.getSize(), pagina.getTotalElements(), pagina.getTotalPages());
    }
}
