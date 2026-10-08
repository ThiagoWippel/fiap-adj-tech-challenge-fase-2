package br.com.fiap.restaurante.interfaceadapter.controller;

import br.com.fiap.restaurante.application.dto.AtualizacaoDeUsuarioDTO;
import br.com.fiap.restaurante.application.dto.NovoUsuarioDTO;
import br.com.fiap.restaurante.application.dto.Pagina;
import br.com.fiap.restaurante.application.dto.PedidoDePagina;
import br.com.fiap.restaurante.application.dto.TrocaDeSenhaDTO;
import br.com.fiap.restaurante.application.dto.TrocaDeTipoDTO;
import br.com.fiap.restaurante.application.port.IPasswordEncoder;
import br.com.fiap.restaurante.interfaceadapter.datasource.DadosTipoUsuario;
import br.com.fiap.restaurante.interfaceadapter.datasource.DadosUsuario;
import br.com.fiap.restaurante.interfaceadapter.datasource.IRestauranteDataSource;
import br.com.fiap.restaurante.interfaceadapter.datasource.ITipoUsuarioDataSource;
import br.com.fiap.restaurante.interfaceadapter.datasource.IUsuarioDataSource;
import br.com.fiap.restaurante.interfaceadapter.presenter.PaginaResponse;
import br.com.fiap.restaurante.interfaceadapter.presenter.RestauranteResponse;
import br.com.fiap.restaurante.interfaceadapter.presenter.UsuarioResponse;
import br.com.fiap.restaurante.suporte.DadosDeExemplo;
import br.com.fiap.restaurante.suporte.TransacaoImediata;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static br.com.fiap.restaurante.suporte.Exemplos.CPF;
import static br.com.fiap.restaurante.suporte.Exemplos.SENHA;
import static br.com.fiap.restaurante.suporte.Exemplos.SENHA_CODIFICADA;
import static br.com.fiap.restaurante.suporte.Exemplos.enderecoDTO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Controller de usuários")
class UsuarioControllerTest {

    @Mock
    private IUsuarioDataSource usuarios;

    @Mock
    private ITipoUsuarioDataSource tipos;

    @Mock
    private IRestauranteDataSource restaurantes;

    @Mock
    private IPasswordEncoder senhas;

    private AutoCloseable mocks;
    private UsuarioController controller;

    @BeforeEach
    void preparar() {
        mocks = MockitoAnnotations.openMocks(this);
        controller = UsuarioController.create(usuarios, tipos, restaurantes, senhas, new TransacaoImediata());
        when(usuarios.buscarPorId(7L)).thenReturn(Optional.of(DadosDeExemplo.maria()));
    }

    @AfterEach
    void encerrar() throws Exception {
        mocks.close();
    }

    @Test
    @DisplayName("USU-18 · cadastrar passa pelo caso de uso e devolve a resposta com o id gerado")
    void deveCadastrar() {
        /* arrange */
        when(tipos.buscarPorCodigo("CLIENTE")).thenReturn(Optional.of(DadosDeExemplo.CLIENTE));
        when(senhas.codificar(SENHA)).thenReturn(SENHA_CODIFICADA);
        when(usuarios.incluir(any())).thenReturn(DadosDeExemplo.maria());

        /* act */
        UsuarioResponse resposta = controller.cadastrar(new NovoUsuarioDTO("Maria Silva", "maria@exemplo.com",
                "maria.silva", SENHA, "CLIENTE", CPF, null, enderecoDTO()));

        /* assert */
        ArgumentCaptor<DadosUsuario> gravado = ArgumentCaptor.forClass(DadosUsuario.class);
        verify(usuarios).incluir(gravado.capture());
        assertThat(gravado.getValue().id()).isNull();
        assertThat(gravado.getValue().senha()).isEqualTo(SENHA_CODIFICADA);
        assertThat(resposta.id()).isEqualTo(7L);
        assertThat(resposta.tipo()).isEqualTo("CLIENTE");
    }

    @Test
    @DisplayName("USU-24 · buscar por id devolve a resposta do usuário")
    void deveBuscarPorId() {
        /* act */
        UsuarioResponse resposta = controller.buscarPorId(7L);

        /* assert */
        assertThat(resposta.nome()).isEqualTo("Maria Silva");
    }

    @Test
    @DisplayName("USU-27 · a busca da v1 devolve a lista e a da v2 devolve a página")
    void deveBuscarPorNomeEmListaEPaginado() {
        /* arrange */
        PedidoDePagina pedido = new PedidoDePagina(0, 10, List.of());
        when(usuarios.buscarPorNome("maria")).thenReturn(List.of(DadosDeExemplo.maria()));
        when(usuarios.buscarPorNome("maria", pedido))
                .thenReturn(new Pagina<>(List.of(DadosDeExemplo.maria()), 0, 10, 1, 1));

        /* act */
        List<UsuarioResponse> lista = controller.buscarPorNome("maria");
        PaginaResponse<UsuarioResponse> pagina = controller.buscarPorNomePaginado("maria", pedido);

        /* assert */
        assertThat(lista).extracting(UsuarioResponse::id).containsExactly(7L);
        assertThat(pagina.conteudo()).extracting(UsuarioResponse::id).containsExactly(7L);
        assertThat(pagina.ultima()).isTrue();
    }

    @Test
    @DisplayName("USU-11 · atualizar grava os novos dados e devolve a resposta")
    void deveAtualizar() {
        /* arrange */
        when(usuarios.atualizar(any())).thenAnswer(chamada -> chamada.getArgument(0));

        /* act */
        UsuarioResponse resposta = controller.atualizar(new AtualizacaoDeUsuarioDTO(7L, "Maria Silva Souza",
                "maria.souza@exemplo.com", "maria.souza", enderecoDTO()));

        /* assert */
        assertThat(resposta.nome()).isEqualTo("Maria Silva Souza");
        assertThat(resposta.login()).isEqualTo("maria.souza");
    }

    @Test
    @DisplayName("USU-15 · trocar a senha grava o novo hash")
    void deveTrocarASenha() {
        /* arrange */
        when(senhas.confere(SENHA, SENHA_CODIFICADA)).thenReturn(true);
        when(senhas.codificar("SenhaNova456")).thenReturn("hash-da-senha-nova");
        when(usuarios.atualizar(any())).thenAnswer(chamada -> chamada.getArgument(0));

        /* act */
        controller.trocarSenha(new TrocaDeSenhaDTO(7L, SENHA, "SenhaNova456"));

        /* assert */
        ArgumentCaptor<DadosUsuario> gravado = ArgumentCaptor.forClass(DadosUsuario.class);
        verify(usuarios).atualizar(gravado.capture());
        assertThat(gravado.getValue().senha()).isEqualTo("hash-da-senha-nova");
    }

    @Test
    @DisplayName("EXC-01 · excluir anonimiza o usuário")
    void deveExcluir() {
        /* act */
        controller.excluir(7L);

        /* assert */
        verify(usuarios).anonimizar(7L);
    }

    @Test
    @DisplayName("TRO-01 · trocar o tipo grava o novo tipo e o novo documento")
    void deveTrocarOTipo() {
        /* arrange */
        when(tipos.buscarPorCodigo("DONO_RESTAURANTE"))
                .thenReturn(Optional.of(new DadosTipoUsuario(2L, "Dono de Restaurante", "DONO_RESTAURANTE")));
        when(usuarios.atualizar(any())).thenAnswer(chamada -> chamada.getArgument(0));

        /* act */
        UsuarioResponse resposta = controller.trocarTipo(new TrocaDeTipoDTO(7L, "DONO_RESTAURANTE", "11222333000181"));

        /* assert */
        assertThat(resposta.tipo()).isEqualTo("DONO_RESTAURANTE");
        assertThat(resposta.documento()).isEqualTo("11222333000181");
    }

    @Test
    @DisplayName("RES-17 · lista os restaurantes ativos do usuário")
    void deveListarOsRestaurantesDoUsuario() {
        /* arrange */
        PedidoDePagina pedido = new PedidoDePagina(0, 10, List.of());
        when(usuarios.buscarPorId(8L)).thenReturn(Optional.of(DadosDeExemplo.ana()));
        when(restaurantes.buscarPorDono(8L, pedido))
                .thenReturn(new Pagina<>(List.of(DadosDeExemplo.cantina()), 0, 10, 1, 1));

        /* act */
        PaginaResponse<RestauranteResponse> pagina = controller.listarRestaurantes(8L, pedido);

        /* assert */
        assertThat(pagina.conteudo()).extracting(RestauranteResponse::nome).containsExactly("Cantina da Nona");
    }
}
