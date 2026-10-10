package br.com.fiap.restaurante.infrastructure.api.comum;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
