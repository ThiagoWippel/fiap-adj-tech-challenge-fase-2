package br.com.fiap.restaurante.infrastructure.api.usuario;

import br.com.fiap.restaurante.application.dto.Pagina;
import br.com.fiap.restaurante.application.dto.PedidoDePagina;
import br.com.fiap.restaurante.application.port.IPasswordEncoder;
import br.com.fiap.restaurante.infrastructure.api.comum.EnderecoRequest;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Optional;

import static br.com.fiap.restaurante.suporte.Exemplos.CNPJ;
import static br.com.fiap.restaurante.suporte.Exemplos.CPF;
import static br.com.fiap.restaurante.suporte.Exemplos.SENHA;
import static br.com.fiap.restaurante.suporte.Exemplos.SENHA_CODIFICADA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Endpoints de usuário da v1")
class UsuarioApiControllerTest {

    private static final EnderecoRequest ENDERECO =
            new EnderecoRequest("Rua das Flores", "123", "Apto 45", "Centro", "Itajaí", "SC", "88301-000");

    @Mock
    private IUsuarioDataSource usuarios;

    @Mock
    private ITipoUsuarioDataSource tipos;

    @Mock
    private IRestauranteDataSource restaurantes;

    @Mock
    private IPasswordEncoder senhas;

    private AutoCloseable mocks;
    private UsuarioApiController api;

    @BeforeEach
    void preparar() {
        mocks = MockitoAnnotations.openMocks(this);
        api = new UsuarioApiController(usuarios, tipos, restaurantes, senhas, new TransacaoImediata());
        when(usuarios.buscarPorId(7L)).thenReturn(Optional.of(DadosDeExemplo.maria()));
        when(usuarios.buscarPorIdParaAlterar(7L)).thenReturn(Optional.of(DadosDeExemplo.maria()));
    }

    @AfterEach
    void encerrar() throws Exception {
        mocks.close();
    }

    @Test
    @DisplayName("USU-18 · cadastrar devolve 201 com o Location do usuário criado, e o corpo vira o cadastro")
    void deveCadastrarComLocation() {
        /* arrange */
        when(tipos.buscarPorCodigo("CLIENTE")).thenReturn(Optional.of(DadosDeExemplo.CLIENTE));
        when(senhas.codificar(SENHA)).thenReturn(SENHA_CODIFICADA);
        when(usuarios.incluir(any())).thenReturn(DadosDeExemplo.maria());
        CriarUsuarioRequest requisicao = new CriarUsuarioRequest("Maria Silva", "maria@exemplo.com", "maria.silva",
                SENHA, "CLIENTE", CPF, null, ENDERECO);

        /* act */
        ResponseEntity<UsuarioResponse> resposta =
                api.cadastrar(requisicao, UriComponentsBuilder.fromUriString("http://localhost:8080"));

        /* assert */
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resposta.getHeaders().getLocation()).hasToString("http://localhost:8080/api/v1/usuarios/7");
        assertThat(resposta.getBody().tipo()).isEqualTo("CLIENTE");
        ArgumentCaptor<DadosUsuario> gravado = ArgumentCaptor.forClass(DadosUsuario.class);
        verify(usuarios).incluir(gravado.capture());
        assertThat(gravado.getValue().login()).isEqualTo("maria.silva");
        assertThat(gravado.getValue().documento()).isEqualTo(CPF);
        assertThat(gravado.getValue().endereco().cidade()).isEqualTo("Itajaí");
    }

    @Test
    @DisplayName("USU-24 · USU-27 · buscar por id e por nome devolvem 200 com o usuário")
    void deveBuscarPorIdEPorNome() {
        /* arrange */
        when(usuarios.buscarPorNome("maria")).thenReturn(List.of(DadosDeExemplo.maria()));

        /* act */
        ResponseEntity<UsuarioResponse> porId = api.buscarPorId(7L);
        ResponseEntity<List<UsuarioResponse>> porNome = api.buscarPorNome("maria");

        /* assert */
        assertThat(porId.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(porId.getBody().nome()).isEqualTo("Maria Silva");
        assertThat(porNome.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(porNome.getBody()).extracting(UsuarioResponse::id).containsExactly(7L);
    }

    @Test
    @DisplayName("USU-25 · atualizar devolve 200 com os dados do corpo, aplicados ao id da rota")
    void deveAtualizar() {
        /* arrange */
        when(usuarios.atualizar(any())).thenAnswer(chamada -> chamada.getArgument(0));
        AtualizarUsuarioRequest requisicao =
                new AtualizarUsuarioRequest("Maria Silva Souza", "maria.souza@exemplo.com", "maria.souza", ENDERECO);

        /* act */
        ResponseEntity<UsuarioResponse> resposta = api.atualizar(7L, requisicao);

        /* assert */
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resposta.getBody().id()).isEqualTo(7L);
        assertThat(resposta.getBody().email()).isEqualTo("maria.souza@exemplo.com");
        assertThat(resposta.getBody().login()).isEqualTo("maria.souza");
    }

    @Test
    @DisplayName("USU-26 · trocar a senha devolve 204, sem corpo")
    void deveTrocarASenhaSemCorpo() {
        /* arrange */
        when(senhas.confere(SENHA, SENHA_CODIFICADA)).thenReturn(true);
        when(senhas.codificar("SenhaNova456")).thenReturn("hash-da-senha-nova");
        when(usuarios.atualizar(any())).thenAnswer(chamada -> chamada.getArgument(0));

        /* act */
        ResponseEntity<Void> resposta = api.trocarSenha(7L, new TrocarSenhaRequest(SENHA, "SenhaNova456"));

        /* assert */
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(resposta.getBody()).isNull();
        ArgumentCaptor<DadosUsuario> gravado = ArgumentCaptor.forClass(DadosUsuario.class);
        verify(usuarios).atualizar(gravado.capture());
        assertThat(gravado.getValue().senha()).isEqualTo("hash-da-senha-nova");
    }

    @Test
    @DisplayName("TRO-08 · trocar o tipo devolve 200 com o novo tipo e o novo documento")
    void deveTrocarOTipo() {
        /* arrange */
        when(tipos.buscarPorCodigo("DONO_RESTAURANTE")).thenReturn(Optional.of(DadosDeExemplo.DONO));
        when(usuarios.atualizar(any())).thenAnswer(chamada -> chamada.getArgument(0));

        /* act */
        ResponseEntity<UsuarioResponse> resposta = api.trocarTipo(7L, new TrocarTipoRequest("DONO_RESTAURANTE", CNPJ));

        /* assert */
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resposta.getBody().tipo()).isEqualTo("DONO_RESTAURANTE");
        assertThat(resposta.getBody().documento()).isEqualTo(CNPJ);
    }

    @Test
    @DisplayName("RES-17 · os restaurantes do usuário saem paginados, com a ordenação pedida")
    void deveListarOsRestaurantesDoUsuario() {
        /* arrange */
        PedidoDePagina pedido = new PedidoDePagina(0, 10, List.of(new PedidoDePagina.Ordem("nome", true)));
        when(usuarios.buscarPorId(8L)).thenReturn(Optional.of(DadosDeExemplo.ana()));
        when(restaurantes.buscarPorDono(8L, pedido))
                .thenReturn(new Pagina<>(List.of(DadosDeExemplo.cantina()), 0, 10, 1, 1));

        /* act */
        ResponseEntity<PaginaResponse<RestauranteResponse>> resposta =
                api.listarRestaurantes(8L, PageRequest.of(0, 10, Sort.by("nome")));

        /* assert */
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resposta.getBody().conteudo()).extracting(RestauranteResponse::nome)
                .containsExactly("Cantina da Nona");
    }

    @Test
    @DisplayName("EXC-02 · excluir devolve 204 e anonimiza o usuário")
    void deveExcluirSemCorpo() {
        /* act */
        ResponseEntity<Void> resposta = api.excluir(7L);

        /* assert */
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(usuarios).anonimizar(7L);
    }
}
