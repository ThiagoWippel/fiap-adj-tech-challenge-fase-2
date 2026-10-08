package br.com.fiap.restaurante.infrastructure.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * Chave e validade do token emitido no login. Sem a chave a aplicação não sobe.
 *
 * @param chave    segredo do HMAC-SHA256, vindo da variável de ambiente JWT_CHAVE
 * @param validade tempo até o token expirar, como {@code 1h}
 */
@Validated
@ConfigurationProperties("restaurante.token")
public record TokenProperties(
        @NotBlank(message = "Defina a variável de ambiente JWT_CHAVE com a chave de assinatura do token.")
        @Size(min = 32, message = "A chave do token (variável JWT_CHAVE) deve ter ao menos 32 caracteres.")
        String chave,
        @NotNull Duration validade) {
}
