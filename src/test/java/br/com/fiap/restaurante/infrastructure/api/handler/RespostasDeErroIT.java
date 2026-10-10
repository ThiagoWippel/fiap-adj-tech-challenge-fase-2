package br.com.fiap.restaurante.infrastructure.api.handler;

import br.com.fiap.restaurante.suporte.ApiDeTeste;
import br.com.fiap.restaurante.suporte.TesteDeIntegracao;
import io.restassured.http.Method;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;

/**
 * Erros que só aparecem com o servidor de verdade: os que o Tomcat produz antes de
 * chegar ao Spring (TRACE, o próprio /error) e os que dependem de como ele decodifica
 * a URL.
 */
@TesteDeIntegracao
@DisplayName("Respostas de erro com o servidor de verdade")
class RespostasDeErroIT {

    private static final String PROBLEMA = "application/problem+json";

    @LocalServerPort
    private int porta;

    private ApiDeTeste api;

    @BeforeEach
    void preparar() {
        api = new ApiDeTeste(porta);
    }

    @Test
    @DisplayName("ERR-11 · rota com barra no fim devolve 404 citando o caminho pedido")
    void deveCitarOCaminhoPedido() {
        api.requisicao().get("/api/v1/tipos-usuario/")
                .then()
                .statusCode(404)
                .contentType(startsWith(PROBLEMA))
                .body("detail", equalTo("Não existe recurso no caminho /api/v1/tipos-usuario/."));
    }

    @Test
    @DisplayName("ERR-13 · TRACE, recusado pelo servidor, devolve 405 em ProblemDetail")
    void deveResponderTraceEmProblemDetail() {
        given().port(porta).request(Method.TRACE, "/api/v1/tipos-usuario")
                .then()
                .statusCode(405)
                .contentType(startsWith(PROBLEMA))
                .body("title", equalTo("Método não permitido"))
                .body("detail", equalTo("O método da requisição não é suportado nesta rota."))
                .body("instance", equalTo("/api/v1/tipos-usuario"))
                .body("momento", containsString("T"));
    }

    @Test
    @DisplayName("ERR-13 · acessar /error diretamente devolve 404 em ProblemDetail")
    void deveResponderOErrorDiretoEmProblemDetail() {
        given().port(porta).get("/error")
                .then()
                .statusCode(404)
                .contentType(startsWith(PROBLEMA))
                .body("title", equalTo("Recurso não encontrado"))
                .body("detail", equalTo("Não existe recurso no caminho /error."));
    }

    @Test
    @DisplayName("ERR-14 · valor com tipo errado num campo aninhado aponta o caminho completo do campo")
    void deveApontarOCampoAninhado() {
        api.requisicao().body("""
                        { "nome": "Maria Silva", "endereco": { "cep": [88301000] } }""")
                .post("/api/v1/usuarios")
                .then()
                .statusCode(400)
                .body("erros[0].campo", equalTo("endereco.cep"))
                .body("erros[0].mensagem", equalTo("Informe um texto."));
    }

    @Test
    @DisplayName("ERR-15 · parâmetro com codificação inválida na URL devolve 400, e não 500")
    void deveRecusarParametroMalCodificado() throws IOException {
        /* act */
        String resposta = requisicaoCrua("GET /api/v1/restaurantes?nome=%zz HTTP/1.1");

        /* assert */
        assertThat(resposta).startsWith("HTTP/1.1 400")
                .contains("Content-Type: " + PROBLEMA)
                .contains("Um parâmetro da URL tem codificação inválida. Confira os caracteres com %.");
    }

    @Test
    @DisplayName("ERR-17 · requisição malformada, barrada pelo Tomcat antes do Spring, também sai em ProblemDetail")
    void deveResponderEmProblemDetail_QuandoOTomcatRecusarARequisicao() throws IOException {
        /* act */
        String caminhoMalCodificado = requisicaoCrua("GET /api/v1/usuarios/%zz HTTP/1.1");
        String cabecalhoGrande = requisicaoCrua("GET /api/v1/tipos-usuario HTTP/1.1",
                "X-Grande: " + "a".repeat(20_000));

        /* assert */
        for (String resposta : List.of(caminhoMalCodificado, cabecalhoGrande)) {
            assertThat(resposta).startsWith("HTTP/1.1 400")
                    .contains("Content-Type: " + PROBLEMA)
                    .contains("\"title\":\"Requisição inválida\"")
                    .contains("\"detail\":\"A requisição está malformada e não pôde ser interpretada.\"")
                    .doesNotContain("<html");
        }
    }

    private String requisicaoCrua(String linha) throws IOException {
        return requisicaoCrua(linha, null);
    }

    // Os clientes HTTP recusam montar uma URL com %zz; um socket manda a linha como está
    private String requisicaoCrua(String linha, String cabecalhoExtra) throws IOException {
        try (Socket socket = new Socket("localhost", porta)) {
            String extra = cabecalhoExtra == null ? "" : cabecalhoExtra + "\r\n";
            socket.getOutputStream().write((linha + "\r\nHost: localhost\r\n" + extra + "Connection: close\r\n\r\n")
                    .getBytes(StandardCharsets.US_ASCII));
            return new String(socket.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
