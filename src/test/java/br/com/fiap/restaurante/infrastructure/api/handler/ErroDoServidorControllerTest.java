package br.com.fiap.restaurante.infrastructure.api.handler;

import br.com.fiap.restaurante.infrastructure.config.ProblemasProperties;
import jakarta.servlet.RequestDispatcher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Erros encaminhados ao /error")
class ErroDoServidorControllerTest {

    @ParameterizedTest(name = "ERR-13 · erro {0} encaminhado ao /error sai em ProblemDetail com o título \"{1}\"")
    @CsvSource(delimiter = '|', value = {
            "405 | Método não permitido | O método da requisição não é suportado nesta rota.",
            "500 | Erro interno | Ocorreu uma falha inesperada ao processar a requisição.",
            "400 | Requisição inválida | A requisição está malformada e não pôde ser interpretada.",
            "403 | Requisição inválida | A requisição não pôde ser atendida."
    })
    void deveResponderOErroEncaminhado(int status, String titulo, String detalhe) {
        /* arrange */
        ErroDoServidorController controller =
                new ErroDoServidorController(new ProblemasProperties(URI.create("http://localhost:8080")));
        MockHttpServletRequest requisicao = new MockHttpServletRequest("GET", "/error");
        requisicao.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, status);
        requisicao.setAttribute(RequestDispatcher.ERROR_REQUEST_URI, "/api/v1/restaurantes");

        /* act */
        ResponseEntity<ProblemDetail> resposta = controller.responder(requisicao);

        /* assert */
        assertThat(resposta.getStatusCode().value()).isEqualTo(status);
        assertThat(resposta.getBody().getTitle()).isEqualTo(titulo);
        assertThat(resposta.getBody().getDetail()).isEqualTo(detalhe);
        assertThat(resposta.getBody().getInstance()).hasToString("/api/v1/restaurantes");
    }

    @Test
    @DisplayName("ERR-13 · o /error acessado direto, sem erro encaminhado, responde 404 citando o próprio /error")
    void deveResponder404AoErrorAcessadoDireto() {
        /* arrange */
        ErroDoServidorController controller =
                new ErroDoServidorController(new ProblemasProperties(URI.create("http://localhost:8080")));

        /* act */
        ResponseEntity<ProblemDetail> resposta = controller.responder(new MockHttpServletRequest("GET", "/error"));

        /* assert */
        assertThat(resposta.getStatusCode().value()).isEqualTo(404);
        assertThat(resposta.getHeaders().getContentType()).hasToString("application/problem+json");
        assertThat(resposta.getBody().getDetail()).isEqualTo("Não existe recurso no caminho /error.");
        assertThat(resposta.getBody().getInstance()).hasToString("/error");
        assertThat(resposta.getBody().getProperties()).containsKey("momento");
    }

    @Test
    @DisplayName("ENT-06 · o 413 do filtro, encaminhado ao /error, sai com o tipo do corpo grande demais")
    void deveResponderOCorpoGrandeDemaisEncaminhado() {
        /* arrange */
        ErroDoServidorController controller =
                new ErroDoServidorController(new ProblemasProperties(URI.create("http://localhost:8080")));
        MockHttpServletRequest requisicao = new MockHttpServletRequest("GET", "/error");
        requisicao.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, 413);
        requisicao.setAttribute(RequestDispatcher.ERROR_REQUEST_URI, "/api/v1/restaurantes");

        /* act */
        ResponseEntity<ProblemDetail> resposta = controller.responder(requisicao);

        /* assert */
        assertThat(resposta.getStatusCode().value()).isEqualTo(413);
        assertThat(resposta.getBody().getType()).hasToString("http://localhost:8080/problemas/corpo-grande-demais");
        assertThat(resposta.getBody().getDetail()).isEqualTo("O corpo da requisição passa do limite de 1 MB.");
    }
}
