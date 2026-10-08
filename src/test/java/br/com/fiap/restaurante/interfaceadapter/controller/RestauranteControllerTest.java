package br.com.fiap.restaurante.interfaceadapter.controller;

import br.com.fiap.restaurante.application.dto.AtualizacaoDeRestauranteDTO;
import br.com.fiap.restaurante.application.dto.FiltroDeRestaurantes;
import br.com.fiap.restaurante.application.dto.NovoRestauranteDTO;
import br.com.fiap.restaurante.application.dto.Pagina;
import br.com.fiap.restaurante.application.dto.PedidoDePagina;
import br.com.fiap.restaurante.interfaceadapter.datasource.IRestauranteDataSource;
import br.com.fiap.restaurante.interfaceadapter.datasource.IUsuarioDataSource;
import br.com.fiap.restaurante.interfaceadapter.presenter.PaginaResponse;
import br.com.fiap.restaurante.interfaceadapter.presenter.RestauranteResponse;
import br.com.fiap.restaurante.suporte.DadosDeExemplo;
import br.com.fiap.restaurante.suporte.TransacaoImediata;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static br.com.fiap.restaurante.suporte.Exemplos.enderecoDTO;
import static br.com.fiap.restaurante.suporte.Exemplos.turnosDTO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Controller de restaurantes")
class RestauranteControllerTest {

    private static final PedidoDePagina PEDIDO = new PedidoDePagina(0, 10, List.of());

    @Mock
    private IRestauranteDataSource restaurantes;

    @Mock
    private IUsuarioDataSource usuarios;

    private AutoCloseable mocks;
    private RestauranteController controller;

    @BeforeEach
    void preparar() {
        mocks = MockitoAnnotations.openMocks(this);
        controller = RestauranteController.create(restaurantes, usuarios, new TransacaoImediata());
        when(usuarios.buscarPorId(8L)).thenReturn(Optional.of(DadosDeExemplo.ana()));
        when(restaurantes.buscarPorId(9L)).thenReturn(Optional.of(DadosDeExemplo.cantina()));
    }

    @AfterEach
    void encerrar() throws Exception {
        mocks.close();
    }

    @Test
    @DisplayName("RES-12 · cadastrar devolve o restaurante com o id e o dono")
    void deveCadastrar() {
        /* arrange */
        when(restaurantes.incluir(any())).thenReturn(DadosDeExemplo.cantina());

        /* act */
        RestauranteResponse resposta = controller.cadastrar(
                new NovoRestauranteDTO("Cantina da Nona", enderecoDTO(), "ITALIANA", turnosDTO(), 8L));

        /* assert */
        assertThat(resposta.id()).isEqualTo(9L);
        assertThat(resposta.dono().nome()).isEqualTo("Ana Souza");
    }

    @Test
    @DisplayName("RES-14 · RES-13 · buscar por id e listar devolvem as respostas")
    void deveConsultar() {
        /* arrange */
        when(restaurantes.listar("", null, PEDIDO))
                .thenReturn(new Pagina<>(List.of(DadosDeExemplo.cantina()), 0, 10, 1, 1));

        /* act */
        RestauranteResponse porId = controller.buscarPorId(9L);
        PaginaResponse<RestauranteResponse> pagina = controller.listar(new FiltroDeRestaurantes(null, null), PEDIDO);

        /* assert */
        assertThat(porId.nome()).isEqualTo("Cantina da Nona");
        assertThat(pagina.conteudo()).hasSize(1);
    }

    @Test
    @DisplayName("RES-08 · atualizar devolve o restaurante gravado")
    void deveAtualizar() {
        /* arrange */
        when(restaurantes.atualizar(any())).thenAnswer(chamada -> chamada.getArgument(0));

        /* act */
        RestauranteResponse resposta = controller.atualizar(new AtualizacaoDeRestauranteDTO(9L, "Pizzaria da Nona",
                enderecoDTO(), "PIZZARIA", turnosDTO(), 8L));

        /* assert */
        assertThat(resposta.nome()).isEqualTo("Pizzaria da Nona");
        assertThat(resposta.tipoCozinha()).isEqualTo("PIZZARIA");
    }

    @Test
    @DisplayName("RES-11 · excluir remove o restaurante logicamente")
    void deveExcluir() {
        /* act */
        controller.excluir(9L);

        /* assert */
        verify(restaurantes).remover(9L);
    }
}
