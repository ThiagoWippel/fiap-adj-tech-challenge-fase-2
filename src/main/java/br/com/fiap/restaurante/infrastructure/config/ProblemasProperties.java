package br.com.fiap.restaurante.infrastructure.config;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.net.URI;

/**
 * Base do campo {@code type} das respostas de erro:
 * {@code {baseUri}/problemas/{identificador}}. Sem ela a aplicação não sobe.
 *
 * @param baseUri endereço da aplicação, como {@code http://localhost:8080}
 */
@Validated
@ConfigurationProperties("restaurante.problemas")
public record ProblemasProperties(@NotNull URI baseUri) {
}
