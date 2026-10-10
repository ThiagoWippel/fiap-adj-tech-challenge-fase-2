package br.com.fiap.restaurante.infrastructure.config;

import br.com.fiap.restaurante.infrastructure.api.handler.ValvulaDeErrosDoTomcat;
import org.apache.catalina.Pipeline;
import org.apache.catalina.Valve;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.valves.ErrorReportValve;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * Troca a página de erro HTML do Tomcat pela {@link ValvulaDeErrosDoTomcat}. Roda
 * depois da configuração do Spring Boot, que instala a válvula padrão, para
 * substituí-la.
 */
@Configuration
public class TomcatConfig {

    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> errosDoTomcatEmProblemDetail(
            ProblemasProperties propriedades) {
        return fabrica -> fabrica.addContextCustomizers(contexto -> {
            StandardHost host = (StandardHost) contexto.getParent();
            Pipeline pipeline = host.getPipeline();
            for (Valve valvula : pipeline.getValves()) {
                if (valvula instanceof ErrorReportValve) {
                    pipeline.removeValve(valvula);
                }
            }
            pipeline.addValve(new ValvulaDeErrosDoTomcat(propriedades.baseUri()));
            // Sem isto o Tomcat, ao iniciar, acrescentaria de novo a válvula padrão
            host.setErrorReportValveClass(ValvulaDeErrosDoTomcat.class.getName());
        });
    }
}
