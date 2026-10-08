package br.com.fiap.restaurante.infrastructure;

import br.com.fiap.restaurante.suporte.TesteDeIntegracao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

@TesteDeIntegracao
@DisplayName("Inicialização")
class InicializacaoIT {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private Environment ambiente;

    @Test
    @DisplayName("INF-01 · o contexto sobe contra o MySQL 8.4 do contêiner, com o Hibernate só validando o schema")
    void deveSubirContraOMysqlDoConteiner_ComHibernateEmModoValidate() throws SQLException {
        /* arrange */
        DatabaseMetaData banco;

        /* act */
        try (Connection conexao = dataSource.getConnection()) {
            banco = conexao.getMetaData();

            /* assert */
            assertThat(banco.getDatabaseProductName()).isEqualTo("MySQL");
            assertThat(banco.getDatabaseProductVersion()).startsWith("8.4");
        }
        assertThat(ambiente.getProperty("spring.jpa.hibernate.ddl-auto")).isEqualTo("validate");
    }
}
