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
import org.springframework.dao.ConcurrencyFailureException;
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
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

/**
 * Converte as exceções da aplicação e do Spring em respostas ProblemDetail
 * (RFC 9457). Além dos campos da RFC, toda resposta leva a extensão
 * {@code momento}, para cruzar com o log, e as falhas de validação levam
 * {@code erros}, com os campos rejeitados.
 */
@RestControllerAdvice
// Com spring.mvc.problemdetails.enabled=true o Spring Boot registra outro tratador;
// a ordem garante que este seja usado.
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TratadorDeErros extends ResponseEntityExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(TratadorDeErros.class);

    // Com padrão fixo: o toString do LocalDateTime omite os segundos quando são zero.
    private static final DateTimeFormatter FORMATO_DO_MOMENTO = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

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
    public ResponseEntity<Object> tratarCredenciaisInvalidas(CredenciaisInvalidasException excecao,
                                                             WebRequest requisicao) {
        return responder(TipoDeProblema.CREDENCIAIS_INVALIDAS, excecao.getMessage(), excecao, requisicao);
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<Object> tratarRecursoNaoEncontrado(RecursoNaoEncontradoException excecao,
                                                             WebRequest requisicao) {
        return responder(TipoDeProblema.RECURSO_NAO_ENCONTRADO, excecao.getMessage(), excecao, requisicao);
    }

    @ExceptionHandler(ConflitoDeDadosException.class)
    public ResponseEntity<Object> tratarConflito(ConflitoDeDadosException excecao, WebRequest requisicao) {
        return responder(TipoDeProblema.CONFLITO_DE_DADOS, excecao.getMessage(), excecao, requisicao);
    }

    // Duas requisições simultâneas podem passar juntas pela verificação de unicidade
    // dos casos de uso; nesse caso é a restrição do banco que barra a segunda.
    // A mensagem do banco fica só no log.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> tratarViolacaoDeIntegridade(DataIntegrityViolationException excecao,
                                                              WebRequest requisicao) {
        LOGGER.warn("Violação de integridade no banco: {}", excecao.getMostSpecificCause().getMessage());
        return responder(TipoDeProblema.CONFLITO_DE_DADOS,
                "Os dados informados conflitam com um registro existente.", excecao, requisicao);
    }

    // As operações de alteração reservam o registro; se a espera pela reserva se
    // esgota, ou o banco desfaz uma das transações para sair de um impasse, quem
    // chamou pode simplesmente tentar de novo.
    @ExceptionHandler(ConcurrencyFailureException.class)
    public ResponseEntity<Object> tratarFalhaDeConcorrencia(ConcurrencyFailureException excecao,
                                                            WebRequest requisicao) {
        LOGGER.warn("Falha de concorrência no banco: {}", excecao.getMostSpecificCause().getMessage());
        return responder(TipoDeProblema.CONFLITO_DE_DADOS,
                "Outra operação estava alterando o mesmo registro. Tente de novo.", excecao, requisicao);
    }

    // Qualquer outra falha, inclusive IllegalArgumentException fora do domínio, é
    // tratada como erro interno. O rastro fica só no log.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> tratarFalhaInesperada(Exception excecao, WebRequest requisicao) {
        LOGGER.error("Falha inesperada ao processar {}", requisicao.getDescription(false), excecao);
        return responder(TipoDeProblema.ERRO_INTERNO,
                "Ocorreu uma falha inesperada ao processar a requisição.", excecao, requisicao);
    }

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

    // Todas as respostas de erro passam por aqui. Os erros do próprio Spring chegam
    // sem type (nulo no Spring 7) e com título em inglês; recebem o tipo e o título
    // pelo status.
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
        problema.setProperty("momento", momento(LocalDateTime.now()));
    }

    static String momento(LocalDateTime agora) {
        return agora.format(FORMATO_DO_MOMENTO);
    }
}
