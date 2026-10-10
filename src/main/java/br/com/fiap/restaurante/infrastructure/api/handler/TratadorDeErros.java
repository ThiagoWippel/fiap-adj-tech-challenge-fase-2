package br.com.fiap.restaurante.infrastructure.api.handler;

import br.com.fiap.restaurante.application.exception.ConflitoDeDadosException;
import br.com.fiap.restaurante.application.exception.CredenciaisInvalidasException;
import br.com.fiap.restaurante.application.exception.RecursoNaoEncontradoException;
import br.com.fiap.restaurante.domain.exception.RegraDeNegocioException;
import br.com.fiap.restaurante.domain.exception.ValidacaoDeDominioException;
import br.com.fiap.restaurante.infrastructure.api.problema.TipoDeProblema;
import br.com.fiap.restaurante.infrastructure.config.ProblemasProperties;
import org.apache.tomcat.util.http.InvalidParameterException;
import org.hibernate.exception.ConstraintViolationException;
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
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.exc.MismatchedInputException;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    // Nome da restrição nas mensagens do MySQL: chave única, chave estrangeira e CHECK
    private static final Pattern RESTRICAO_NA_MENSAGEM =
            Pattern.compile("for key '([^']+)'|CONSTRAINT `([^`]+)`|[Cc]heck constraint '([^']+)'");

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
    // dos casos de uso; nesse caso é a restrição do banco que barra a segunda. O log
    // cita só a restrição: a mensagem do banco traz o valor repetido, que pode ser um
    // e-mail ou um documento.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> tratarViolacaoDeIntegridade(DataIntegrityViolationException excecao,
                                                              WebRequest requisicao) {
        LOGGER.warn("Violação de integridade no banco: restrição {}", restricaoViolada(excecao));
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

    // O Tomcat só decodifica a query quando um parâmetro é lido; com um % inválido
    // (?nome=%zz), a leitura lança esta exceção, que sem tratamento viraria 500.
    @ExceptionHandler(InvalidParameterException.class)
    public ResponseEntity<Object> tratarParametroMalCodificado(InvalidParameterException excecao,
                                                               WebRequest requisicao) {
        return responder(TipoDeProblema.REQUISICAO_INVALIDA,
                "Um parâmetro da URL tem codificação inválida. Confira os caracteres com %.", excecao, requisicao);
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

    // A mensagem padrão usa o caminho já normalizado: "/api/v1/tipos-usuario/" virava
    // "não existe /api/v1/tipos-usuario", justamente a rota que existe.
    @Override
    protected ResponseEntity<Object> handleNoResourceFoundException(NoResourceFoundException excecao,
                                                                    HttpHeaders cabecalhos,
                                                                    HttpStatusCode status,
                                                                    WebRequest requisicao) {
        String caminho = ((ServletWebRequest) requisicao).getRequest().getRequestURI();
        ProblemDetail problema = problemaDe(TipoDeProblema.RECURSO_NAO_ENCONTRADO,
                "Não existe recurso no caminho " + caminho + ".");
        return handleExceptionInternal(excecao, problema, cabecalhos, status, requisicao);
    }

    // Um valor com o tipo errado ("preco": "abc") chega como corpo ilegível. O Jackson
    // sabe em que campo parou, e a resposta aponta esse campo, como na validação.
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException excecao,
                                                                  HttpHeaders cabecalhos,
                                                                  HttpStatusCode status,
                                                                  WebRequest requisicao) {
        if (!(excecao.getCause() instanceof MismatchedInputException tipoErrado) || tipoErrado.getPath().isEmpty()) {
            return super.handleHttpMessageNotReadable(excecao, cabecalhos, status, requisicao);
        }
        String campo = caminhoDoCampo(tipoErrado.getPath());
        ProblemDetail problema = problemaDe(TipoDeProblema.REQUISICAO_INVALIDA,
                "O campo " + campo + " tem um valor do tipo errado.");
        problema.setProperty("erros", List.of(new ErroDeCampo(campo, valorEsperado(tipoErrado.getTargetType()))));
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

    static String restricaoViolada(DataIntegrityViolationException excecao) {
        for (Throwable causa = excecao; causa != null; causa = causa.getCause()) {
            if (causa instanceof ConstraintViolationException violacao && violacao.getConstraintName() != null) {
                return violacao.getConstraintName();
            }
        }
        Matcher restricao = RESTRICAO_NA_MENSAGEM.matcher(String.valueOf(excecao.getMostSpecificCause().getMessage()));
        if (!restricao.find()) {
            return "não identificada";
        }
        // Só um dos três grupos casa
        return restricao.group(1) != null ? restricao.group(1)
                : restricao.group(2) != null ? restricao.group(2) : restricao.group(3);
    }

    // "endereco" > "cep" vira endereco.cep; um índice de lista vira [0]
    static String caminhoDoCampo(List<JacksonException.Reference> caminho) {
        StringBuilder campo = new StringBuilder();
        for (JacksonException.Reference parte : caminho) {
            if (parte.getPropertyName() != null) {
                campo.append(campo.isEmpty() ? "" : ".").append(parte.getPropertyName());
            } else if (parte.getIndex() >= 0) {
                campo.append('[').append(parte.getIndex()).append(']');
            }
        }
        return campo.toString();
    }

    static String valorEsperado(Class<?> tipo) {
        if (tipo == null) {
            return "O valor tem o tipo errado.";
        }
        if (CharSequence.class.isAssignableFrom(tipo)) {
            return "Informe um texto.";
        }
        if (tipo == Integer.class || tipo == Long.class || tipo == int.class || tipo == long.class) {
            return "Informe um número inteiro.";
        }
        if (Number.class.isAssignableFrom(tipo)) {
            return "Informe um número.";
        }
        if (tipo == Boolean.class || tipo == boolean.class) {
            return "Informe true ou false.";
        }
        if (Collection.class.isAssignableFrom(tipo) || tipo.isArray()) {
            return "Informe uma lista.";
        }
        return "Informe um objeto.";
    }
}
