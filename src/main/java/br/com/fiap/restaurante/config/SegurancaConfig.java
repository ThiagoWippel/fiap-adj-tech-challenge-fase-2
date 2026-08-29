package br.com.fiap.restaurante.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Codificação de senhas.
 *
 * Usa apenas spring-security-crypto, a biblioteca de criptografia isolada, sem
 * a cadeia de filtros do Spring Security completo - que o enunciado dispensa
 * explicitamente.
 *
 * O BCrypt é unidirecional: o valor armazenado não pode ser revertido. A
 * verificação compara hashes, nunca textos. O algoritmo incorpora um valor
 * aleatório por registro, de modo que senhas idênticas produzem hashes
 * diferentes - impedindo que senhas repetidas sejam identificadas por
 * inspeção do banco.
 *
 * O tipo devolvido é a interface PasswordEncoder, e não a implementação. Os
 * serviços passam a depender da abstração, o que atende ao Princípio da
 * Inversão de Dependência e permite substituir o algoritmo alterando apenas
 * esta classe.
 */
@Configuration
public class SegurancaConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
