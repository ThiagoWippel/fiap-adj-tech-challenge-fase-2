package br.com.fiap.restaurante.application.usecase.usuario;

import br.com.fiap.restaurante.application.dto.AtualizacaoDeUsuarioDTO;
import br.com.fiap.restaurante.application.dto.EnderecoDTO;
import br.com.fiap.restaurante.application.exception.ConflitoDeDadosException;
import br.com.fiap.restaurante.application.exception.RecursoNaoEncontradoException;
import br.com.fiap.restaurante.application.gateway.IUsuarioGateway;
import br.com.fiap.restaurante.domain.entity.Usuario;
import br.com.fiap.restaurante.domain.exception.ValidacaoDeDominioException;
import br.com.fiap.restaurante.suporte.TransacaoImediata;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static br.com.fiap.restaurante.suporte.Exemplos.SENHA_CODIFICADA;
import static br.com.fiap.restaurante.suporte.Exemplos.maria;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Atualizar usuário")
class AtualizarUsuarioUseCaseTest {

    private static final EnderecoDTO NOVO_ENDERECO =
            new EnderecoDTO("Avenida Brasil", "S/N", null, "Centro", "Balneário Camboriú", "SC", "88330-000");

    @Mock
    private IUsuarioGateway usuarios;

    private AutoCloseable mocks;
    private TransacaoImediata transacao;
    private AtualizarUsuarioUseCase useCase;

    @BeforeEach
    void preparar() {
        mocks = MockitoAnnotations.openMocks(this);
        transacao = new TransacaoImediata();
        useCase = AtualizarUsuarioUseCase.create(usuarios, transacao);
        when(usuarios.buscarPorId(7L)).thenReturn(Optional.of(maria()));
        when(usuarios.atualizar(any())).thenAnswer(chamada -> chamada.getArgument(0));
    }

    @AfterEach
    void encerrar() throws Exception {
        mocks.close();
    }

    @Test
    @DisplayName("USU-11 · altera nome, e-mail, login e endereço sem tocar na senha nem no tipo")
    void deveAtualizarOsDados() {
        /* arrange */
        AtualizacaoDeUsuarioDTO dados = new AtualizacaoDeUsuarioDTO(7L, "Maria Silva Souza",
                "maria.souza@exemplo.com", "maria.souza", NOVO_ENDERECO);

        /* act */
        Usuario atualizado = useCase.run(dados);

        /* assert */
        assertThat(atualizado.getNome()).isEqualTo("Maria Silva Souza");
        assertThat(atualizado.getEmail()).isEqualTo("maria.souza@exemplo.com");
        assertThat(atualizado.getLogin()).isEqualTo("maria.souza");
        assertThat(atualizado.getEndereco().cidade()).isEqualTo("Balneário Camboriú");
        assertThat(atualizado.getSenha()).isEqualTo(SENHA_CODIFICADA);
        assertThat(atualizado.getTipo()).isEqualTo(maria().getTipo());
        assertThat(transacao.execucoes()).isEqualTo(1);
    }

    @Test
    @DisplayName("USU-12 · e-mail de outro usuário devolve conflito")
    void deveRecusarEmailDeOutroUsuario() {
        /* arrange */
        when(usuarios.existeEmailEmOutroUsuario("ana@exemplo.com", 7L)).thenReturn(true);
        AtualizacaoDeUsuarioDTO dados = new AtualizacaoDeUsuarioDTO(7L, "Maria Silva", "ana@exemplo.com",
                "maria.silva", NOVO_ENDERECO);

        /* act + assert */
        assertThatThrownBy(() -> useCase.run(dados))
                .isInstanceOf(ConflitoDeDadosException.class)
                .hasMessage("O e-mail informado já está cadastrado.");
        verify(usuarios, never()).atualizar(any());
    }

    @Test
    @DisplayName("USU-12 · login de outro usuário devolve conflito")
    void deveRecusarLoginDeOutroUsuario() {
        /* arrange */
        when(usuarios.existeLoginEmOutroUsuario("ana.souza", 7L)).thenReturn(true);
        AtualizacaoDeUsuarioDTO dados = new AtualizacaoDeUsuarioDTO(7L, "Maria Silva", "maria@exemplo.com",
                "ana.souza", NOVO_ENDERECO);

        /* act + assert */
        assertThatThrownBy(() -> useCase.run(dados))
                .isInstanceOf(ConflitoDeDadosException.class)
                .hasMessage("O login informado já está cadastrado.");
        verify(usuarios, never()).atualizar(any());
    }

    @Test
    @DisplayName("USU-13 · manter o próprio e-mail e o próprio login é aceito")
    void deveAceitarOsPropriosEmailELogin() {
        /* arrange */
        AtualizacaoDeUsuarioDTO dados = new AtualizacaoDeUsuarioDTO(7L, "Maria Silva", "maria@exemplo.com",
                "maria.silva", NOVO_ENDERECO);

        /* act */
        Usuario atualizado = useCase.run(dados);

        /* assert */
        assertThat(atualizado.getEmail()).isEqualTo("maria@exemplo.com");
        verify(usuarios).existeEmailEmOutroUsuario("maria@exemplo.com", 7L);
        verify(usuarios).atualizar(any());
    }

    @Test
    @DisplayName("USU-14 · usuário inexistente ou removido devolve não encontrado")
    void deveRecusarUsuarioInexistente() {
        /* arrange */
        AtualizacaoDeUsuarioDTO dados = new AtualizacaoDeUsuarioDTO(99L, "Maria Silva", "maria@exemplo.com",
                "maria.silva", NOVO_ENDERECO);

        /* act + assert */
        assertThatThrownBy(() -> useCase.run(dados))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Usuário 99 não encontrado.");
        verify(usuarios, never()).atualizar(any());
    }

    @Test
    @DisplayName("USU-05 · endereço ausente na atualização é recusado e nada é gravado")
    void deveRecusarEnderecoAusente() {
        /* arrange */
        AtualizacaoDeUsuarioDTO dados = new AtualizacaoDeUsuarioDTO(7L, "Maria Silva", "maria@exemplo.com",
                "maria.silva", null);

        /* act + assert */
        assertThatThrownBy(() -> useCase.run(dados))
                .isInstanceOf(ValidacaoDeDominioException.class)
                .hasMessage("O endereço é obrigatório.");
        verify(usuarios, never()).atualizar(any());
    }
}
