package br.com.fiap.restaurante.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Metadados da documentação OpenAPI.
 *
 * <p>Define o cabeçalho da interface Swagger. A descrição registra a política de
 * versionamento e o formato dos erros, para que quem abre a documentação entenda
 * as convenções sem consultar o relatório.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI restauranteOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Gestão de Restaurantes")
                        .version("v1")
                        .description("""
                                Sistema compartilhado de gestão para restaurantes - Fase 2.

                                Gerencia usuários e seus tipos, restaurantes com horários de \
                                funcionamento e os itens do cardápio de cada restaurante.

                                **Versionamento:** a versão faz parte da rota (/api/v1/...). \
                                Mudanças compatíveis, como um campo novo na resposta, ficam na \
                                versão atual. Só uma mudança que quebra quem já consome a API \
                                gera versão nova, como /api/v2/usuarios, cuja busca por nome \
                                devolve uma página em vez de uma lista. Recursos novos nascem em v1.

                                **Erros:** todas as respostas de erro seguem a RFC 9457 \
                                (ProblemDetail). O campo type aponta para /problemas/{identificador}, \
                                que descreve o problema.""")
                        .contact(new Contact()
                                .name("Thiago Wippel Chaves")
                                .email("thiagowippel@hotmail.com"))
                        .license(new License()
                                .name("Uso acadêmico - FIAP Pós Tech")));
    }
}
