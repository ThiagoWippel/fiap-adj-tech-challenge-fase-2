package br.com.fiap.restaurante.application.usecase.usuario;

import br.com.fiap.restaurante.application.dto.NovoUsuarioDTO;
import br.com.fiap.restaurante.application.exception.ConflitoDeDadosException;
import br.com.fiap.restaurante.application.exception.RecursoNaoEncontradoException;
import br.com.fiap.restaurante.application.gateway.ITipoUsuarioGateway;
import br.com.fiap.restaurante.application.gateway.IUsuarioGateway;
import br.com.fiap.restaurante.application.port.IPasswordEncoder;
import br.com.fiap.restaurante.domain.entity.TipoUsuario;
import br.com.fiap.restaurante.domain.entity.Usuario;
import br.com.fiap.restaurante.domain.exception.RegraDeNegocioException;
import br.com.fiap.restaurante.domain.exception.ValidacaoDeDominioException;
import br.com.fiap.restaurante.suporte.TransacaoImediata;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static br.com.fiap.restaurante.suporte.Exemplos.CNPJ;
import static br.com.fiap.restaurante.suporte.Exemplos.CPF;
import static br.com.fiap.restaurante.suporte.Exemplos.SENHA;
import static br.com.fiap.restaurante.suporte.Exemplos.SENHA_CODIFICADA;
import static br.com.fiap.restaurante.suporte.Exemplos.cliente;
import static br.com.fiap.restaurante.suporte.Exemplos.donoDeRestaurante;
import static br.com.fiap.restaurante.suporte.Exemplos.enderecoDTO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Cadastrar usuário")
class CadastrarUsuarioUseCaseTest {

    @Mock
    private IUsuarioGateway usuarios;

    @Mock
    private ITipoUsuarioGateway tipos;

    @Mock
    private IPasswordEncoder senhas;

    private AutoCloseable mocks;
    private TransacaoImediata transacao;
    private CadastrarUsuarioUseCase useCase;

    @BeforeEach
    void preparar() {
        mocks = MockitoAnnotations.openMocks(this);
        transacao = new TransacaoImediata();
        useCase = CadastrarUsuarioUseCase.create(usuarios, tipos, senhas, transacao);
        when(tipos.buscarPorCodigo(TipoUsuario.CODIGO_CLIENTE)).thenReturn(Optional.of(cliente()));
        when(tipos.buscarPorCodigo(TipoUsuario.CODIGO_DONO_RESTAURANTE)).thenReturn(Optional.of(donoDeRestaurante()));
        when(senhas.codificar(SENHA)).thenReturn(SENHA_CODIFICADA);
        when(usuarios.incluir(any())).thenAnswer(chamada -> chamada.getArgument(0));
    }

    @AfterEach
    void encerrar() throws Exception {
        mocks.close();
    }

    @Test
    @DisplayName("USU-06 · cadastra o usuário com a senha codificada, nunca em texto")
    void deveCadastrarComASenhaCodificada() {
        /* arrange */
        NovoUsuarioDTO dados = novoCliente("maria@exemplo.com", "maria.silva");

        /* act */
        Usuario cadastrado = useCase.run(dados);

        /* assert */
        ArgumentCaptor<Usuario> gravado = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarios).incluir(gravado.capture());
        assertThat(gravado.getValue().getSenha()).isEqualTo(SENHA_CODIFICADA);
        assertThat(cadastrado.getNome()).isEqualTo("Maria Silva");
        assertThat(cadastrado.getTipo().getCodigo()).isEqualTo(TipoUsuario.CODIGO_CLIENTE);
        assertThat(cadastrado.getDocumento().numero()).isEqualTo(CPF);
        assertThat(cadastrado.getEndereco().cep()).isEqualTo("88301000");
    }

    @Test
    @DisplayName("USU-06 · o cadastro roda dentro de uma transação")
    void deveCadastrarDentroDeUmaTransacao() {
        /* act */
        useCase.run(novoCliente("maria@exemplo.com", "maria.silva"));

        /* assert */
        assertThat(transacao.execucoes()).isEqualTo(1);
    }

    @Test
    @DisplayName("DOC-05 · Dono de Restaurante é cadastrado com o CNPJ, mesmo que o CPF também venha preenchido")
    void deveUsarOCnpjParaDonoDeRestaurante() {
        /* arrange */
        NovoUsuarioDTO dados = new NovoUsuarioDTO("Ana Souza", "ana@exemplo.com", "ana.souza", SENHA,
                TipoUsuario.CODIGO_DONO_RESTAURANTE, CPF, CNPJ, enderecoDTO());

        /* act */
        Usuario cadastrado = useCase.run(dados);

        /* assert */
        assertThat(cadastrado.getDocumento().numero()).isEqualTo(CNPJ);
        assertThat(cadastrado.ehDonoDeRestaurante()).isTrue();
    }

    @Test
    @DisplayName("USU-32 · o código do tipo é procurado sem os espaços das pontas")
    void deveProcurarOTipoSemEspacos() {
        /* arrange */
        NovoUsuarioDTO dados = new NovoUsuarioDTO("Maria Silva", "maria@exemplo.com", "maria.silva", SENHA,
                " CLIENTE ", CPF, null, enderecoDTO());

        /* act */
        Usuario cadastrado = useCase.run(dados);

        /* assert */
        assertThat(cadastrado.getTipo().getCodigo()).isEqualTo(TipoUsuario.CODIGO_CLIENTE);
        assertThat(CadastrarUsuarioUseCase.semEspacosNasPontas(null)).isNull();
    }

    @Test
    @DisplayName("USU-07 · e-mail já cadastrado devolve conflito e nada é gravado")
    void deveRecusarEmailJaCadastrado() {
        /* arrange */
        when(usuarios.existeEmail("maria@exemplo.com")).thenReturn(true);

        /* act + assert */
        assertThatThrownBy(() -> useCase.run(novoCliente("maria@exemplo.com", "maria.silva")))
                .isInstanceOf(ConflitoDeDadosException.class)
                .hasMessage("O e-mail informado já está cadastrado.");
        verify(usuarios, never()).incluir(any());
        verify(senhas, never()).codificar(anyString());
    }

    @Test
    @DisplayName("USU-08 · login já cadastrado devolve conflito")
    void deveRecusarLoginJaCadastrado() {
        /* arrange */
        when(usuarios.existeLogin("maria.silva")).thenReturn(true);

        /* act + assert */
        assertThatThrownBy(() -> useCase.run(novoCliente("maria@exemplo.com", "maria.silva")))
                .isInstanceOf(ConflitoDeDadosException.class)
                .hasMessage("O login informado já está cadastrado.");
        verify(usuarios, never()).incluir(any());
    }

    @Test
    @DisplayName("USU-09 · documento já cadastrado devolve conflito")
    void deveRecusarDocumentoJaCadastrado() {
        /* arrange */
        when(usuarios.existeDocumento(CPF)).thenReturn(true);

        /* act + assert */
        assertThatThrownBy(() -> useCase.run(novoCliente("maria@exemplo.com", "maria.silva")))
                .isInstanceOf(ConflitoDeDadosException.class)
                .hasMessage("O documento informado já está cadastrado.");
        verify(usuarios, never()).incluir(any());
    }

    @Test
    @DisplayName("USU-10 · código de tipo inexistente devolve não encontrado")
    void deveRecusarTipoInexistente() {
        /* arrange */
        NovoUsuarioDTO dados = new NovoUsuarioDTO("Maria Silva", "maria@exemplo.com", "maria.silva", SENHA,
                "ENTREGADOR", CPF, null, enderecoDTO());

        /* act + assert */
        assertThatThrownBy(() -> useCase.run(dados))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Tipo de usuário ENTREGADOR não encontrado.");
        verify(usuarios, never()).incluir(any());
    }

    @Test
    @DisplayName("USU-23 · cliente sem CPF é recusado pela regra do documento")
    void deveRecusarClienteSemCpf() {
        /* arrange */
        NovoUsuarioDTO dados = new NovoUsuarioDTO("Maria Silva", "maria@exemplo.com", "maria.silva", SENHA,
                TipoUsuario.CODIGO_CLIENTE, null, CNPJ, enderecoDTO());

        /* act + assert */
        assertThatThrownBy(() -> useCase.run(dados))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessage("O CPF é obrigatório para usuários do tipo Cliente.");
        verify(usuarios, never()).incluir(any());
    }

    @Test
    @DisplayName("DOC-05 · Dono de Restaurante sem CNPJ é recusado pela regra do documento")
    void deveRecusarDonoSemCnpj() {
        /* arrange */
        NovoUsuarioDTO dados = new NovoUsuarioDTO("Ana Souza", "ana@exemplo.com", "ana.souza", SENHA,
                TipoUsuario.CODIGO_DONO_RESTAURANTE, CPF, null, enderecoDTO());

        /* act + assert */
        assertThatThrownBy(() -> useCase.run(dados))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessage("O CNPJ é obrigatório para usuários do tipo Dono de Restaurante.");
        verify(usuarios, never()).incluir(any());
    }

    @Test
    @DisplayName("USU-26 · senha fora de 8 a 72 caracteres é recusada antes de codificar")
    void deveRecusarSenhaForaDoLimite() {
        /* arrange */
        NovoUsuarioDTO dados = new NovoUsuarioDTO("Maria Silva", "maria@exemplo.com", "maria.silva", "curta",
                TipoUsuario.CODIGO_CLIENTE, CPF, null, enderecoDTO());

        /* act + assert */
        assertThatThrownBy(() -> useCase.run(dados))
                .isInstanceOf(ValidacaoDeDominioException.class)
                .hasMessage("A senha deve ter entre 8 e 72 caracteres.");
        verify(senhas, never()).codificar(anyString());
        verify(usuarios, never()).incluir(any());
    }

    @Test
    @DisplayName("USU-22 · endereço ausente é recusado")
    void deveRecusarEnderecoAusente() {
        /* arrange */
        NovoUsuarioDTO dados = new NovoUsuarioDTO("Maria Silva", "maria@exemplo.com", "maria.silva", SENHA,
                TipoUsuario.CODIGO_CLIENTE, CPF, null, null);

        /* act + assert */
        assertThatThrownBy(() -> useCase.run(dados))
                .isInstanceOf(ValidacaoDeDominioException.class)
                .hasMessage("O endereço é obrigatório.");
    }

    private static NovoUsuarioDTO novoCliente(String email, String login) {
        return new NovoUsuarioDTO("Maria Silva", email, login, SENHA, TipoUsuario.CODIGO_CLIENTE, CPF, null,
                enderecoDTO());
    }
}
