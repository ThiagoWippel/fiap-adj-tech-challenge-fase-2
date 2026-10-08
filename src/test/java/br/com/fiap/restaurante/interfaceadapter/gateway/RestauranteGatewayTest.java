package br.com.fiap.restaurante.interfaceadapter.gateway;

import br.com.fiap.restaurante.application.dto.Pagina;
import br.com.fiap.restaurante.application.dto.PedidoDePagina;
import br.com.fiap.restaurante.domain.entity.Restaurante;
import br.com.fiap.restaurante.domain.enums.TipoCozinha;
import br.com.fiap.restaurante.domain.valueobject.Turno;
import br.com.fiap.restaurante.interfaceadapter.datasource.DadosRestaurante;
import br.com.fiap.restaurante.interfaceadapter.datasource.DadosTurno;
import br.com.fiap.restaurante.interfaceadapter.datasource.IRestauranteDataSource;
import br.com.fiap.restaurante.suporte.DadosDeExemplo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static br.com.fiap.restaurante.suporte.Exemplos.cantina;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Gateway de restaurantes")
class RestauranteGatewayTest {

    private static final PedidoDePagina PEDIDO = new PedidoDePagina(0, 10, List.of());

    @Mock
    private IRestauranteDataSource dataSource;

    private AutoCloseable mocks;
    private RestauranteGateway gateway;

    @BeforeEach
    void preparar() {
        mocks = MockitoAnnotations.openMocks(this);
        gateway = RestauranteGateway.create(dataSource);
    }

    @AfterEach
    void encerrar() throws Exception {
        mocks.close();
    }

    @Test
    @DisplayName("RES-01 · incluir converte o restaurante em dados, com o dono e os turnos, e reconstrói a entidade")
    void deveConverterNosDoisSentidos() {
        /* arrange */
        when(dataSource.incluir(any())).thenReturn(DadosDeExemplo.cantina());

        /* act */
        Restaurante incluido = gateway.incluir(cantina());

        /* assert */
        ArgumentCaptor<DadosRestaurante> enviados = ArgumentCaptor.forClass(DadosRestaurante.class);
        verify(dataSource).incluir(enviados.capture());
        assertThat(enviados.getValue().tipoCozinha()).isEqualTo("ITALIANA");
        assertThat(enviados.getValue().dono().id()).isEqualTo(8L);
        assertThat(enviados.getValue().horarios()).extracting(DadosTurno::diaSemana).containsExactly("SEGUNDA", "SEXTA");
        assertThat(incluido.getTipoCozinha()).isEqualTo(TipoCozinha.ITALIANA);
        assertThat(incluido.getHorarios().turnos()).extracting(Turno::toString)
                .containsExactly("SEGUNDA 11:00–15:00", "SEXTA 18:00–02:00");
        assertThat(incluido.getDono().ehDonoDeRestaurante()).isTrue();
    }

    @Test
    @DisplayName("RES-08 · atualizar e buscar por id convertem o restaurante")
    void deveAtualizarEBuscar() {
        /* arrange */
        when(dataSource.atualizar(any())).thenReturn(DadosDeExemplo.cantina());
        when(dataSource.buscarPorId(9L)).thenReturn(Optional.of(DadosDeExemplo.cantina()));

        /* act + assert */
        assertThat(gateway.atualizar(cantina()).getId()).isEqualTo(9L);
        assertThat(gateway.buscarPorId(9L)).get().extracting(Restaurante::getNome).isEqualTo("Cantina da Nona");
        assertThat(gateway.buscarPorId(99L)).isEmpty();
    }

    @Test
    @DisplayName("RES-13 · RES-17 · listagens convertem a página; o tipo de cozinha vai como texto, ou nulo sem filtro")
    void deveListar() {
        /* arrange */
        Pagina<DadosRestaurante> pagina = new Pagina<>(List.of(DadosDeExemplo.cantina()), 0, 10, 1, 1);
        when(dataSource.listar("nona", "ITALIANA", PEDIDO)).thenReturn(pagina);
        when(dataSource.listar("", null, PEDIDO)).thenReturn(pagina);
        when(dataSource.buscarPorDono(8L, PEDIDO)).thenReturn(pagina);

        /* act + assert */
        assertThat(gateway.listar("nona", TipoCozinha.ITALIANA, PEDIDO).conteudo()).hasSize(1);
        assertThat(gateway.listar("", null, PEDIDO).conteudo()).hasSize(1);
        assertThat(gateway.buscarPorDono(8L, PEDIDO).conteudo()).extracting(Restaurante::getId).containsExactly(9L);
    }

    @Test
    @DisplayName("EXC-07 · ITE-22 · a contagem por dono, a verificação de ativo e a remoção são repassadas")
    void deveRepassarContagemERemocao() {
        /* arrange */
        when(dataSource.contarAtivosPorDono(8L)).thenReturn(2L);
        when(dataSource.existeAtivo(9L)).thenReturn(true);

        /* act */
        gateway.remover(9L);

        /* assert */
        assertThat(gateway.contarAtivosPorDono(8L)).isEqualTo(2L);
        assertThat(gateway.existeAtivo(9L)).isTrue();
        assertThat(gateway.existeAtivo(99L)).isFalse();
        verify(dataSource).remover(9L);
    }
}
