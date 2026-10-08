package br.com.fiap.restaurante.infrastructure.api.handler;

import br.com.fiap.restaurante.infrastructure.config.ProblemasProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tratador de erros")
class TratadorDeErrosTest {

    @Test
    @DisplayName("ERR-10 · uma resposta cujo corpo não é ProblemDetail passa sem alteração")
    void naoDeveAlterarCorpoQueNaoSejaProblemDetail() {
        /* arrange */
        TratadorDeErros tratador = new TratadorDeErros(new ProblemasProperties(URI.create("http://localhost:8080")));
        ServletWebRequest requisicao = new ServletWebRequest(new MockHttpServletRequest("GET", "/qualquer"));

        /* act */
        ResponseEntity<Object> resposta =
                tratador.createResponseEntity("texto simples", new HttpHeaders(), HttpStatus.BAD_REQUEST, requisicao);

        /* assert */
        assertThat(resposta.getBody()).isEqualTo("texto simples");
    }
}
