package br.com.fiap.restaurante.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Metadados da documentação OpenAPI.
 *
 * Define o cabeçalho exibido no topo da interface Swagger. A descrição registra
 * a estratégia de versionamento, de modo que quem abre a documentação entende a
 * convenção de rotas sem precisar consultar o relatório.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI restauranteOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Gestao de Usuarios - Restaurantes")
                        .version("v1")
                        .description("""
                                Sistema compartilhado de gestao para restaurantes - Fase 1.

                                Contempla o cadastro de usuarios em dois tipos (cliente e dono de \
                                restaurante), atualizacao de dados, troca de senha em endpoint \
                                proprio, busca por nome e validacao de credenciais.

                                **Versionamento:** a versao integra o caminho da rota \
                                (/api/v1/...). Alteracoes compativeis - como a inclusao de um \
                                campo na resposta - permanecem na versao corrente. Apenas \
                                mudancas que quebram consumidores existentes originam uma nova \
                                versao, como ocorre em /api/v2/usuarios, cuja busca por nome \
                                devolve um objeto paginado no lugar de uma lista.

                                **Erros:** todas as respostas de erro seguem a RFC 7807 \
                                (ProblemDetail).""")
                        .contact(new Contact()
                                .name("Thiago Wippel Chaves")
                                .email("thiagowippel@hotmail.com"))
                        .license(new License()
                                .name("Uso academico - FIAP Pos Tech")));
    }
}
