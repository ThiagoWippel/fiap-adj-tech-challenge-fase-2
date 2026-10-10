package br.com.fiap.restaurante.application.usecase.autenticacao;

import br.com.fiap.restaurante.application.dto.Autenticacao;
import br.com.fiap.restaurante.application.dto.CredenciaisDTO;
import br.com.fiap.restaurante.application.dto.TokenDeAcesso;
import br.com.fiap.restaurante.application.exception.CredenciaisInvalidasException;
import br.com.fiap.restaurante.application.gateway.IUsuarioGateway;
import br.com.fiap.restaurante.application.port.IPasswordEncoder;
import br.com.fiap.restaurante.application.port.ITokenGenerator;
import br.com.fiap.restaurante.domain.entity.Usuario;
import br.com.fiap.restaurante.domain.exception.ValidacaoDeDominioException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;

import static br.com.fiap.restaurante.suporte.Exemplos.SENHA;
import static br.com.fiap.restaurante.suporte.Exemplos.SENHA_CODIFICADA;
import static br.com.fiap.restaurante.suporte.Exemplos.maria;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Autenticar usuário")
class AutenticarUsuarioUseCaseTest {

    private static final TokenDeAcesso TOKEN = new TokenDeAcesso("eyJhbGciOiJIUzI1NiJ9.exemplo.assinatura",
            LocalDateTime.of(2026, 10, 8, 11, 0, 0));

    @Mock
    private IUsuarioGateway usuarios;

    @Mock
    private IPasswordEncoder senhas;

    @Mock
    private ITokenGenerator tokens;

    private AutoCloseable mocks;
    private AutenticarUsuarioUseCase useCase;

    @BeforeEach
    void preparar() {
        mocks = MockitoAnnotations.openMocks(this);
        useCase = AutenticarUsuarioUseCase.create(usuarios, senhas, tokens);
        when(usuarios.buscarPorLogin("maria.silva")).thenReturn(Optional.of(maria()));
        when(senhas.confere(SENHA, SENHA_CODIFICADA)).thenReturn(true);
        when(tokens.gerar(any())).thenReturn(TOKEN);
    }

    @AfterEach
    void encerrar() throws Exception {
        mocks.close();
    }

    @Test
    @DisplayName("LOG-08 · o login é procurado sem os espaços das pontas")
    void deveProcurarOLoginSemEspacos() {
        /* act */
        Autenticacao autenticacao = useCase.run(new CredenciaisDTO(" maria.silva ", SENHA));

        /* assert */
        assertThat(autenticacao.usuario().getId()).isEqualTo(7L);
    }

    @Test
    @DisplayName("LOG-01 · credenciais válidas devolvem o usuário e o token")
    void deveAutenticarEDevolverOToken() {
        /* act */
        Autenticacao autenticacao = useCase.run(new CredenciaisDTO("maria.silva", SENHA));

        /* assert */
        assertThat(autenticacao.usuario().getId()).isEqualTo(7L);
        assertThat(autenticacao.token()).isEqualTo(TOKEN);
    }

    @Test
    @DisplayName("LOG-02 · login inexistente devolve a mesma mensagem da senha incorreta")
    void deveRecusarLoginInexistente() {
        /* arrange */
        when(usuarios.buscarPorLogin("ninguem")).thenReturn(Optional.empty());

        /* act + assert */
        assertThatThrownBy(() -> useCase.run(new CredenciaisDTO("ninguem", SENHA)))
                .isInstanceOf(CredenciaisInvalidasException.class)
                .hasMessage("Login ou senha inválidos.");
        verify(tokens, never()).gerar(any(Usuario.class));
    }

    @Test
    @DisplayName("LOG-03 · login inexistente ainda passa pela conferência de senha, para levar o mesmo tempo")
    void deveConferirASenhaMesmoComLoginInexistente() {
        /* arrange */
        when(usuarios.buscarPorLogin("ninguem")).thenReturn(Optional.empty());

        /* act */
        assertThatThrownBy(() -> useCase.run(new CredenciaisDTO("ninguem", SENHA)))
                .isInstanceOf(CredenciaisInvalidasException.class);

        /* assert */
        verify(senhas).confere(SENHA, null);
    }

    @Test
    @DisplayName("LOG-04 · senha incorreta devolve credenciais inválidas")
    void deveRecusarSenhaIncorreta() {
        /* act + assert */
        assertThatThrownBy(() -> useCase.run(new CredenciaisDTO("maria.silva", "SenhaErrada1")))
                .isInstanceOf(CredenciaisInvalidasException.class)
                .hasMessage("Login ou senha inválidos.");
        verify(tokens, never()).gerar(any(Usuario.class));
    }

    @Test
    @DisplayName("LOG-06 · usuário removido não é encontrado pelo login e recebe a mesma resposta")
    void deveRecusarUsuarioRemovido() {
        /* arrange */
        // a busca do gateway só considera usuários ativos
        when(usuarios.buscarPorLogin("maria.silva")).thenReturn(Optional.empty());

        /* act + assert */
        assertThatThrownBy(() -> useCase.run(new CredenciaisDTO("maria.silva", SENHA)))
                .isInstanceOf(CredenciaisInvalidasException.class)
                .hasMessage("Login ou senha inválidos.");
        verify(tokens, never()).gerar(any(Usuario.class));
    }

    @ParameterizedTest(name = "LOG-07 · login \"{0}\" e senha \"{1}\" são dados inválidos e o banco nem é consultado")
    @CsvSource(value = {"NULO, SenhaSegura123", "maria.silva, NULO", "'  ', SenhaSegura123", "maria.silva, ''"},
            nullValues = "NULO")
    void deveRecusarCredenciaisAusentes(String login, String senha) {
        /* act + assert */
        assertThatThrownBy(() -> useCase.run(new CredenciaisDTO(login, senha)))
                .isInstanceOf(ValidacaoDeDominioException.class)
                .hasMessage("Login e senha são obrigatórios.");
        verify(usuarios, never()).buscarPorLogin(anyString());
    }
}
