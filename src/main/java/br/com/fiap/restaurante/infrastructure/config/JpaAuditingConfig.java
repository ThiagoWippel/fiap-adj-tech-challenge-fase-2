package br.com.fiap.restaurante.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Liga a auditoria do Spring Data, que preenche as datas de criação e de última
 * alteração. Fica fora da classe principal porque testes como o
 * {@code @WebMvcTest} não sobem o JPA, e a anotação ali quebraria esses testes.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
