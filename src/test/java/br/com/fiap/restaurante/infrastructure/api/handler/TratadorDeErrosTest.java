package br.com.fiap.restaurante.infrastructure.api.handler;

import br.com.fiap.restaurante.application.exception.ConflitoDeDadosException;
import br.com.fiap.restaurante.application.exception.CredenciaisInvalidasException;
import br.com.fiap.restaurante.application.exception.RecursoNaoEncontradoException;
import br.com.fiap.restaurante.domain.exception.RegraDeNegocioException;
import br.com.fiap.restaurante.domain.exception.ValidacaoDeDominioException;
import br.com.fiap.restaurante.infrastructure.api.comum.CorpoGrandeDemaisException;
import br.com.fiap.restaurante.infrastructure.config.ProblemasProperties;
import org.apache.tomcat.util.http.InvalidParameterException;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.core.MethodParameter;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.exc.MismatchedInputException;

import java.io.IOException;
import java.io.UncheckedIOException;
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

    @Test
    @DisplayName("ERR-01 · todo problema leva o type, o título, o status, a rota chamada em instance e o momento")
    void deveCompletarOsCamposDoProblema() {
        /* act */
        ResponseEntity<Object> resposta = tratador().tratarValidacaoDeDominio(
                new ValidacaoDeDominioException("O nome é obrigatório."), requisicao("/api/v1/tipos-usuario"));

        /* assert */
        ProblemDetail problema = (ProblemDetail) resposta.getBody();
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(problema.getType()).hasToString("http://localhost:8080/problemas/dados-invalidos");
        assertThat(problema.getTitle()).isEqualTo("Dados inválidos");
        assertThat(problema.getStatus()).isEqualTo(400);
        assertThat(problema.getDetail()).isEqualTo("O nome é obrigatório.");
        assertThat(problema.getInstance()).hasToString("/api/v1/tipos-usuario");
        assertThat(problema.getProperties().get("momento")).asString()
                .matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}");
    }

    @Test
    @DisplayName("ERR-09 · LOG-02 · USU-24 · USU-20 · as exceções da aplicação viram o status e o título do seu tipo")
    void deveResponderAsExcecoesDaAplicacao() {
        /* arrange */
        TratadorDeErros tratador = tratador();
        ServletWebRequest requisicao = requisicao("/api/v1/usuarios/7");

        /* act */
        ProblemDetail regra = problema(tratador.tratarRegraDeNegocio(
                new RegraDeNegocioException("Cliente precisa de CPF."), requisicao));
        ProblemDetail credenciais = problema(tratador.tratarCredenciaisInvalidas(
                new CredenciaisInvalidasException("Login ou senha inválidos."), requisicao));
        ProblemDetail naoEncontrado = problema(tratador.tratarRecursoNaoEncontrado(
                new RecursoNaoEncontradoException("Usuário 7 não encontrado."), requisicao));
        ProblemDetail conflito = problema(tratador.tratarConflito(
                new ConflitoDeDadosException("O e-mail já está em uso."), requisicao));

        /* assert */
        assertThat(regra.getStatus()).isEqualTo(400);
        assertThat(regra.getTitle()).isEqualTo("Regra de negócio violada");
        assertThat(credenciais.getStatus()).isEqualTo(401);
        assertThat(credenciais.getDetail()).isEqualTo("Login ou senha inválidos.");
        assertThat(naoEncontrado.getStatus()).isEqualTo(404);
        assertThat(naoEncontrado.getInstance()).hasToString("/api/v1/usuarios/7");
        assertThat(conflito.getStatus()).isEqualTo(409);
        assertThat(conflito.getType()).hasToString("http://localhost:8080/problemas/conflito-de-dados");
    }

    @Test
    @DisplayName("ERR-16 · CON-04 · as falhas do banco viram 409, sem repetir o valor que conflitou")
    void deveResponderAsFalhasDoBanco() {
        /* arrange */
        TratadorDeErros tratador = tratador();
        ServletWebRequest requisicao = requisicao("/api/v1/usuarios");
        var integridade = new DataIntegrityViolationException(
                "Duplicate entry 'maria@exemplo.com' for key 'usuario.uk_usuario_email'");

        /* act */
        ResponseEntity<Object> duplicado = tratador.tratarViolacaoDeIntegridade(integridade, requisicao);
        ResponseEntity<Object> concorrencia = tratador.tratarFalhaDeConcorrencia(
                new ConcurrencyFailureException("Lock wait timeout exceeded"), requisicao);

        /* assert */
        assertThat(duplicado.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(problema(duplicado).getDetail())
                .isEqualTo("Os dados informados conflitam com um registro existente.")
                .doesNotContain("maria@exemplo.com");
        assertThat(concorrencia.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(problema(concorrencia).getDetail()).endsWith("Tente de novo.");
    }

    @Test
    @DisplayName("ERR-15 · parâmetro com codificação inválida na URL vira 400, e não 500")
    void deveResponderParametroMalCodificado() {
        /* act */
        ResponseEntity<Object> resposta = tratador().tratarParametroMalCodificado(
                new InvalidParameterException("Character decoding failed"), requisicao("/api/v1/usuarios"));

        /* assert */
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(problema(resposta).getTitle()).isEqualTo("Requisição inválida");
    }

    @Test
    @DisplayName("ERR-07 · ERR-08 · uma falha inesperada vira 500 sem a mensagem da exceção no corpo")
    void deveEsconderAFalhaInesperada() {
        /* act */
        ResponseEntity<Object> resposta = tratador().tratarFalhaInesperada(
                new IllegalArgumentException("detalhe interno da falha"), requisicao("/api/v1/restaurantes"));

        /* assert */
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(problema(resposta).getTitle()).isEqualTo("Erro interno");
        assertThat(problema(resposta).getDetail())
                .isEqualTo("Ocorreu uma falha inesperada ao processar a requisição.")
                .doesNotContain("detalhe interno");
    }

    @Test
    @DisplayName("ERR-03 · a falha de validação lista cada campo com a sua mensagem, em ordem de campo")
    void deveListarOsCamposRejeitados() throws NoSuchMethodException {
        /* arrange */
        BeanPropertyBindingResult resultado = new BeanPropertyBindingResult(new Object(), "requisicao");
        resultado.addError(new FieldError("requisicao", "nome", "O nome é obrigatório."));
        resultado.addError(new FieldError("requisicao", "email", "O e-mail informado não é válido."));
        resultado.addError(new FieldError("requisicao", "email", "O e-mail é obrigatório."));
        MethodParameter parametro = new MethodParameter(Rotas.class.getMethod("cadastrar", Object.class), 0);

        /* act */
        ResponseEntity<Object> resposta = tratador().handleMethodArgumentNotValid(
                new MethodArgumentNotValidException(parametro, resultado), new HttpHeaders(), HttpStatus.BAD_REQUEST,
                requisicao("/api/v1/usuarios"));

        /* assert */
        ProblemDetail problema = problema(resposta);
        assertThat(problema.getTitle()).isEqualTo("Dados inválidos");
        assertThat(problema.getDetail()).isEqualTo("Um ou mais campos da requisição não passaram na validação.");
        assertThat(problema.getProperties().get("erros")).isEqualTo(List.of(
                new ErroDeCampo("email", "O e-mail informado não é válido."),
                new ErroDeCampo("email", "O e-mail é obrigatório."),
                new ErroDeCampo("nome", "O nome é obrigatório.")));
    }

    @Test
    @DisplayName("ERR-11 · rota inexistente com barra no fim devolve 404 citando o caminho pedido, com a barra")
    void deveCitarOCaminhoPedido() {
        /* act */
        ResponseEntity<Object> resposta = tratador().handleNoResourceFoundException(
                new NoResourceFoundException(HttpMethod.GET, "/api/v1/tipos-usuario/", "api/v1/tipos-usuario"),
                new HttpHeaders(), HttpStatus.NOT_FOUND, requisicao("/api/v1/tipos-usuario/"));

        /* assert */
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(problema(resposta).getDetail()).isEqualTo("Não existe recurso no caminho /api/v1/tipos-usuario/.");
    }

    @Test
    @DisplayName("ENT-06 · o corpo grande demais, mesmo dentro de outra exceção, vira 413")
    void deveResponderCorpoGrandeDemais() {
        /* arrange */
        var excecao = new HttpMessageNotReadableException("falhou ao ler",
                new UncheckedIOException(new IOException(new CorpoGrandeDemaisException())),
                new MockHttpInputMessage(new byte[0]));

        /* act */
        ResponseEntity<Object> resposta = tratador().handleHttpMessageNotReadable(excecao, new HttpHeaders(),
                HttpStatus.BAD_REQUEST, requisicao("/api/v1/restaurantes"));

        /* assert */
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CONTENT_TOO_LARGE);
        assertThat(problema(resposta).getTitle()).isEqualTo("Corpo grande demais");
    }

    @Test
    @DisplayName("ERR-14 · um valor com o tipo errado devolve 400 apontando o campo e o tipo esperado")
    void deveApontarOCampoComTipoErrado() {
        /* arrange */
        MismatchedInputException tipoErrado = MismatchedInputException.from(null, BigDecimal.class, "tipo errado");
        tipoErrado.prependPath(null, "preco");
        var excecao = new HttpMessageNotReadableException("falhou ao ler", tipoErrado,
                new MockHttpInputMessage(new byte[0]));

        /* act */
        ResponseEntity<Object> resposta = tratador().handleHttpMessageNotReadable(excecao, new HttpHeaders(),
                HttpStatus.BAD_REQUEST, requisicao("/api/v1/restaurantes/9/itens-cardapio"));

        /* assert */
        ProblemDetail problema = problema(resposta);
        assertThat(problema.getTitle()).isEqualTo("Requisição inválida");
        assertThat(problema.getDetail()).isEqualTo("O campo preco tem um valor do tipo errado.");
        assertThat(problema.getProperties().get("erros"))
                .isEqualTo(List.of(new ErroDeCampo("preco", "Informe um número.")));
    }

    @Test
    @DisplayName("ERR-04 · ERR-10 · JSON malformado, ou sem campo identificado, segue o Spring e ganha o tipo pelo status")
    void deveCompletarOErroDoSpring() {
        /* arrange */
        var malformado = new HttpMessageNotReadableException("JSON malformado", new MockHttpInputMessage(new byte[0]));
        var semCampo = new HttpMessageNotReadableException("falhou ao ler",
                MismatchedInputException.from(null, BigDecimal.class, "sem caminho"),
                new MockHttpInputMessage(new byte[0]));

        /* act */
        ResponseEntity<Object> semCausa = tratador().handleHttpMessageNotReadable(malformado, new HttpHeaders(),
                HttpStatus.BAD_REQUEST, requisicao("/api/v1/usuarios"));
        ResponseEntity<Object> semCaminho = tratador().handleHttpMessageNotReadable(semCampo, new HttpHeaders(),
                HttpStatus.BAD_REQUEST, requisicao("/api/v1/usuarios"));

        /* assert */
        for (ResponseEntity<Object> resposta : List.of(semCausa, semCaminho)) {
            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(problema(resposta).getType())
                    .hasToString("http://localhost:8080/problemas/requisicao-invalida");
            assertThat(problema(resposta).getTitle()).isEqualTo("Requisição inválida");
            assertThat(problema(resposta).getProperties()).containsKey("momento");
        }
    }

    private static TratadorDeErros tratador() {
        return new TratadorDeErros(new ProblemasProperties(URI.create("http://localhost:8080")));
    }

    private static ServletWebRequest requisicao(String caminho) {
        return new ServletWebRequest(new MockHttpServletRequest("POST", caminho));
    }

    private static ProblemDetail problema(ResponseEntity<Object> resposta) {
        return (ProblemDetail) resposta.getBody();
    }

    static class Rotas {

        public void cadastrar(Object corpo) {
            // só a assinatura importa
        }
    }
}
