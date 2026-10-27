package br.com.fiap.restaurante.infrastructure.api.restaurante;

import br.com.fiap.restaurante.application.dto.Pagina;
import br.com.fiap.restaurante.application.dto.PedidoDePagina;
import br.com.fiap.restaurante.infrastructure.api.comum.EnderecoRequest;
import br.com.fiap.restaurante.interfaceadapter.datasource.DadosRestaurante;
import br.com.fiap.restaurante.interfaceadapter.datasource.DadosTurno;
import br.com.fiap.restaurante.interfaceadapter.datasource.IRestauranteDataSource;
import br.com.fiap.restaurante.interfaceadapter.datasource.IUsuarioDataSource;
import br.com.fiap.restaurante.interfaceadapter.presenter.PaginaResponse;
import br.com.fiap.restaurante.interfaceadapter.presenter.RestauranteResponse;
import br.com.fiap.restaurante.interfaceadapter.presenter.TurnoResponse;
import br.com.fiap.restaurante.suporte.DadosDeExemplo;
import br.com.fiap.restaurante.suporte.TransacaoImediata;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Endpoints de restaurante")
class RestauranteApiControllerTest {

    private static final EnderecoRequest ENDERECO =
            new EnderecoRequest("Rua das Flores", "123", "Apto 45", "Centro", "Itajaí", "SC", "88301-000");
    private static final List<TurnoRequest> TURNOS = List.of(new TurnoRequest("SEXTA", "18:00", "02:00"),
            new TurnoRequest("SEGUNDA", "11:00", "15:00"));

    @Mock
    private IRestauranteDataSource restaurantes;

    @Mock
    private IUsuarioDataSource usuarios;

    private AutoCloseable mocks;
    private RestauranteApiController api;

    @BeforeEach
    void preparar() {
        mocks = MockitoAnnotations.openMocks(this);
        api = new RestauranteApiController(restaurantes, usuarios, new TransacaoImediata());
        when(usuarios.buscarPorId(8L)).thenReturn(Optional.of(DadosDeExemplo.ana()));
        when(usuarios.buscarPorIdParaAlterar(8L)).thenReturn(Optional.of(DadosDeExemplo.ana()));
        when(restaurantes.buscarPorId(9L)).thenReturn(Optional.of(DadosDeExemplo.cantina()));
        when(restaurantes.buscarPorIdParaAlterar(9L)).thenReturn(Optional.of(DadosDeExemplo.cantina()));
    }

    @AfterEach
    void encerrar() throws Exception {
        mocks.close();
    }

    @Test
    @DisplayName("RES-12 · cadastrar devolve 201 com o Location, o dono e os turnos ordenados")
    void deveCadastrarComLocation() {
        /* arrange */
        when(restaurantes.incluir(any())).thenReturn(DadosDeExemplo.cantina());
        RestauranteRequest requisicao = new RestauranteRequest("Cantina da Nona", ENDERECO, "ITALIANA", 8L, TURNOS);

        /* act */
        ResponseEntity<RestauranteResponse> resposta =
                api.cadastrar(requisicao, UriComponentsBuilder.fromUriString("http://localhost:8080"));

        /* assert */
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resposta.getHeaders().getLocation()).hasToString("http://localhost:8080/api/v1/restaurantes/9");
        assertThat(resposta.getBody().dono().nome()).isEqualTo("Ana Souza");
        assertThat(resposta.getBody().horarios()).extracting(TurnoResponse::diaSemana)
                .containsExactly("SEGUNDA", "SEXTA");
        ArgumentCaptor<DadosRestaurante> gravado = ArgumentCaptor.forClass(DadosRestaurante.class);
        verify(restaurantes).incluir(gravado.capture());
        assertThat(gravado.getValue().horarios()).contains(new DadosTurno("SEXTA", LocalTime.of(18, 0),
                LocalTime.of(2, 0)));
    }

    @Test
    @DisplayName("RES-13 · RES-14 · listar e buscar por id devolvem 200")
    void deveConsultar() {
        /* arrange */
        PedidoDePagina pedido = new PedidoDePagina(0, 10, List.of(new PedidoDePagina.Ordem("nome", true)));
        when(restaurantes.listar("", null, pedido))
                .thenReturn(new Pagina<>(List.of(DadosDeExemplo.cantina()), 0, 10, 1, 1));

        /* act */
        ResponseEntity<PaginaResponse<RestauranteResponse>> pagina =
                api.listar(null, null, PageRequest.of(0, 10, Sort.by("nome")));
        ResponseEntity<RestauranteResponse> porId = api.buscarPorId(9L);

        /* assert */
        assertThat(pagina.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(pagina.getBody().conteudo()).extracting(RestauranteResponse::id).containsExactly(9L);
        assertThat(porId.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(porId.getBody().nome()).isEqualTo("Cantina da Nona");
    }

    @Test
    @DisplayName("RES-15 · atualizar devolve 200 com os dados do corpo, aplicados ao id da rota")
    void deveAtualizar() {
        /* arrange */
        when(restaurantes.atualizar(any())).thenAnswer(chamada -> chamada.getArgument(0));
        RestauranteRequest requisicao = new RestauranteRequest("Pizzaria da Nona", ENDERECO, "PIZZARIA", 8L,
                List.of(new TurnoRequest("SABADO", "18:00", "23:00")));

        /* act */
        ResponseEntity<RestauranteResponse> resposta = api.atualizar(9L, requisicao);

        /* assert */
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resposta.getBody().id()).isEqualTo(9L);
        assertThat(resposta.getBody().tipoCozinha()).isEqualTo("PIZZARIA");
        assertThat(resposta.getBody().horarios()).extracting(TurnoResponse::diaSemana).containsExactly("SABADO");
    }

    @Test
    @DisplayName("RES-16 · excluir devolve 204")
    void deveExcluirSemCorpo() {
        /* act */
        ResponseEntity<Void> resposta = api.excluir(9L);

        /* assert */
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(restaurantes).remover(9L);
    }
}
