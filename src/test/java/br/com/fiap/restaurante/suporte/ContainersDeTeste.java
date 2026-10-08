package br.com.fiap.restaurante.suporte;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.utility.MountableFile;

/**
 * MySQL descartável para os testes de integração.
 *
 * <p>Usa a mesma imagem do docker-compose e o mesmo script de schema, copiado
 * para a pasta que o MySQL executa na primeira inicialização. Assim os testes
 * rodam contra as mesmas tabelas e restrições da aplicação em contêiner.
 *
 * <p>O contêiner fica num campo estático para ser um só em toda a suíte: o
 * Spring reaproveita os contextos entre classes de teste, e quando um teste
 * precisa de contexto próprio, recebe o mesmo banco em vez de subir outro.
 * {@code @ServiceConnection} entrega ao Spring a URL, o usuário e a senha do
 * contêiner, dispensando qualquer propriedade de conexão.
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
