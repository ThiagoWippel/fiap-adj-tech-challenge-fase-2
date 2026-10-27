package br.com.fiap.restaurante.infrastructure.api.tipousuario;

import br.com.fiap.restaurante.application.dto.Pagina;
import br.com.fiap.restaurante.application.dto.PedidoDePagina;
import br.com.fiap.restaurante.interfaceadapter.datasource.DadosTipoUsuario;
import br.com.fiap.restaurante.interfaceadapter.datasource.ITipoUsuarioDataSource;
import br.com.fiap.restaurante.interfaceadapter.datasource.IUsuarioDataSource;
import br.com.fiap.restaurante.interfaceadapter.presenter.PaginaResponse;
import br.com.fiap.restaurante.interfaceadapter.presenter.TipoUsuarioResponse;
import br.com.fiap.restaurante.interfaceadapter.presenter.UsuarioResponse;
import br.com.fiap.restaurante.suporte.DadosDeExemplo;
import br.com.fiap.restaurante.suporte.TransacaoImediata;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Endpoints de tipo de usuário")
class TipoUsuarioApiControllerTest {

    private static final DadosTipoUsuario ENTREGADOR = new DadosTipoUsuario(3L, "Entregador", "ENTREGADOR");
    private static final PedidoDePagina PEDIDO =
            new PedidoDePagina(0, 10, List.of(new PedidoDePagina.Ordem("nome", true)));

    @Mock
    private ITipoUsuarioDataSource tipos;

    @Mock
    private IUsuarioDataSource usuarios;

    private AutoCloseable mocks;
    private TipoUsuarioApiController api;

    @BeforeEach
    void preparar() {
        mocks = MockitoAnnotations.openMocks(this);
        api = new TipoUsuarioApiController(tipos, usuarios, new TransacaoImediata());
        when(tipos.buscarPorId(1L)).thenReturn(Optional.of(DadosDeExemplo.CLIENTE));
        when(tipos.buscarPorId(3L)).thenReturn(Optional.of(ENTREGADOR));
        when(tipos.buscarPorIdParaAlterar(3L)).thenReturn(Optional.of(ENTREGADOR));
    }

    @AfterEach
    void encerrar() throws Exception {
        mocks.close();
    }

    @Test
    @DisplayName("TIP-16 · cadastrar devolve 201 com o código gerado e o Location do tipo")
    void deveCadastrarComLocation() {
        /* arrange */
        when(tipos.buscarPorCodigo("ENTREGADOR")).thenReturn(Optional.empty());
        when(tipos.incluir(any())).thenReturn(ENTREGADOR);

        /* act */
        ResponseEntity<TipoUsuarioResponse> resposta = api.cadastrar(new TipoUsuarioRequest("Entregador"),
                UriComponentsBuilder.fromUriString("http://localhost:8080"));

        /* assert */
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resposta.getHeaders().getLocation()).hasToString("http://localhost:8080/api/v1/tipos-usuario/3");
        assertThat(resposta.getBody().codigo()).isEqualTo("ENTREGADOR");
    }

    @Test
    @DisplayName("TIP-16 · listar, buscar por id e renomear devolvem 200")
    void deveConsultarERenomear() {
        /* arrange */
        when(tipos.listar(PEDIDO)).thenReturn(new Pagina<>(List.of(DadosDeExemplo.CLIENTE, ENTREGADOR), 0, 10, 2, 1));
        when(tipos.atualizar(any())).thenAnswer(chamada -> chamada.getArgument(0));

        /* act */
        ResponseEntity<PaginaResponse<TipoUsuarioResponse>> pagina = api.listar(PageRequest.of(0, 10, Sort.by("nome")));
        ResponseEntity<TipoUsuarioResponse> porId = api.buscarPorId(1L);
        ResponseEntity<TipoUsuarioResponse> renomeado =
                api.renomear(3L, new TipoUsuarioRequest("Entregador Parceiro"));

        /* assert */
        assertThat(pagina.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(pagina.getBody().conteudo()).hasSize(2);
        assertThat(porId.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(porId.getBody().sistema()).isTrue();
        assertThat(renomeado.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(renomeado.getBody().nome()).isEqualTo("Entregador Parceiro");
        assertThat(renomeado.getBody().codigo()).isEqualTo("ENTREGADOR");
    }

    @Test
    @DisplayName("TIP-16 · excluir devolve 204")
    void deveExcluirSemCorpo() {
        /* act */
        ResponseEntity<Void> resposta = api.excluir(3L);

        /* assert */
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(tipos).excluir(3L);
    }

    @Test
    @DisplayName("TIP-17 · os usuários do tipo saem paginados")
    void deveListarOsUsuariosDoTipo() {
        /* arrange */
        when(usuarios.buscarPorTipo(1L, PEDIDO)).thenReturn(new Pagina<>(List.of(DadosDeExemplo.maria()), 0, 10, 1, 1));

        /* act */
        ResponseEntity<PaginaResponse<UsuarioResponse>> resposta =
                api.listarUsuarios(1L, PageRequest.of(0, 10, Sort.by("nome")));

        /* assert */
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resposta.getBody().conteudo()).extracting(UsuarioResponse::tipo).containsExactly("CLIENTE");
    }
}
