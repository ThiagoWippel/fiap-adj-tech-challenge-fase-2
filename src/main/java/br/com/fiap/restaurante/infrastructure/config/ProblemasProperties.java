package br.com.fiap.restaurante.infrastructure.config;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.net.URI;

/**
 * Configuração dos identificadores de tipo de problema das respostas de erro.
 *
 * <p>O campo {@code type} do ProblemDetail é montado como
 * {@code {baseUri}/problemas/{identificador}}. A RFC 9457 recomenda que ele seja
 * absoluto, e a própria aplicação responde nesse endereço com a descrição do
 * problema. Sem a propriedade, a aplicação não sobe.
 *
 * @param baseUri endereço público da aplicação, como {@code http://localhost:8080}
 */
@Validated
@ConfigurationProperties("restaurante.problemas")
public record ProblemasProperties(@NotNull URI baseUri) {
}
