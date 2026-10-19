package br.com.fiap.restaurante.suporte;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.mysql.MySQLContainer;

/**
 * MySQL dos testes de integração: mesma imagem do docker-compose. A estrutura vem
 * das migrações do Flyway, aplicadas ao subir o contexto, como na aplicação. O
 * contêiner é estático para ser um só em toda a suíte.
 */
@TestConfiguration(proxyBeanMethods = false)
public class ContainersDeTeste {

    private static final MySQLContainer MYSQL = new MySQLContainer("mysql:8.4");

    @Bean
    @ServiceConnection
    MySQLContainer mysql() {
        return MYSQL;
    }
}
