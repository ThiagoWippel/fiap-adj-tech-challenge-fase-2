package br.com.fiap.restaurante.infrastructure.api.handler;

import br.com.fiap.restaurante.infrastructure.api.problema.TipoDeProblema;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ErrorReportValve;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Responde em ProblemDetail os erros que o Tomcat produz antes de a requisição
 * chegar ao Spring, como uma URL com codificação inválida no caminho ou um cabeçalho
 * grande demais. Sem ela, esses erros voltariam como página HTML.
 */
public class ValvulaDeErrosDoTomcat extends ErrorReportValve {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final URI baseDosTipos;

    public ValvulaDeErrosDoTomcat(URI baseDosTipos) {
        this.baseDosTipos = baseDosTipos;
    }

    @Override
    protected void report(Request requisicao, Response resposta, Throwable erro) {
        int status = resposta.getStatus();
        // Erro que a aplicação já respondeu (com corpo) passa direto
        if (status < 400 || resposta.getContentWritten() > 0 || !resposta.setErrorReported()) {
            return;
        }
        String caminho = requisicao.getRequestURI();
        TipoDeProblema tipo = TipoDeProblema.paraErroDoFramework(status);
        Map<String, Object> problema = new LinkedHashMap<>();
        problema.put("type", tipo.uri(baseDosTipos).toString());
        problema.put("title", tipo.titulo());
        problema.put("status", status);
        problema.put("detail", ErroDoServidorController.detalhe(status, caminho));
        problema.put("instance", caminho);
        problema.put("momento", TratadorDeErros.momento(LocalDateTime.now()));
        try {
            resposta.setContentType("application/problem+json");
            resposta.setCharacterEncoding("UTF-8");
            PrintWriter saida = resposta.getReporter();
            if (saida != null) {
                saida.write(JSON.writeValueAsString(problema));
                saida.flush();
            }
        } catch (IOException | IllegalStateException e) {
            // A conexão já não aceita corpo; fica só o status
        }
    }
}
