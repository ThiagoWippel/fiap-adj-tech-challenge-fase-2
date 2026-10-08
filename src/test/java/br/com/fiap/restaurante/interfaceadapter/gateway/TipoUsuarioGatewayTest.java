package br.com.fiap.restaurante.interfaceadapter.gateway;

import br.com.fiap.restaurante.domain.entity.TipoUsuario;
import br.com.fiap.restaurante.interfaceadapter.datasource.DadosTipoUsuario;
import br.com.fiap.restaurante.interfaceadapter.datasource.ITipoUsuarioDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@DisplayName("Gateway de tipos de usuário")
class TipoUsuarioGatewayTest {

    @Mock
    private ITipoUsuarioDataSource dataSource;

    private AutoCloseable mocks;

    @BeforeEach
    void preparar() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void encerrar() throws Exception {
        mocks.close();
    }

    @Test
    @DisplayName("TIP-05 · a busca por código devolve o tipo convertido, ou vazio")
    void deveBuscarPorCodigo() {
        /* arrange */
        when(dataSource.buscarPorCodigo("CLIENTE")).thenReturn(Optional.of(new DadosTipoUsuario(1L, "Cliente", "CLIENTE")));
        TipoUsuarioGateway gateway = TipoUsuarioGateway.create(dataSource);

        /* act */
        Optional<TipoUsuario> cliente = gateway.buscarPorCodigo("CLIENTE");

        /* assert */
        assertThat(cliente).get().extracting(TipoUsuario::getId, TipoUsuario::getNome, TipoUsuario::getCodigo)
                .containsExactly(1L, "Cliente", "CLIENTE");
        assertThat(gateway.buscarPorCodigo("ENTREGADOR")).isEmpty();
    }
}
