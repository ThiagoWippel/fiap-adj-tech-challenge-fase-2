package br.com.fiap.restaurante.infrastructure.persistence.datasource;

import br.com.fiap.restaurante.application.port.ITransactionManager;
import br.com.fiap.restaurante.interfaceadapter.datasource.DadosItemCardapio;
import br.com.fiap.restaurante.interfaceadapter.datasource.IItemCardapioDataSource;
import br.com.fiap.restaurante.suporte.ApiDeTeste;
import br.com.fiap.restaurante.suporte.LimpezaDoBanco;
import br.com.fiap.restaurante.suporte.TesteDeIntegracao;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;

import static br.com.fiap.restaurante.suporte.ApiDeTeste.dono;
import static br.com.fiap.restaurante.suporte.ApiDeTeste.restaurante;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Origem de dados de itens contra o MySQL: a unicidade do nome entre os itens
 * ativos, garantida pela coluna gerada ativo_marcador.
 */
@TesteDeIntegracao
@DisplayName("Origem de dados JPA de itens do cardápio")
class JpaItemCardapioDataSourceIT {

    @LocalServerPort
    private int porta;

    @Autowired
    private IItemCardapioDataSource itens;

    @Autowired
    private ITransactionManager transacao;

    @Autowired
    private JdbcTemplate jdbc;

    private long cantina;

    @BeforeEach
    void preparar() {
        LimpezaDoBanco.limpar(jdbc);
        ApiDeTeste api = new ApiDeTeste(porta);
        long ana = api.cadastrar(dono("Ana Souza", "ana@exemplo.com", "ana.souza", "11222333000181"));
        cantina = api.cadastrarRestaurante(restaurante("Cantina da Nona", "ITALIANA", ana, "SEGUNDA 11:00 15:00"));
    }

    @AfterEach
    void restaurar() {
        LimpezaDoBanco.limpar(jdbc);
    }

    @Test
    @DisplayName("ITE-20 · o banco recusa dois itens ativos com o mesmo nome no restaurante, mesmo sem o caso de uso")
    void deveBarrarNomeRepetidoNoBanco() {
        /* arrange */
        incluir("Lasanha");

        /* act + assert */
        assertThatThrownBy(() -> incluir("LASANHA "))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("ITE-19 · ITE-20 · depois de removido, o nome fica livre: o marcador do item removido é nulo")
    void deveLiberarONomeDoItemRemovido() {
        /* arrange */
        Long removido = incluir("Lasanha").id();
        transacao.executar(() -> itens.remover(removido));

        /* act */
        DadosItemCardapio novo = incluir("Lasanha");

        /* assert */
        assertThat(novo.id()).isNotEqualTo(removido);
        assertThat(jdbc.queryForObject("SELECT ativo_marcador FROM item_cardapio WHERE id = ?", Integer.class, removido))
                .isNull();
        assertThat(jdbc.queryForObject("SELECT ativo_marcador FROM item_cardapio WHERE id = ?", Integer.class, novo.id()))
                .isEqualTo(1);
        assertThat(itens.existeNomeAtivo(cantina, "lasanha")).isTrue();
        assertThat(itens.existeNomeAtivoEmOutroItem(cantina, "lasanha", novo.id())).isFalse();
    }

    private DadosItemCardapio incluir(String nome) {
        return transacao.executar(() -> itens.incluir(new DadosItemCardapio(null, cantina, nome, "Prato da casa.",
                new BigDecimal("48.00"), true, "fotos/lasanha.jpg", null, null)));
    }
}
