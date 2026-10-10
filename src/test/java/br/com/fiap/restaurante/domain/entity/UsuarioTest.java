package br.com.fiap.restaurante.domain.entity;

import br.com.fiap.restaurante.domain.exception.RegraDeNegocioException;
import br.com.fiap.restaurante.domain.exception.ValidacaoDeDominioException;
import br.com.fiap.restaurante.domain.valueobject.Documento;
import br.com.fiap.restaurante.domain.valueobject.Endereco;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Usuário")
class UsuarioTest {

    private static final String SENHA_CODIFICADA = "$2a$10$7EqJtq98hPqEX7fNZaFWoO5ZQ9QxM0rR5c7wEJxq1jC1r7Mq0x5mG";

    private final TipoUsuario cliente = TipoUsuario.create(1L, "Cliente", "CLIENTE");
    private final TipoUsuario dono = TipoUsuario.create(2L, "Dono de Restaurante", "DONO_RESTAURANTE");
    private final TipoUsuario entregador = TipoUsuario.create(3L, "Entregador", "ENTREGADOR");
    private final Endereco endereco = new Endereco("Rua das Flores", "123", null, "Centro", "Itajaí", "SC", "88301000");
    private final Documento cpf = Documento.cpf("12345678909");
    private final Documento cnpj = Documento.cnpj("11222333000181");

    @Test
    @DisplayName("USU-01 · usuário válido é criado com todos os dados")
    void deveCriarUsuarioValido() {
        /* act */
        Usuario usuario = Usuario.create("  Maria Silva ", " maria@exemplo.com ", "maria.silva", SENHA_CODIFICADA,
                endereco, cliente, cpf);

        /* assert */
        assertThat(usuario.getId()).isNull();
        assertThat(usuario.getNome()).isEqualTo("Maria Silva");
        assertThat(usuario.getEmail()).isEqualTo("maria@exemplo.com");
        assertThat(usuario.getLogin()).isEqualTo("maria.silva");
        assertThat(usuario.getSenha()).isEqualTo(SENHA_CODIFICADA);
        assertThat(usuario.getEndereco()).isEqualTo(endereco);
        assertThat(usuario.getTipo()).isEqualTo(cliente);
        assertThat(usuario.getDocumento()).isEqualTo(cpf);
        assertThat(usuario.ehDonoDeRestaurante()).isFalse();
    }

    @Test
    @DisplayName("USU-01 · ao ler do banco, o usuário vem com id e datas")
    void deveReconstituirUsuarioComIdEDatas() {
        /* arrange */
        LocalDateTime criacao = LocalDateTime.of(2026, 10, 1, 10, 0);
        LocalDateTime alteracao = LocalDateTime.of(2026, 10, 5, 14, 30);

        /* act */
        Usuario usuario = Usuario.create(7L, "Maria Silva", "maria@exemplo.com", "maria.silva", SENHA_CODIFICADA,
                endereco, dono, cnpj, criacao, alteracao);

        /* assert */
        assertThat(usuario.getId()).isEqualTo(7L);
        assertThat(usuario.getDataCriacao()).isEqualTo(criacao);
        assertThat(usuario.getDataUltimaAlteracao()).isEqualTo(alteracao);
        assertThat(usuario.ehDonoDeRestaurante()).isTrue();
    }

    @ParameterizedTest(name = "USU-02 · nome \"{0}\" é recusado")
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "Al"})
    void deveRecusarNomeInvalido(String nome) {
        /* act + assert */
        assertThatThrownBy(() -> Usuario.create(nome, "maria@exemplo.com", "maria.silva", SENHA_CODIFICADA,
                endereco, cliente, cpf))
                .isInstanceOf(ValidacaoDeDominioException.class)
                .hasMessage("O nome deve ter entre 3 e 120 caracteres.");
    }

    @Test
    @DisplayName("USU-02 · nome com mais de 120 caracteres é recusado")
    void deveRecusarNomeLongoDemais() {
        /* act + assert */
        assertThatThrownBy(() -> Usuario.create("M".repeat(121), "maria@exemplo.com", "maria.silva",
                SENHA_CODIFICADA, endereco, cliente, cpf))
                .hasMessage("O nome deve ter entre 3 e 120 caracteres.");
    }

    @ParameterizedTest(name = "USU-03 · e-mail \"{0}\" é recusado")
    @NullAndEmptySource
    @ValueSource(strings = {"maria", "maria@", "maria@exemplo", "maria silva@exemplo.com", "@exemplo.com"})
    void deveRecusarEmailInvalido(String email) {
        /* act + assert */
        assertThatThrownBy(() -> Usuario.create("Maria Silva", email, "maria.silva", SENHA_CODIFICADA,
                endereco, cliente, cpf))
                .isInstanceOf(ValidacaoDeDominioException.class)
                .hasMessage("O e-mail informado não é válido.");
    }

    @Test
    @DisplayName("USU-03 · e-mail com mais de 255 caracteres é recusado")
    void deveRecusarEmailLongoDemais() {
        /* arrange */
        String email = "m".repeat(250) + "@exemplo.com";

        /* act + assert */
        assertThatThrownBy(() -> Usuario.create("Maria Silva", email, "maria.silva", SENHA_CODIFICADA,
                endereco, cliente, cpf))
                .hasMessage("O e-mail informado não é válido.");
    }

    @ParameterizedTest(name = "USU-04 · login \"{0}\" é recusado")
    @NullAndEmptySource
    @ValueSource(strings = {"mar", "maria silva", "maria#silva", "m23456789012345678901234567890123456789012345678901"})
    void deveRecusarLoginInvalido(String login) {
        /* act + assert */
        assertThatThrownBy(() -> Usuario.create("Maria Silva", "maria@exemplo.com", login, SENHA_CODIFICADA,
                endereco, cliente, cpf))
                .isInstanceOf(ValidacaoDeDominioException.class)
                .hasMessage("O login deve ter de 4 a 50 caracteres: letras, números, ponto, hífen ou sublinhado.");
    }

    @Test
    @DisplayName("USU-01 · senha codificada e endereço são obrigatórios")
    void deveExigirSenhaEEndereco() {
        /* act + assert */
        assertThatThrownBy(() -> Usuario.create("Maria Silva", "maria@exemplo.com", "maria.silva", " ",
                endereco, cliente, cpf))
                .isInstanceOf(ValidacaoDeDominioException.class)
                .hasMessage("A senha é obrigatória.");
        assertThatThrownBy(() -> Usuario.create("Maria Silva", "maria@exemplo.com", "maria.silva", null,
                endereco, cliente, cpf))
                .hasMessage("A senha é obrigatória.");
        assertThatThrownBy(() -> Usuario.create("Maria Silva", "maria@exemplo.com", "maria.silva", SENHA_CODIFICADA,
                null, cliente, cpf))
                .hasMessage("O endereço é obrigatório.");
        assertThatThrownBy(() -> Usuario.create("Maria Silva", "maria@exemplo.com", "maria.silva", SENHA_CODIFICADA,
                endereco, null, cpf))
                .hasMessage("O tipo do usuário é obrigatório.");
    }

    @Test
    @DisplayName("USU-05 · alterar um dado para um valor inválido falha e o usuário mantém o valor anterior")
    void deveManterOValorAnterior_QuandoAAlteracaoFalhar() {
        /* arrange */
        Usuario usuario = Usuario.create("Maria Silva", "maria@exemplo.com", "maria.silva", SENHA_CODIFICADA,
                endereco, cliente, cpf);

        /* act */
        assertThatThrownBy(() -> usuario.setNome("M")).isInstanceOf(ValidacaoDeDominioException.class);
        assertThatThrownBy(() -> usuario.setEmail("invalido")).isInstanceOf(ValidacaoDeDominioException.class);

        /* assert */
        assertThat(usuario.getNome()).isEqualTo("Maria Silva");
        assertThat(usuario.getEmail()).isEqualTo("maria@exemplo.com");
    }

    @Test
    @DisplayName("USU-11 · a atualização troca nome, e-mail, login, endereço e senha pelos setters")
    void deveAtualizarOsDadosPelosSetters() {
        /* arrange */
        Usuario usuario = Usuario.create("Maria Silva", "maria@exemplo.com", "maria.silva", SENHA_CODIFICADA,
                endereco, cliente, cpf);
        Endereco novoEndereco = new Endereco("Avenida Brasil", "S/N", "Loja 2", "Centro", "Balneário Camboriú", "SC",
                "88330000");

        /* act */
        usuario.setNome("Maria Silva Souza");
        usuario.setEmail("maria.souza@exemplo.com");
        usuario.setLogin("maria.souza");
        usuario.setEndereco(novoEndereco);
        usuario.setSenha("$2a$10$outroHashCodificadoQualquerParaOTesteDeTrocaDeSenha12345678");

        /* assert */
        assertThat(usuario.getNome()).isEqualTo("Maria Silva Souza");
        assertThat(usuario.getEmail()).isEqualTo("maria.souza@exemplo.com");
        assertThat(usuario.getLogin()).isEqualTo("maria.souza");
        assertThat(usuario.getEndereco()).isEqualTo(novoEndereco);
        assertThat(usuario.getSenha()).startsWith("$2a$10$outro");
    }

    @Test
    @DisplayName("DOC-05 · Dono de Restaurante exige CNPJ; com CPF é recusado")
    void donoDeRestauranteDeveExigirCnpj() {
        /* act */
        Usuario comCnpj = Usuario.create("Restaurante da Ana", "ana@exemplo.com", "ana.dona", SENHA_CODIFICADA,
                endereco, dono, cnpj);

        /* assert */
        assertThat(comCnpj.getDocumento()).isEqualTo(cnpj);
        assertThatThrownBy(() -> Usuario.create("Restaurante da Ana", "ana@exemplo.com", "ana.dona",
                SENHA_CODIFICADA, endereco, dono, cpf))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessage("Usuário do tipo Dono de Restaurante deve informar CNPJ.");
        assertThatThrownBy(() -> Usuario.create("Restaurante da Ana", "ana@exemplo.com", "ana.dona",
                SENHA_CODIFICADA, endereco, dono, null))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessage("O CNPJ é obrigatório para usuários do tipo Dono de Restaurante.");
    }

    @Test
    @DisplayName("DOC-06 · qualquer outro tipo exige CPF; com CNPJ é recusado")
    void demaisTiposDevemExigirCpf() {
        /* act */
        Usuario entregadorComCpf = Usuario.create("João Lima", "joao@exemplo.com", "joao.lima", SENHA_CODIFICADA,
                endereco, entregador, cpf);

        /* assert */
        assertThat(entregadorComCpf.getDocumento()).isEqualTo(cpf);
        assertThatThrownBy(() -> Usuario.create("Maria Silva", "maria@exemplo.com", "maria.silva",
                SENHA_CODIFICADA, endereco, cliente, cnpj))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessage("Usuário do tipo Cliente deve informar CPF.");
        assertThatThrownBy(() -> Usuario.create("Maria Silva", "maria@exemplo.com", "maria.silva",
                SENHA_CODIFICADA, endereco, cliente, null))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessage("O CPF é obrigatório para usuários do tipo Cliente.");
    }

    @Test
    @DisplayName("USU-01 · dois usuários são o mesmo quando têm o mesmo id")
    void deveCompararUsuariosPeloId() {
        /* arrange */
        LocalDateTime agora = LocalDateTime.of(2026, 10, 1, 10, 0);

        /* act */
        Usuario maria = Usuario.create(7L, "Maria Silva", "maria@exemplo.com", "maria.silva", SENHA_CODIFICADA,
                endereco, cliente, cpf, agora, agora);
        Usuario mariaAlterada = Usuario.create(7L, "Maria Souza", "maria.souza@exemplo.com", "maria.souza",
                SENHA_CODIFICADA, endereco, cliente, cpf, agora, agora);
        Usuario outra = Usuario.create(8L, "Maria Silva", "maria@exemplo.com", "maria.silva", SENHA_CODIFICADA,
                endereco, cliente, cpf, agora, agora);

        /* assert */
        assertThat(maria).isEqualTo(mariaAlterada).isNotEqualTo(outra);
    }

    @Test
    @DisplayName("TRO-01 · trocar para Dono de Restaurante com CNPJ troca o tipo e o documento")
    void deveTrocarParaDonoComCnpj() {
        /* arrange */
        Usuario usuario = Usuario.create(7L, "Maria Silva", "maria@exemplo.com", "maria.silva", SENHA_CODIFICADA,
                endereco, cliente, cpf, LocalDateTime.of(2026, 10, 1, 10, 0), LocalDateTime.of(2026, 10, 1, 10, 0));

        /* act */
        usuario.trocarTipo(dono, cnpj);

        /* assert */
        assertThat(usuario.getTipo()).isEqualTo(dono);
        assertThat(usuario.getDocumento()).isEqualTo(cnpj);
        assertThat(usuario.ehDonoDeRestaurante()).isTrue();
        assertThat(usuario.getId()).isEqualTo(7L);
        assertThat(usuario.getNome()).isEqualTo("Maria Silva");
        assertThat(usuario.getDataCriacao()).isEqualTo(LocalDateTime.of(2026, 10, 1, 10, 0));
    }

    @Test
    @DisplayName("TRO-02 · trocar para Dono de Restaurante informando CPF é recusado, e o usuário fica como estava")
    void deveRecusarTrocaParaDonoComCpf() {
        /* arrange */
        Usuario usuario = Usuario.create("Maria Silva", "maria@exemplo.com", "maria.silva", SENHA_CODIFICADA,
                endereco, cliente, cpf);

        /* act + assert */
        assertThatThrownBy(() -> usuario.trocarTipo(dono, Documento.cpf("52998224725")))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessage("Usuário do tipo Dono de Restaurante deve informar CNPJ.");
        assertThat(usuario.getTipo()).isEqualTo(cliente);
        assertThat(usuario.getDocumento()).isEqualTo(cpf);
    }

    @Test
    @DisplayName("TRO-03 · trocar para Cliente, ou para um tipo criado pelo CRUD, informando CNPJ é recusado")
    void deveRecusarTrocaParaOutrosTiposComCnpj() {
        /* arrange */
        Usuario usuario = Usuario.create("Ana Souza", "ana@exemplo.com", "ana.souza", SENHA_CODIFICADA,
                endereco, dono, cnpj);

        /* act + assert */
        assertThatThrownBy(() -> usuario.trocarTipo(cliente, cnpj))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessage("Usuário do tipo Cliente deve informar CPF.");
        assertThatThrownBy(() -> usuario.trocarTipo(entregador, cnpj))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessage("Usuário do tipo Entregador deve informar CPF.");
        assertThat(usuario.getTipo()).isEqualTo(dono);
    }

    @Test
    @DisplayName("TRO-03 · trocar para um tipo criado pelo CRUD com CPF é aceito")
    void deveTrocarParaTipoCriadoPeloCrud() {
        /* arrange */
        Usuario usuario = Usuario.create("Ana Souza", "ana@exemplo.com", "ana.souza", SENHA_CODIFICADA,
                endereco, dono, cnpj);

        /* act */
        usuario.trocarTipo(entregador, cpf);

        /* assert */
        assertThat(usuario.getTipo()).isEqualTo(entregador);
        assertThat(usuario.getDocumento()).isEqualTo(cpf);
    }

    @Test
    @DisplayName("TRO-07 · o usuário sabe dizer se já tem o tipo e o documento pedidos")
    void deveReconhecerOTipoEODocumentoAtuais() {
        /* arrange */
        Usuario usuario = Usuario.create("Maria Silva", "maria@exemplo.com", "maria.silva", SENHA_CODIFICADA,
                endereco, cliente, cpf);

        /* act + assert */
        assertThat(usuario.temTipoEDocumento(cliente, Documento.cpf("123.456.789-09"))).isTrue();
        assertThat(usuario.temTipoEDocumento(cliente, Documento.cpf("52998224725"))).isFalse();
        assertThat(usuario.temTipoEDocumento(entregador, cpf)).isFalse();
    }

    @Test
    @DisplayName("ENT-05 · nome ou e-mail com caractere de controle é recusado")
    void deveRecusarControleNoNomeENoEmail() {
        /* act + assert */
        assertThatThrownBy(() -> Usuario.create("Maria\u0000Silva", "maria@exemplo.com", "maria.silva",
                SENHA_CODIFICADA, endereco, cliente, cpf))
                .isInstanceOf(ValidacaoDeDominioException.class)
                .hasMessage("O campo nome não aceita quebra de linha nem caracteres de controle.");
        assertThatThrownBy(() -> Usuario.create("Maria Silva", "maria\u202E@exemplo.com", "maria.silva",
                SENHA_CODIFICADA, endereco, cliente, cpf))
                .hasMessage("O campo e-mail não aceita quebra de linha nem caracteres de controle.");
    }
}
