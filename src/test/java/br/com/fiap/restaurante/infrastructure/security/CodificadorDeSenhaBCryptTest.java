package br.com.fiap.restaurante.infrastructure.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Codificador de senha")
class CodificadorDeSenhaBCryptTest {

    private final CodificadorDeSenhaBCrypt codificador = new CodificadorDeSenhaBCrypt();

    @Test
    @DisplayName("INF-09 · a senha codificada é um hash BCrypt, nunca o texto original")
    void deveGerarHashBCrypt() {
        /* act */
        String hash = codificador.codificar("SenhaSegura123");

        /* assert */
        assertThat(hash).startsWith("$2a$").hasSize(60).doesNotContain("SenhaSegura123");
    }

    @Test
    @DisplayName("INF-09 · a mesma senha gera hashes diferentes a cada codificação")
    void deveGerarHashesDiferentesParaAMesmaSenha() {
        /* act */
        String primeiro = codificador.codificar("SenhaSegura123");
        String segundo = codificador.codificar("SenhaSegura123");

        /* assert */
        assertThat(primeiro).isNotEqualTo(segundo);
    }

    @Test
    @DisplayName("INF-09 · confere a senha correta e recusa a errada")
    void deveConferirASenhaCorretaERecusarAErrada() {
        /* arrange */
        String hash = codificador.codificar("SenhaSegura123");

        /* act + assert */
        assertThat(codificador.confere("SenhaSegura123", hash)).isTrue();
        assertThat(codificador.confere("SenhaErrada123", hash)).isFalse();
    }

    @Test
    @DisplayName("LOG-03 · sem hash para comparar (login inexistente), faz a comparação de descarte e recusa")
    void deveRecusarQuandoNaoHouverHash() {
        /* act + assert */
        assertThat(codificador.confere("SenhaSegura123", null)).isFalse();
    }
}
