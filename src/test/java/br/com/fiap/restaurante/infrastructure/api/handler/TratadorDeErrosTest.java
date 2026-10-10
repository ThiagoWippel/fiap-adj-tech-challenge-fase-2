package br.com.fiap.restaurante.infrastructure.api.handler;

import br.com.fiap.restaurante.infrastructure.config.ProblemasProperties;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;
import tools.jackson.core.JacksonException;

import java.math.BigDecimal;
import java.net.URI;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

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

    @Test
    @DisplayName("ERR-01 · o momento sai sempre com os segundos, mesmo quando são zero, e sem frações")
    void deveFormatarOMomentoSempreComSegundos() {
        /* act + assert */
        assertThat(TratadorDeErros.momento(LocalDateTime.of(2026, 10, 8, 10, 15, 0, 500_000_000)))
                .isEqualTo("2026-10-08T10:15:00");
        assertThat(TratadorDeErros.momento(LocalDateTime.of(2026, 10, 8, 10, 15, 42)))
                .isEqualTo("2026-10-08T10:15:42");
    }

    @ParameterizedTest(name = "ERR-16 · a restrição sai da mensagem do banco: \"{0}\" vira {1}")
    @CsvSource(delimiter = '|', value = {
            "Duplicate entry 'maria@exemplo.com' for key 'usuario.uk_usuario_email' | usuario.uk_usuario_email",
            "a foreign key constraint fails (`db`.`usuario`, CONSTRAINT `fk_usuario_tipo` FOREIGN KEY) | fk_usuario_tipo",
            "Check constraint 'ck_item_preco_positivo' is violated. | ck_item_preco_positivo",
            "uma mensagem sem restrição | não identificada"
    })
    void deveAcharARestricaoNaMensagemDoBanco(String mensagem, String restricao) {
        /* act + assert */
        assertThat(TratadorDeErros.restricaoViolada(new DataIntegrityViolationException(mensagem)))
                .isEqualTo(restricao);
    }

    @Test
    @DisplayName("ERR-16 · o nome da restrição informado pelo Hibernate tem preferência; sem ele, vale a mensagem")
    void devePreferirARestricaoDoHibernate() {
        /* arrange */
        SQLException banco = new SQLException("Duplicate entry 'x' for key 'uk_da_mensagem'");
        var comNome = new DataIntegrityViolationException("falhou",
                new ConstraintViolationException("falhou", banco, "uk_do_hibernate"));
        var semNome = new DataIntegrityViolationException("falhou",
                new ConstraintViolationException("falhou", banco, null));

        /* act + assert */
        assertThat(TratadorDeErros.restricaoViolada(comNome)).isEqualTo("uk_do_hibernate");
        assertThat(TratadorDeErros.restricaoViolada(semNome)).isEqualTo("uk_da_mensagem");
    }

    @Test
    @DisplayName("ERR-14 · o caminho do campo junta nomes com ponto e índices entre colchetes")
    void deveMontarOCaminhoDoCampo() {
        /* arrange */
        List<JacksonException.Reference> caminho = List.of(new JacksonException.Reference(null, "horarios"),
                new JacksonException.Reference(null, 2), new JacksonException.Reference(null, "abertura"),
                new JacksonException.Reference(null));

        /* act + assert */
        assertThat(TratadorDeErros.caminhoDoCampo(caminho)).isEqualTo("horarios[2].abertura");
    }

    @Test
    @DisplayName("ERR-14 · a mensagem do campo diz o tipo esperado")
    void deveDizerOTipoEsperado() {
        /* act + assert */
        assertThat(TratadorDeErros.valorEsperado(String.class)).isEqualTo("Informe um texto.");
        assertThat(TratadorDeErros.valorEsperado(Long.class)).isEqualTo("Informe um número inteiro.");
        assertThat(TratadorDeErros.valorEsperado(int.class)).isEqualTo("Informe um número inteiro.");
        assertThat(TratadorDeErros.valorEsperado(long.class)).isEqualTo("Informe um número inteiro.");
        assertThat(TratadorDeErros.valorEsperado(Integer.class)).isEqualTo("Informe um número inteiro.");
        assertThat(TratadorDeErros.valorEsperado(BigDecimal.class)).isEqualTo("Informe um número.");
        assertThat(TratadorDeErros.valorEsperado(Boolean.class)).isEqualTo("Informe true ou false.");
        assertThat(TratadorDeErros.valorEsperado(boolean.class)).isEqualTo("Informe true ou false.");
        assertThat(TratadorDeErros.valorEsperado(List.class)).isEqualTo("Informe uma lista.");
        assertThat(TratadorDeErros.valorEsperado(String[].class)).isEqualTo("Informe uma lista.");
        assertThat(TratadorDeErros.valorEsperado(Object.class)).isEqualTo("Informe um objeto.");
        assertThat(TratadorDeErros.valorEsperado(null)).isEqualTo("O valor tem o tipo errado.");
    }
}
