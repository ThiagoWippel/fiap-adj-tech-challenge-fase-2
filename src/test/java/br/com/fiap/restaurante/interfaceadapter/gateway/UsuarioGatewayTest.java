package br.com.fiap.restaurante.interfaceadapter.gateway;

import br.com.fiap.restaurante.application.dto.Pagina;
import br.com.fiap.restaurante.application.dto.PedidoDePagina;
import br.com.fiap.restaurante.domain.entity.Usuario;
import br.com.fiap.restaurante.interfaceadapter.datasource.DadosUsuario;
import br.com.fiap.restaurante.interfaceadapter.datasource.IUsuarioDataSource;
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

import static br.com.fiap.restaurante.suporte.Exemplos.CPF;
import static br.com.fiap.restaurante.suporte.Exemplos.maria;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Gateway de usuários")
class UsuarioGatewayTest {

    private static final DadosUsuario DADOS_MARIA = DadosDeExemplo.maria();

    @Mock
    private IUsuarioDataSource dataSource;

    private AutoCloseable mocks;
    private UsuarioGateway gateway;

    @BeforeEach
    void preparar() {
        mocks = MockitoAnnotations.openMocks(this);
        gateway = UsuarioGateway.create(dataSource);
    }

    @AfterEach
    void encerrar() throws Exception {
        mocks.close();
    }

    @Test
    @DisplayName("USU-06 · incluir converte o usuário em dados e devolve o usuário gravado")
    void deveConverterNosDoisSentidosAoIncluir() {
        /* arrange */
        when(dataSource.incluir(any())).thenReturn(DADOS_MARIA);

        /* act */
        Usuario gravado = gateway.incluir(maria());

        /* assert */
        ArgumentCaptor<DadosUsuario> enviados = ArgumentCaptor.forClass(DadosUsuario.class);
        verify(dataSource).incluir(enviados.capture());
        assertThat(enviados.getValue()).isEqualTo(DADOS_MARIA);
        assertThat(gravado).usingRecursiveComparison().isEqualTo(maria());
    }

    @Test
    @DisplayName("USU-11 · atualizar converte nos dois sentidos")
    void deveConverterNosDoisSentidosAoAtualizar() {
        /* arrange */
        when(dataSource.atualizar(DADOS_MARIA)).thenReturn(DADOS_MARIA);

        /* act */
        Usuario atualizado = gateway.atualizar(maria());

        /* assert */
        assertThat(atualizado).usingRecursiveComparison().isEqualTo(maria());
    }

    @Test
    @DisplayName("USU-24 · as buscas por id e por login devolvem o usuário convertido, ou vazio")
    void deveBuscarPorIdEPorLogin() {
        /* arrange */
        when(dataSource.buscarPorId(7L)).thenReturn(Optional.of(DADOS_MARIA));
        when(dataSource.buscarPorLogin("maria.silva")).thenReturn(Optional.of(DADOS_MARIA));

        /* act + assert */
        assertThat(gateway.buscarPorId(7L)).get().usingRecursiveComparison().isEqualTo(maria());
        assertThat(gateway.buscarPorId(8L)).isEmpty();
        assertThat(gateway.buscarPorLogin("maria.silva")).get().extracting(Usuario::getId).isEqualTo(7L);
        assertThat(gateway.buscarPorLogin("ninguem")).isEmpty();
    }

    @Test
    @DisplayName("USU-27 · a busca por nome converte cada usuário da lista e da página")
    void deveBuscarPorNome() {
        /* arrange */
        PedidoDePagina pedido = new PedidoDePagina(0, 10, List.of());
        when(dataSource.buscarPorNome("maria")).thenReturn(List.of(DADOS_MARIA));
        when(dataSource.buscarPorNome("maria", pedido)).thenReturn(new Pagina<>(List.of(DADOS_MARIA), 0, 10, 1, 1));

        /* act */
        List<Usuario> lista = gateway.buscarPorNome("maria");
        Pagina<Usuario> pagina = gateway.buscarPorNome("maria", pedido);

        /* assert */
        assertThat(lista).extracting(Usuario::getNome).containsExactly("Maria Silva");
        assertThat(pagina.conteudo()).extracting(Usuario::getNome).containsExactly("Maria Silva");
        assertThat(pagina.totalElementos()).isEqualTo(1);
    }

    @Test
    @DisplayName("USU-07 · as verificações de unicidade e a anonimização são repassadas à origem de dados")
    void deveRepassarVerificacoesEAnonimizacao() {
        /* arrange */
        when(dataSource.existeEmail("maria@exemplo.com")).thenReturn(true);
        when(dataSource.existeEmailEmOutroUsuario("maria@exemplo.com", 8L)).thenReturn(true);
        when(dataSource.existeLogin("maria.silva")).thenReturn(true);
        when(dataSource.existeLoginEmOutroUsuario("maria.silva", 8L)).thenReturn(true);
        when(dataSource.existeDocumento(CPF)).thenReturn(true);

        /* act */
        gateway.anonimizar(7L);

        /* assert */
        assertThat(gateway.existeEmail("maria@exemplo.com")).isTrue();
        assertThat(gateway.existeEmailEmOutroUsuario("maria@exemplo.com", 8L)).isTrue();
        assertThat(gateway.existeLogin("maria.silva")).isTrue();
        assertThat(gateway.existeLoginEmOutroUsuario("maria.silva", 8L)).isTrue();
        assertThat(gateway.existeDocumento(CPF)).isTrue();
        verify(dataSource).anonimizar(7L);
    }
}
