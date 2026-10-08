package br.com.fiap.restaurante.infrastructure.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A aplicação não sobe sem a chave do token. No compose a chave vem da variável
 * JWT_CHAVE; sem ela, a propriedade chega vazia.
 */
@DisplayName("Configuração do token")
class ConfiguracaoDoTokenIT {

    private final ApplicationContextRunner contexto = new ApplicationContextRunner()
            .withUserConfiguration(ComToken.class)
            .withPropertyValues("restaurante.token.validade=1h");

    @Test
    @DisplayName("INF-06 · sem a chave, a inicialização falha com uma mensagem que nomeia a variável JWT_CHAVE")
    void deveFalharSemAChave() {
        contexto.withPropertyValues("restaurante.token.chave=").run(aplicacao -> {
            assertThat(aplicacao).hasFailed();
            assertThat(aplicacao.getStartupFailure()).rootCause().hasMessageContaining("JWT_CHAVE");
        });
    }

    @Test
    @DisplayName("INF-06 · uma chave com menos de 32 caracteres também impede a inicialização")
    void deveFalharComChaveCurta() {
        contexto.withPropertyValues("restaurante.token.chave=curta-demais").run(aplicacao -> {
            assertThat(aplicacao).hasFailed();
            assertThat(aplicacao.getStartupFailure()).rootCause().hasMessageContaining("ao menos 32 caracteres");
        });
    }

    @Test
    @DisplayName("INF-06 · com chave e validade, as propriedades são carregadas")
    void deveCarregarAsPropriedades() {
        contexto.withPropertyValues("restaurante.token.chave=chave-de-teste-com-pelo-menos-32-caracteres")
                .run(aplicacao -> assertThat(aplicacao.getBean(TokenProperties.class).validade())
                        .isEqualTo(Duration.ofHours(1)));
    }

    @EnableConfigurationProperties(TokenProperties.class)
    static class ComToken {
    }
}
