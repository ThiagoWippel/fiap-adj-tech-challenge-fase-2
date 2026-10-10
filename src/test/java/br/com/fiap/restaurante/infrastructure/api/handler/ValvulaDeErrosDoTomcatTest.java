package br.com.fiap.restaurante.infrastructure.api.handler;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Válvula de erros do Tomcat")
class ValvulaDeErrosDoTomcatTest {

    private final ValvulaDeErrosDoTomcat valvula = new ValvulaDeErrosDoTomcat(URI.create("http://localhost:8080"));
    private Request requisicao;
    private Response resposta;

    @BeforeEach
    void preparar() {
        requisicao = mock(Request.class);
        resposta = mock(Response.class);
        when(requisicao.getRequestURI()).thenReturn("/api/v1/usuarios/%zz");
        when(resposta.getStatus()).thenReturn(400);
        when(resposta.setErrorReported()).thenReturn(true);
    }

    @Test
    @DisplayName("ERR-17 · erro do Tomcat sem corpo vira ProblemDetail")
    void deveEscreverOProblema() throws IOException {
        /* arrange */
        StringWriter corpo = new StringWriter();
        when(resposta.getReporter()).thenReturn(new PrintWriter(corpo));

        /* act */
        valvula.report(requisicao, resposta, null);

        /* assert */
        verify(resposta).setContentType("application/problem+json");
        assertThat(corpo.toString())
                .contains("\"type\":\"http://localhost:8080/problemas/requisicao-invalida\"")
                .contains("\"status\":400")
                .contains("\"instance\":\"/api/v1/usuarios/%zz\"");
    }

    @Test
    @DisplayName("ERR-17 · resposta de sucesso, já escrita ou já relatada passa sem alteração")
    void naoDeveMexerNaRespostaQueNaoPrecisa() throws IOException {
        /* arrange */
        Response sucesso = mock(Response.class);
        when(sucesso.getStatus()).thenReturn(200);
        Response jaEscrita = mock(Response.class);
        when(jaEscrita.getStatus()).thenReturn(404);
        when(jaEscrita.getContentWritten()).thenReturn(120L);
        Response jaRelatada = mock(Response.class);
        when(jaRelatada.getStatus()).thenReturn(500);
        when(jaRelatada.setErrorReported()).thenReturn(false);

        /* act */
        valvula.report(requisicao, sucesso, null);
        valvula.report(requisicao, jaEscrita, null);
        valvula.report(requisicao, jaRelatada, null);

        /* assert */
        verify(sucesso, never()).getReporter();
        verify(jaEscrita, never()).getReporter();
        verify(jaRelatada, never()).getReporter();
    }

    @Test
    @DisplayName("ERR-17 · sem como escrever o corpo, fica só o status, sem lançar exceção")
    void deveTolerarRespostaSemCorpo() throws IOException {
        /* arrange */
        Response semEscritor = mock(Response.class);
        when(semEscritor.getStatus()).thenReturn(400);
        when(semEscritor.setErrorReported()).thenReturn(true);
        when(resposta.getReporter()).thenThrow(new IOException("conexão fechada"));

        /* act + assert */
        assertThatCode(() -> valvula.report(requisicao, semEscritor, null)).doesNotThrowAnyException();
        assertThatCode(() -> valvula.report(requisicao, resposta, null)).doesNotThrowAnyException();
    }
}
