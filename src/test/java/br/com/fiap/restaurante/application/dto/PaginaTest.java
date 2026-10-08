package br.com.fiap.restaurante.application.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Página")
class PaginaTest {

    @Test
    @DisplayName("PAG-01 · indica se é a última página")
    void deveIndicarSeEAUltimaPagina() {
        /* act + assert */
        assertThat(new Pagina<>(List.of("a"), 0, 10, 25, 3).ultima()).isFalse();
        assertThat(new Pagina<>(List.of("a"), 2, 10, 25, 3).ultima()).isTrue();
        assertThat(new Pagina<>(List.of(), 0, 10, 0, 0).ultima()).isTrue();
    }

    @Test
    @DisplayName("PAG-01 · converte o conteúdo sem mudar os dados da paginação")
    void deveConverterOConteudo() {
        /* arrange */
        Pagina<Integer> numeros = new Pagina<>(List.of(1, 2), 1, 2, 6, 3);

        /* act */
        Pagina<String> textos = numeros.map(numero -> "n" + numero);

        /* assert */
        assertThat(textos).isEqualTo(new Pagina<>(List.of("n1", "n2"), 1, 2, 6, 3));
    }
}
