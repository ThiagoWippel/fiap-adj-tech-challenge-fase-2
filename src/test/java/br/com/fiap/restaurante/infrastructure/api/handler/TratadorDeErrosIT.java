package br.com.fiap.restaurante.infrastructure.api.handler;

import br.com.fiap.restaurante.application.exception.ConflitoDeDadosException;
import br.com.fiap.restaurante.application.exception.CredenciaisInvalidasException;
import br.com.fiap.restaurante.application.exception.RecursoNaoEncontradoException;
import br.com.fiap.restaurante.domain.exception.RegraDeNegocioException;
import br.com.fiap.restaurante.domain.exception.ValidacaoDeDominioException;
import br.com.fiap.restaurante.infrastructure.api.problema.ProblemaController;
import br.com.fiap.restaurante.infrastructure.api.problema.TipoDeProblema;
import br.com.fiap.restaurante.infrastructure.config.ProblemasProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.apache.tomcat.util.http.InvalidParameterException;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Um controller de teste lança cada tipo de exceção e os testes conferem a
 * resposta. O {@code @WebMvcTest} não carrega as propriedades da aplicação, por
 * isso o {@code @EnableConfigurationProperties}.
 */
@WebMvcTest(controllers = {TratadorDeErrosIT.ControllerDeErros.class, ProblemaController.class})
@Import(TratadorDeErrosIT.ControllerDeErros.class)
@EnableConfigurationProperties(ProblemasProperties.class)
@DisplayName("Tratamento de erros")
@ExtendWith(OutputCaptureExtension.class)
class TratadorDeErrosIT {

    private static final String BASE_DOS_TIPOS = "http://localhost:8080/problemas/";
    private static final String FORMATO_DO_MOMENTO = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}";

    @Autowired
    private MockMvcTester mvc;

    @Test
    @DisplayName("ERR-01 · o erro sai como application/problem+json com type, title, status, detail, instance e momento")
    void deveResponderNoFormatoProblemDetail() {
        /* act */
        MvcTestResult resultado = mvc.get().uri("/teste-de-erros/nao-encontrado").exchange();

        /* assert */
        assertThat(resultado).hasStatus(HttpStatus.NOT_FOUND).hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
        assertThat(resultado).bodyJson().extractingPath("$.type").isEqualTo(BASE_DOS_TIPOS + "recurso-nao-encontrado");
        assertThat(resultado).bodyJson().extractingPath("$.title").isEqualTo("Recurso não encontrado");
        assertThat(resultado).bodyJson().extractingPath("$.status").isEqualTo(404);
        assertThat(resultado).bodyJson().extractingPath("$.detail").isEqualTo("Usuário 7 não encontrado.");
        assertThat(resultado).bodyJson().extractingPath("$.instance").isEqualTo("/teste-de-erros/nao-encontrado");
        assertThat(resultado).bodyJson().extractingPath("$.momento").asString().matches(FORMATO_DO_MOMENTO);
    }

    @ParameterizedTest(name = "ERR-01 · {0} devolve {1} com o título \"{2}\"")
    @CsvSource({
            "validacao-de-dominio,  400, Dados inválidos,          dados-invalidos",
            "regra-de-negocio,      400, Regra de negócio violada, regra-de-negocio",
            "credenciais-invalidas, 401, Credenciais inválidas,    credenciais-invalidas",
            "nao-encontrado,        404, Recurso não encontrado,   recurso-nao-encontrado",
            "conflito,              409, Conflito de dados,        conflito-de-dados",
            "integridade,           409, Conflito de dados,        conflito-de-dados"
    })
    void deveMapearCadaExcecaoParaSeuStatusETipo(String rota, int status, String titulo, String tipo) {
        /* act */
        MvcTestResult resultado = mvc.get().uri("/teste-de-erros/" + rota).exchange();

        /* assert */
        assertThat(resultado).hasStatus(status);
        assertThat(resultado).bodyJson().extractingPath("$.title").isEqualTo(titulo);
        assertThat(resultado).bodyJson().extractingPath("$.type").isEqualTo(BASE_DOS_TIPOS + tipo);
    }

    @Test
    @DisplayName("ERR-01 · a violação de integridade do banco não expõe a mensagem do banco")
    void naoDeveExporAMensagemDoBanco_QuandoHouverViolacaoDeIntegridade() {
        /* act */
        MvcTestResult resultado = mvc.get().uri("/teste-de-erros/integridade").exchange();

        /* assert */
        assertThat(resultado).hasStatus(HttpStatus.CONFLICT);
        assertThat(resultado).bodyText().doesNotContain("Duplicate entry").doesNotContain("uk_usuario_email");
    }

    @Test
    @DisplayName("CON-04 · bloqueio esgotado ou impasse no banco devolve 409 e pede para tentar de novo")
    void deveDevolver409_QuandoHouverFalhaDeConcorrencia() {
        /* act */
        MvcTestResult resultado = mvc.get().uri("/teste-de-erros/concorrencia").exchange();

        /* assert */
        assertThat(resultado).hasStatus(HttpStatus.CONFLICT);
        assertThat(resultado).bodyJson().extractingPath("$.type").isEqualTo(BASE_DOS_TIPOS + "conflito-de-dados");
        assertThat(resultado).bodyJson().extractingPath("$.detail")
                .isEqualTo("Outra operação estava alterando o mesmo registro. Tente de novo.");
        assertThat(resultado).bodyText().doesNotContain("Lock wait timeout");
    }

    @Test
    @DisplayName("ERR-11 · o 404 cita o caminho pedido, inclusive com a barra do fim")
    void deveCitarOCaminhoPedido_QuandoARotaTerminarComBarra() {
        /* act */
        MvcTestResult resultado = mvc.get().uri("/rota-que-nao-existe/").exchange();

        /* assert */
        assertThat(resultado).hasStatus(HttpStatus.NOT_FOUND);
        assertThat(resultado).bodyJson().extractingPath("$.detail")
                .isEqualTo("Não existe recurso no caminho /rota-que-nao-existe/.");
    }

    @Test
    @DisplayName("ERR-12 · pedir um formato que a API não produz devolve 406 com tipo próprio")
    void deveDevolver406ComTipoProprio_QuandoOFormatoPedidoNaoExistir() {
        /* act */
        MvcTestResult resultado = mvc.get().uri("/teste-de-erros/objeto").accept(MediaType.APPLICATION_XML).exchange();

        /* assert */
        assertThat(resultado).hasStatus(HttpStatus.NOT_ACCEPTABLE).hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
        assertThat(resultado).bodyJson().extractingPath("$.type").isEqualTo(BASE_DOS_TIPOS + "formato-nao-disponivel");
        assertThat(resultado).bodyJson().extractingPath("$.title").isEqualTo("Formato de resposta não disponível");
    }

    @Test
    @DisplayName("ERR-14 · valor com tipo errado no JSON aponta o campo, inclusive dentro de objetos e listas")
    void deveApontarOCampo_QuandoUmValorTiverOTipoErrado() {
        /* act */
        MvcTestResult textoComObjeto = mvc.post().uri("/teste-de-erros/corpo").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nome": {"a": 1}, "email": "maria@exemplo.com"}""").exchange();
        MvcTestResult numeroComTexto = mvc.post().uri("/teste-de-erros/corpo").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nome": "Maria", "email": "maria@exemplo.com", "idade": "dez"}""").exchange();
        MvcTestResult dentroDaLista = mvc.post().uri("/teste-de-erros/corpo").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nome": "Maria", "email": "maria@exemplo.com", "turnos": [{"abertura": [1]}]}""").exchange();

        /* assert */
        assertThat(textoComObjeto).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(textoComObjeto).bodyJson().extractingPath("$.title").isEqualTo("Requisição inválida");
        assertThat(textoComObjeto).bodyJson().extractingPath("$.detail")
                .isEqualTo("O campo nome tem um valor do tipo errado.");
        assertThat(textoComObjeto).bodyJson().extractingPath("$.erros[0].campo").isEqualTo("nome");
        assertThat(textoComObjeto).bodyJson().extractingPath("$.erros[0].mensagem").isEqualTo("Informe um texto.");
        assertThat(numeroComTexto).bodyJson().extractingPath("$.erros[0].campo").isEqualTo("idade");
        assertThat(numeroComTexto).bodyJson().extractingPath("$.erros[0].mensagem")
                .isEqualTo("Informe um número inteiro.");
        assertThat(dentroDaLista).bodyJson().extractingPath("$.erros[0].campo").isEqualTo("turnos[0].abertura");
    }

    @Test
    @DisplayName("ERR-15 · parâmetro com codificação inválida na URL devolve 400, e não 500")
    void deveDevolver400_QuandoUmParametroTiverCodificacaoInvalida() {
        /* act */
        MvcTestResult resultado = mvc.get().uri("/teste-de-erros/parametro-invalido").exchange();

        /* assert */
        assertThat(resultado).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(resultado).bodyJson().extractingPath("$.title").isEqualTo("Requisição inválida");
        assertThat(resultado).bodyJson().extractingPath("$.detail")
                .isEqualTo("Um parâmetro da URL tem codificação inválida. Confira os caracteres com %.");
    }

    @Test
    @DisplayName("ERR-16 · o log da violação de integridade cita a restrição, e não o dado pessoal que a violou")
    void naoDeveRegistrarDadoPessoal_QuandoHouverViolacaoDeIntegridade(CapturedOutput saida) {
        /* act */
        mvc.get().uri("/teste-de-erros/integridade").exchange();
        mvc.get().uri("/teste-de-erros/integridade-do-hibernate").exchange();

        /* assert */
        assertThat(saida.getOut())
                .contains("Violação de integridade no banco: restrição uk_usuario_email")
                .contains("Violação de integridade no banco: restrição uk_usuario_login")
                .doesNotContain("maria@exemplo.com")
                .doesNotContain("maria.silva");
    }

    @Test
    @DisplayName("ERR-02 · o type aponta para uma URL da própria aplicação que descreve o problema")
    void deveDescreverOTipoDeProblemaNaUrlDoType() {
        /* act */
        MvcTestResult resultado = mvc.get().uri("/problemas/conflito-de-dados").exchange();

        /* assert */
        assertThat(resultado).hasStatusOk();
        assertThat(resultado).bodyJson().extractingPath("$.identificador").isEqualTo("conflito-de-dados");
        assertThat(resultado).bodyJson().extractingPath("$.titulo").isEqualTo("Conflito de dados");
        assertThat(resultado).bodyJson().extractingPath("$.status").isEqualTo(409);
        assertThat(resultado).bodyJson().extractingPath("$.descricao").asString().isNotBlank();
    }

    @Test
    @DisplayName("ERR-02 · a lista de tipos de problema traz todos os tipos que a API usa")
    void deveListarTodosOsTiposDeProblema() {
        /* act */
        MvcTestResult resultado = mvc.get().uri("/problemas").exchange();

        /* assert */
        assertThat(resultado).hasStatusOk();
        assertThat(resultado).bodyJson().extractingPath("$.length()").isEqualTo(TipoDeProblema.values().length);
    }

    @Test
    @DisplayName("ERR-02 · um tipo de problema que não existe devolve 404")
    void deveDevolver404_QuandoOTipoDeProblemaNaoExistir() {
        /* act */
        MvcTestResult resultado = mvc.get().uri("/problemas/inexistente").exchange();

        /* assert */
        assertThat(resultado).hasStatus(HttpStatus.NOT_FOUND).hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    @Test
    @DisplayName("ERR-03 · falha de validação traz a extensão erros com o campo e a mensagem de cada falha")
    void deveListarOsCamposInvalidos_QuandoAValidacaoFalhar() {
        /* arrange */
        String corpo = """
                {"nome": "", "email": "sem-arroba"}
                """;

        /* act */
        MvcTestResult resultado = mvc.post().uri("/teste-de-erros/corpo")
                .contentType(MediaType.APPLICATION_JSON).content(corpo).exchange();

        /* assert */
        assertThat(resultado).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(resultado).bodyJson().extractingPath("$.title").isEqualTo("Dados inválidos");
        assertThat(resultado).bodyJson().extractingPath("$.erros[*].campo").asArray().containsExactly("email", "nome");
        assertThat(resultado).bodyJson().extractingPath("$.erros[*].mensagem").asArray()
                .containsExactly("O e-mail informado não é válido.", "O nome é obrigatório.");
    }

    @Test
    @DisplayName("ERR-04 · JSON malformado devolve 400 em ProblemDetail")
    void deveDevolver400_QuandoOJsonEstiverMalformado() {
        /* act */
        MvcTestResult resultado = mvc.post().uri("/teste-de-erros/corpo")
                .contentType(MediaType.APPLICATION_JSON).content("{\"nome\": ").exchange();

        /* assert */
        assertThat(resultado).hasStatus(HttpStatus.BAD_REQUEST).hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
        assertThat(resultado).bodyJson().extractingPath("$.title").isEqualTo("Requisição inválida");
        assertThat(resultado).bodyJson().extractingPath("$.type").isEqualTo(BASE_DOS_TIPOS + "requisicao-invalida");
        assertThat(resultado).bodyJson().extractingPath("$.detail").asString().startsWith("O corpo da requisição não pôde ser lido.");
    }

    @Test
    @DisplayName("ERR-04 · uma lista no lugar do objeto inteiro devolve a mensagem geral, sem apontar campo")
    void deveUsarAMensagemGeral_QuandoOCorpoInteiroTiverOTipoErrado() {
        /* act */
        MvcTestResult resultado = mvc.post().uri("/teste-de-erros/corpo")
                .contentType(MediaType.APPLICATION_JSON).content("[1]").exchange();

        /* assert */
        assertThat(resultado).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(resultado).bodyJson().extractingPath("$.detail").asString()
                .startsWith("O corpo da requisição não pôde ser lido.");
    }

    @Test
    @DisplayName("ERR-04 · corpo enviado num formato que não é JSON devolve 415 em ProblemDetail")
    void deveDevolver415_QuandoOCorpoNaoForJson() {
        /* act */
        MvcTestResult resultado = mvc.post().uri("/teste-de-erros/corpo")
                .contentType(MediaType.TEXT_PLAIN).content("nome=Maria").exchange();

        /* assert */
        assertThat(resultado).hasStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE).hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
        assertThat(resultado).bodyJson().extractingPath("$.title").isEqualTo("Tipo de mídia não suportado");
        assertThat(resultado).bodyJson().extractingPath("$.detail").asString()
                .startsWith("O tipo de conteúdo text/plain")
                .endsWith("não é suportado. Envie application/json.");
    }

    @Test
    @DisplayName("ERR-05 · verbo HTTP não suportado devolve 405 em ProblemDetail")
    void deveDevolver405_QuandoOVerboNaoForSuportado() {
        /* act */
        MvcTestResult resultado = mvc.delete().uri("/teste-de-erros/nao-encontrado").exchange();

        /* assert */
        assertThat(resultado).hasStatus(HttpStatus.METHOD_NOT_ALLOWED).hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
        assertThat(resultado).bodyJson().extractingPath("$.title").isEqualTo("Método não permitido");
        assertThat(resultado).bodyJson().extractingPath("$.type").isEqualTo(BASE_DOS_TIPOS + "metodo-nao-permitido");
    }

    @Test
    @DisplayName("ERR-06 · rota inexistente devolve 404 em ProblemDetail")
    void deveDevolver404_QuandoARotaNaoExistir() {
        /* act */
        MvcTestResult resultado = mvc.get().uri("/rota-que-nao-existe").exchange();

        /* assert */
        assertThat(resultado).hasStatus(HttpStatus.NOT_FOUND).hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
        assertThat(resultado).bodyJson().extractingPath("$.title").isEqualTo("Recurso não encontrado");
        assertThat(resultado).bodyJson().extractingPath("$.detail").isEqualTo("Não existe recurso no caminho /rota-que-nao-existe.");
        assertThat(resultado).bodyJson().extractingPath("$.instance").isEqualTo("/rota-que-nao-existe");
    }

    @Test
    @DisplayName("ERR-07 · falha inesperada devolve 500 sem rastro de pilha nem mensagem interna no corpo")
    void deveEsconderODetalheInterno_QuandoHouverFalhaInesperada() {
        /* act */
        MvcTestResult resultado = mvc.get().uri("/teste-de-erros/falha").exchange();

        /* assert */
        assertThat(resultado).hasStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(resultado).bodyJson().extractingPath("$.title").isEqualTo("Erro interno");
        assertThat(resultado).bodyText()
                .doesNotContain("IllegalStateException")
                .doesNotContain("conexão perdida")
                .doesNotContain("at br.com.fiap");
    }

    @Test
    @DisplayName("ERR-08 · IllegalArgumentException lançada fora do domínio é tratada como falha interna")
    void deveDevolver500_QuandoUmaIllegalArgumentExceptionVierDeForaDoDominio() {
        /* act */
        MvcTestResult resultado = mvc.get().uri("/teste-de-erros/bug").exchange();

        /* assert */
        assertThat(resultado).hasStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(resultado).bodyJson().extractingPath("$.title").isEqualTo("Erro interno");
    }

    @Test
    @DisplayName("ERR-09 · os erros gerados pelo próprio Spring também saem com título e mensagem em português")
    void deveTraduzirOsErrosDoFramework() {
        /* act */
        MvcTestResult resultado = mvc.delete().uri("/teste-de-erros/nao-encontrado").exchange();

        /* assert */
        assertThat(resultado).bodyJson().extractingPath("$.detail")
                .isEqualTo("O método DELETE não é suportado nesta rota.");
    }

    @Test
    @DisplayName("ERR-10 · a extensão momento aparece também nos erros gerados pelo Spring")
    void deveIncluirOMomento_NosErrosDoFramework() {
        /* act */
        MvcTestResult jsonMalformado = mvc.post().uri("/teste-de-erros/corpo")
                .contentType(MediaType.APPLICATION_JSON).content("{").exchange();
        MvcTestResult rotaInexistente = mvc.get().uri("/rota-que-nao-existe").exchange();
        MvcTestResult verboNaoSuportado = mvc.delete().uri("/teste-de-erros/nao-encontrado").exchange();

        /* assert */
        assertThat(jsonMalformado).bodyJson().extractingPath("$.momento").asString().matches(FORMATO_DO_MOMENTO);
        assertThat(rotaInexistente).bodyJson().extractingPath("$.momento").asString().matches(FORMATO_DO_MOMENTO);
        assertThat(verboNaoSuportado).bodyJson().extractingPath("$.momento").asString().matches(FORMATO_DO_MOMENTO);
    }

    @RestController
    @RequestMapping("/teste-de-erros")
    static class ControllerDeErros {

        @GetMapping("/validacao-de-dominio")
        void validacaoDeDominio() {
            throw new ValidacaoDeDominioException("O nome deve ter entre 3 e 120 caracteres.");
        }

        @GetMapping("/regra-de-negocio")
        void regraDeNegocio() {
            throw new RegraDeNegocioException("O CPF é obrigatório para usuários do tipo Cliente.");
        }

        @GetMapping("/credenciais-invalidas")
        void credenciaisInvalidas() {
            throw new CredenciaisInvalidasException("Login ou senha inválidos.");
        }

        @GetMapping("/nao-encontrado")
        void naoEncontrado() {
            throw new RecursoNaoEncontradoException("Usuário 7 não encontrado.");
        }

        @GetMapping("/conflito")
        void conflito() {
            throw new ConflitoDeDadosException("Já existe um usuário com este e-mail.");
        }

        @GetMapping("/integridade")
        void integridade() {
            throw new DataIntegrityViolationException("Duplicate entry 'maria@exemplo.com' for key 'uk_usuario_email'");
        }

        @GetMapping("/concorrencia")
        void concorrencia() {
            throw new CannotAcquireLockException("Lock wait timeout exceeded; try restarting transaction");
        }

        @GetMapping("/integridade-do-hibernate")
        void integridadeDoHibernate() {
            var causa = new SQLIntegrityConstraintViolationException(
                    "Duplicate entry 'maria.silva' for key 'usuario.uk_usuario_login'");
            throw new DataIntegrityViolationException("could not execute statement",
                    new ConstraintViolationException("could not execute statement", causa, "uk_usuario_login"));
        }

        @GetMapping("/parametro-invalido")
        void parametroInvalido() {
            throw new InvalidParameterException("Character decoding failed. Parameter [nome] with value [%zz]");
        }

        @GetMapping("/objeto")
        Map<String, String> objeto() {
            return Map.of("nome", "Maria");
        }

        @GetMapping("/bug")
        void bug() {
            throw new IllegalArgumentException("Índice fora do intervalo.");
        }

        @GetMapping("/falha")
        void falha() {
            throw new IllegalStateException("conexão perdida com o servidor interno");
        }

        @PostMapping("/corpo")
        void corpo(@Valid @RequestBody CorpoDeTeste corpo) {
            // só valida o corpo
        }

        record CorpoDeTeste(
                @NotBlank(message = "O nome é obrigatório.")
                String nome,

                @NotBlank(message = "O e-mail é obrigatório.")
                @Email(message = "O e-mail informado não é válido.")
                String email,

                Integer idade,

                List<TurnoDeTeste> turnos) {
        }

        record TurnoDeTeste(String abertura) {
        }
    }
}
