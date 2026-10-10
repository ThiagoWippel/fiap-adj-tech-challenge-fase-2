package br.com.fiap.restaurante.infrastructure.api.handler;

import br.com.fiap.restaurante.infrastructure.api.problema.TipoDeProblema;
import br.com.fiap.restaurante.infrastructure.config.ProblemasProperties;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.LocalDateTime;

/**
 * Responde em ProblemDetail os erros que o servidor encaminha para /error sem passar
 * pelos controllers: um TRACE recusado pelo Tomcat, um corpo grande demais barrado
 * pelo filtro ou o próprio /error acessado direto. Substitui o controller padrão do
 * Spring Boot, que responderia em outro formato.
 */
@Hidden
@RestController
public class ErroDoServidorController implements ErrorController {

    private final ProblemasProperties propriedades;

    public ErroDoServidorController(ProblemasProperties propriedades) {
        this.propriedades = propriedades;
    }

    @RequestMapping("${server.error.path:/error}")
    public ResponseEntity<ProblemDetail> responder(HttpServletRequest requisicao) {
        // Sem código de erro, o /error foi pedido diretamente: para quem chamou, a rota não existe
        int status = requisicao.getAttribute(RequestDispatcher.ERROR_STATUS_CODE) instanceof Integer codigo
                ? codigo : 404;
        String caminho = requisicao.getAttribute(RequestDispatcher.ERROR_REQUEST_URI) instanceof String original
                ? original : requisicao.getRequestURI();

        TipoDeProblema tipo = TipoDeProblema.paraErroDoFramework(status);
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(status),
                detalhe(status, caminho));
        problema.setType(tipo.uri(propriedades.baseUri()));
        problema.setTitle(tipo.titulo());
        problema.setInstance(URI.create(caminho));
        problema.setProperty("momento", TratadorDeErros.momento(LocalDateTime.now()));
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(problema);
    }

    // Na reentrada pelo /error o Tomcat troca o método para GET; o 405 não pode citá-lo
    static String detalhe(int status, String caminho) {
        return switch (status) {
            case 400 -> "A requisição está malformada e não pôde ser interpretada.";
            case 404 -> "Não existe recurso no caminho " + caminho + ".";
            case 405 -> "O método da requisição não é suportado nesta rota.";
            case 413 -> "O corpo da requisição passa do limite de 1 MB.";
            default -> status >= 500
                    ? "Ocorreu uma falha inesperada ao processar a requisição."
                    : "A requisição não pôde ser atendida.";
        };
    }
}
