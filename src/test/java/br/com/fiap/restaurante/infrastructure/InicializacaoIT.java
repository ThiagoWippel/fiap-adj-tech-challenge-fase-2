package br.com.fiap.restaurante.infrastructure;

import br.com.fiap.restaurante.suporte.TesteDeIntegracao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;

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

    @Autowired
    private JdbcTemplate jdbc;

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

    @Test
    @DisplayName("INF-01 · o script de schema cria as tabelas que as entidades mapeiam")
    void deveCriarAsTabelasPeloScript() {
        /* act */
        var tabelas = jdbc.queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = DATABASE()", String.class);

        /* assert */
        assertThat(tabelas).contains("tipo_usuario", "usuario");
    }

    @Test
    @DisplayName("TIP-15 · uma base recém-criada já tem os tipos Cliente e Dono de Restaurante")
    void deveTerOsTiposDeSistema() {
        /* act */
        var tipos = jdbc.queryForList("SELECT CONCAT(id, ' ', codigo, ' ', nome) FROM tipo_usuario ORDER BY id", String.class);

        /* assert */
        assertThat(tipos).startsWith("1 CLIENTE Cliente", "2 DONO_RESTAURANTE Dono de Restaurante");
    }
}
