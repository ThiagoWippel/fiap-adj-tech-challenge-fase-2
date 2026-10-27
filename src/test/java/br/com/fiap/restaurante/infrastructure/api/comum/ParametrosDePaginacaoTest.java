package br.com.fiap.restaurante.infrastructure.api.comum;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Parâmetros de paginação")
class ParametrosDePaginacaoTest {

    private final ParametrosDePaginacao interceptor = new ParametrosDePaginacao();

    @Test
    @DisplayName("ENT-02 · só confere as rotas que recebem paginação")
    void deveIgnorarRotasSemPaginacao() throws NoSuchMethodException {
        /* arrange */
        MockHttpServletRequest requisicao = requisicao("page", "abc");
        HandlerMethod semPaginacao = new HandlerMethod(new Rotas(), Rotas.class.getMethod("buscar", String.class));

        /* act + assert */
        assertThat(interceptor.preHandle(requisicao, new MockHttpServletResponse(), semPaginacao)).isTrue();
        assertThat(interceptor.preHandle(requisicao, new MockHttpServletResponse(), new Object())).isTrue();
    }

    @Test
    @DisplayName("ENT-02 · page vazio vale como ausente; page acima do maior inteiro é recusado")
    void deveConferirOsLimitesDoPage() throws NoSuchMethodException {
        /* arrange */
        HandlerMethod listagem = new HandlerMethod(new Rotas(), Rotas.class.getMethod("listar", Pageable.class));

        /* act + assert */
        assertThat(interceptor.preHandle(requisicao("page", ""), new MockHttpServletResponse(), listagem)).isTrue();
        assertThat(interceptor.preHandle(requisicao("sort", "nome,desc"), new MockHttpServletResponse(), listagem))
                .isTrue();
        assertThatThrownBy(() -> interceptor.preHandle(requisicao("page", "2147483648"), new MockHttpServletResponse(),
                listagem))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("O parâmetro page deve ser um número inteiro a partir de 0.");
    }

    @ParameterizedTest(name = "ENT-02 · {0}={1} é recusado com 400")
    @CsvSource(delimiter = '|', value = {
            "page | abc | O parâmetro page deve ser um número inteiro a partir de 0.",
            "page | -1  | O parâmetro page deve ser um número inteiro a partir de 0.",
            "page | 1.5 | O parâmetro page deve ser um número inteiro a partir de 0.",
            "size | 0   | O parâmetro size deve ser um número inteiro a partir de 1.",
            "size | abc | O parâmetro size deve ser um número inteiro a partir de 1."
    })
    void deveRecusarPageESizeInvalidos(String parametro, String valor, String mensagem) throws NoSuchMethodException {
        /* arrange */
        HandlerMethod listagem = new HandlerMethod(new Rotas(), Rotas.class.getMethod("listar", Pageable.class));

        /* act + assert */
        assertThatThrownBy(() -> interceptor.preHandle(requisicao(parametro, valor), new MockHttpServletResponse(),
                listagem))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining(mensagem);
    }

    @Test
    @DisplayName("ENT-01 · sort com caractere que não cabe num nome de campo é recusado com 400; size válido passa")
    void deveRecusarSortComCaractereInvalido() throws NoSuchMethodException {
        /* arrange */
        HandlerMethod listagem = new HandlerMethod(new Rotas(), Rotas.class.getMethod("listar", Pageable.class));

        /* act + assert */
        assertThat(interceptor.preHandle(requisicao("size", "50"), new MockHttpServletResponse(), listagem)).isTrue();
        assertThatThrownBy(() -> interceptor.preHandle(requisicao("sort", "nome;desc"), new MockHttpServletResponse(),
                listagem))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("O parâmetro sort aceita nomes de campo e a direção, como nome,desc.");
    }

    private static MockHttpServletRequest requisicao(String parametro, String valor) {
        MockHttpServletRequest requisicao = new MockHttpServletRequest("GET", "/listagem");
        requisicao.addParameter(parametro, valor);
        return requisicao;
    }

    static class Rotas {

        public void listar(Pageable paginacao) {
            // só a assinatura importa
        }

        public void buscar(String nome) {
            // só a assinatura importa
        }
    }
}
