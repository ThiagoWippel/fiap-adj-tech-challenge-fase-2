package br.com.fiap.restaurante.suporte;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.utility.MountableFile;

/**
 * MySQL dos testes de integração: mesma imagem e mesmo script de schema do
 * docker-compose. O contêiner é estático para ser um só em toda a suíte.
 */
@TestConfiguration(proxyBeanMethods = false)
public class ContainersDeTeste {

    private static final MySQLContainer MYSQL = new MySQLContainer("mysql:8.4")
            .withCopyFileToContainer(
                    MountableFile.forHostPath("docker/mysql/init/"),
                    "/docker-entrypoint-initdb.d/");

    @Bean
    @ServiceConnection
    MySQLContainer mysql() {
        return MYSQL;
    }
}
