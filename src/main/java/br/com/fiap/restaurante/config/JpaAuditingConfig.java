package br.com.fiap.restaurante.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Habilita a auditoria automática de datas do Spring Data JPA.
 *
 * Sem esta configuração, as anotações @CreatedDate e @LastModifiedDate nas
 * entidades são simplesmente ignoradas - sem erro, sem aviso. A data ficaria
 * nula e a inserção falharia por violação de NOT NULL.
 *
 * Atende ao requisito de "registro da data da última alteração" sem exigir
 * que cada operação de escrita lembre de atualizar o campo manualmente.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
