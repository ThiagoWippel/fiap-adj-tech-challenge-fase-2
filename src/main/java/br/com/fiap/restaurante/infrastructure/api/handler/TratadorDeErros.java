package br.com.fiap.restaurante.infrastructure.api.handler;

import br.com.fiap.restaurante.application.exception.ConflitoDeDadosException;
import br.com.fiap.restaurante.application.exception.CredenciaisInvalidasException;
import br.com.fiap.restaurante.application.exception.RecursoNaoEncontradoException;
import br.com.fiap.restaurante.domain.exception.RegraDeNegocioException;
import br.com.fiap.restaurante.domain.exception.ValidacaoDeDominioException;
import br.com.fiap.restaurante.infrastructure.api.problema.TipoDeProblema;
import br.com.fiap.restaurante.infrastructure.config.ProblemasProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

/**
 * Tratamento centralizado de erros, no formato ProblemDetail (RFC 9457, que
 * atualiza a RFC 7807).
 *
 * <p>Toda resposta de erro da API passa por aqui, inclusive as geradas pelo
 * próprio Spring, como JSON malformado, verbo não suportado e rota inexistente.
 * Os cinco campos da RFC são preenchidos sempre: {@code type} aponta para a
 * descrição do problema servida pela aplicação, {@code title} é fixo para cada
 * tipo, {@code detail} descreve a ocorrência, {@code status} e {@code instance}
 * identificam a resposta. Duas extensões completam: {@code momento}, para
 * correlacionar a resposta com o log, e {@code erros}, com os campos rejeitados
 * numa falha de validação.
 *
 * <p>Os campos comuns e as extensões são completados num único método,
 * {@link #createResponseEntity}, por onde passam tanto os erros tratados aqui
 * quanto os tratados pela classe base.
 */
@RestControllerAdvice
// Precedência máxima. Com spring.mvc.problemdetails.enabled=true, o Spring Boot
// registra o próprio tratador de ProblemDetail; sem a ordem explícita, os dois
// disputariam as mesmas exceções.
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TratadorDeErros extends ResponseEntityExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(TratadorDeErros.class);

    private final ProblemasProperties propriedades;

    public TratadorDeErros(ProblemasProperties propriedades) {
        this.propriedades = propriedades;
    }

    @ExceptionHandler(ValidacaoDeDominioException.class)
    public ResponseEntity<Object> tratarValidacaoDeDominio(ValidacaoDeDominioException excecao, WebRequest requisicao) {
        return responder(TipoDeProblema.DADOS_INVALIDOS, excecao.getMessage(), excecao, requisicao);
    }

    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<Object> tratarRegraDeNegocio(RegraDeNegocioException excecao, WebRequest requisicao) {
        return responder(TipoDeProblema.REGRA_DE_NEGOCIO, excecao.getMessage(), excecao, requisicao);
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<Object> tratarCredenciaisInvalidas(CredenciaisInvalidasException excecao, WebRequest requisicao) {
        return responder(TipoDeProblema.CREDENCIAIS_INVALIDAS, excecao.getMessage(), excecao, requisicao);
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<Object> tratarRecursoNaoEncontrado(RecursoNaoEncontradoException excecao, WebRequest requisicao) {
        return responder(TipoDeProblema.RECURSO_NAO_ENCONTRADO, excecao.getMessage(), excecao, requisicao);
    }

    @ExceptionHandler(ConflitoDeDadosException.class)
    public ResponseEntity<Object> tratarConflito(ConflitoDeDadosException excecao, WebRequest requisicao) {
        return responder(TipoDeProblema.CONFLITO_DE_DADOS, excecao.getMessage(), excecao, requisicao);
    }

    /**
     * Rede de segurança para as restrições do banco.
     *
     * <p>Os casos de uso verificam unicidade e referências antes de gravar, mas
     * duas requisições simultâneas podem passar juntas por essa verificação. A
     * restrição do banco resolve a disputa, e este método converte a falha numa
     * resposta 409. A mensagem do banco vai só para o log, porque revela nomes de
     * tabelas e de restrições.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> tratarViolacaoDeIntegridade(DataIntegrityViolationException excecao,
                                                              WebRequest requisicao) {
        LOGGER.warn("Violação de integridade no banco: {}", excecao.getMostSpecificCause().getMessage());
        return responder(TipoDeProblema.CONFLITO_DE_DADOS,
                "Os dados informados conflitam com um registro existente.", excecao, requisicao);
    }

    /**
     * Última barreira: qualquer falha que nenhum outro método previu.
     *
     * <p>O rastro vai para o log, nunca para a resposta, porque revela estrutura
     * interna. Uma {@code IllegalArgumentException} lançada fora do domínio também
     * cai aqui: ela indica um defeito, não um dado inválido do cliente.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> tratarFalhaInesperada(Exception excecao, WebRequest requisicao) {
        LOGGER.error("Falha inesperada ao processar {}", requisicao.getDescription(false), excecao);
        return responder(TipoDeProblema.ERRO_INTERNO,
                "Ocorreu uma falha inesperada ao processar a requisição.", excecao, requisicao);
    }

    /**
     * Falhas de Bean Validation nos DTOs de requisição: a resposta lista cada
     * campo rejeitado, em ordem alfabética, na extensão {@code erros}.
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException excecao,
                                                                  HttpHeaders cabecalhos,
                                                                  HttpStatusCode status,
                                                                  WebRequest requisicao) {
        List<ErroDeCampo> erros = excecao.getBindingResult().getFieldErrors().stream()
                .map(erro -> new ErroDeCampo(erro.getField(), erro.getDefaultMessage()))
                .sorted(Comparator.comparing(ErroDeCampo::campo).thenComparing(ErroDeCampo::mensagem))
                .toList();

        ProblemDetail problema = problemaDe(TipoDeProblema.DADOS_INVALIDOS,
                "Um ou mais campos da requisição não passaram na validação.");
        problema.setProperty("erros", erros);
        return handleExceptionInternal(excecao, problema, cabecalhos, status, requisicao);
    }

    /**
     * O único lugar que completa as respostas de erro.
     *
     * <p>Os erros do próprio Spring chegam sem {@code type} (no Spring 7 o campo
     * fica nulo) e com o título padrão em inglês; recebem aqui o tipo e o título
     * correspondentes ao status. Todos recebem {@code instance}, com a rota chamada, e
     * {@code momento}.
     */
    @Override
    protected ResponseEntity<Object> createResponseEntity(Object corpo,
                                                          HttpHeaders cabecalhos,
                                                          HttpStatusCode status,
                                                          WebRequest requisicao) {
        if (corpo instanceof ProblemDetail problema) {
            completar(problema, requisicao);
        }
        return super.createResponseEntity(corpo, cabecalhos, status, requisicao);
    }

    private ResponseEntity<Object> responder(TipoDeProblema tipo, String detalhe, Exception excecao,
                                             WebRequest requisicao) {
        ProblemDetail problema = problemaDe(tipo, detalhe);
        return handleExceptionInternal(excecao, problema, new HttpHeaders(),
                HttpStatusCode.valueOf(tipo.status()), requisicao);
    }

    private ProblemDetail problemaDe(TipoDeProblema tipo, String detalhe) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(tipo.status()), detalhe);
        problema.setType(tipo.uri(propriedades.baseUri()));
        problema.setTitle(tipo.titulo());
        return problema;
    }

    private void completar(ProblemDetail problema, WebRequest requisicao) {
        if (problema.getType() == null) {
            TipoDeProblema tipo = TipoDeProblema.paraErroDoFramework(problema.getStatus());
            problema.setType(tipo.uri(propriedades.baseUri()));
            problema.setTitle(tipo.titulo());
        }
        problema.setInstance(URI.create(((ServletWebRequest) requisicao).getRequest().getRequestURI()));
        problema.setProperty("momento", LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).toString());
    }
}
