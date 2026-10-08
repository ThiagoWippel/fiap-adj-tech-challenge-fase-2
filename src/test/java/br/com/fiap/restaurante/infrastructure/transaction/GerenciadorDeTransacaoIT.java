package br.com.fiap.restaurante.infrastructure.transaction;

import br.com.fiap.restaurante.application.port.ITransactionManager;
import br.com.fiap.restaurante.suporte.TesteDeIntegracao;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@TesteDeIntegracao
@DisplayName("Gerenciador de transação")
class GerenciadorDeTransacaoIT {

    @Autowired
    private ITransactionManager transacao;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void criarTabelaDeTeste() {
        jdbc.execute("CREATE TABLE IF NOT EXISTS teste_transacao (id INT PRIMARY KEY) ENGINE = InnoDB");
        jdbc.execute("DELETE FROM teste_transacao");
    }

    @AfterEach
    void apagarTabelaDeTeste() {
        jdbc.execute("DROP TABLE IF EXISTS teste_transacao");
    }

    @Test
    @DisplayName("INF-08 · desfaz tudo o que foi gravado quando a operação lança exceção")
    void deveDesfazerAsGravacoes_QuandoAOperacaoFalhar() {
        /* act */
        assertThatThrownBy(() -> transacao.executar(() -> {
            jdbc.update("INSERT INTO teste_transacao (id) VALUES (1)");
            jdbc.update("INSERT INTO teste_transacao (id) VALUES (2)");
            throw new IllegalStateException("falha depois de duas gravações");
        })).isInstanceOf(IllegalStateException.class);

        /* assert */
        assertThat(contarRegistros()).isZero();
    }

    @Test
    @DisplayName("INF-08 · confirma as gravações e devolve o resultado quando a operação termina sem erro")
    void deveConfirmarAsGravacoes_QuandoAOperacaoTerminarSemErro() {
        /* act */
        Integer linhasGravadas = transacao.executar(() -> jdbc.update("INSERT INTO teste_transacao (id) VALUES (1)"));

        /* assert */
        assertThat(linhasGravadas).isEqualTo(1);
        assertThat(contarRegistros()).isEqualTo(1);
    }

    private int contarRegistros() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM teste_transacao", Integer.class);
    }
}
