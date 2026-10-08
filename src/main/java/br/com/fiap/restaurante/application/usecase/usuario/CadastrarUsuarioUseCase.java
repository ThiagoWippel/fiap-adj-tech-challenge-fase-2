package br.com.fiap.restaurante.application.usecase.usuario;

import br.com.fiap.restaurante.application.dto.NovoUsuarioDTO;
import br.com.fiap.restaurante.application.exception.ConflitoDeDadosException;
import br.com.fiap.restaurante.application.gateway.ITipoUsuarioGateway;
import br.com.fiap.restaurante.application.gateway.IUsuarioGateway;
import br.com.fiap.restaurante.application.port.IPasswordEncoder;
import br.com.fiap.restaurante.application.port.ITransactionManager;
import br.com.fiap.restaurante.application.usecase.tipousuario.BuscarTipoUsuarioPorIdUseCase;
import br.com.fiap.restaurante.domain.entity.TipoUsuario;
import br.com.fiap.restaurante.domain.entity.Usuario;
import br.com.fiap.restaurante.domain.valueobject.Documento;
import br.com.fiap.restaurante.domain.valueobject.Endereco;
import br.com.fiap.restaurante.domain.valueobject.SenhaEmTexto;

/**
 * Cadastra um usuário. Confere se o tipo existe e se e-mail, login e documento
 * estão livres; a senha é gravada codificada, nunca em texto.
 */
public class CadastrarUsuarioUseCase {

    private final IUsuarioGateway usuarios;
    private final ITipoUsuarioGateway tipos;
    private final IPasswordEncoder senhas;
    private final ITransactionManager transacao;

    private CadastrarUsuarioUseCase(IUsuarioGateway usuarios, ITipoUsuarioGateway tipos, IPasswordEncoder senhas,
                                    ITransactionManager transacao) {
        this.usuarios = usuarios;
        this.tipos = tipos;
        this.senhas = senhas;
        this.transacao = transacao;
    }

    public static CadastrarUsuarioUseCase create(IUsuarioGateway usuarios, ITipoUsuarioGateway tipos,
                                                 IPasswordEncoder senhas, ITransactionManager transacao) {
        return new CadastrarUsuarioUseCase(usuarios, tipos, senhas, transacao);
    }

    public Usuario run(NovoUsuarioDTO dados) {
        return transacao.executar(() -> {
            TipoUsuario tipo = tipos.buscarPorCodigo(dados.tipo())
                    .orElseThrow(() -> BuscarTipoUsuarioPorIdUseCase.tipoNaoEncontrado(dados.tipo()));
            Documento documento = documentoExigidoPelo(tipo, dados);
            Endereco endereco = dados.endereco() == null ? null : dados.endereco().paraEndereco();
            SenhaEmTexto senha = new SenhaEmTexto(dados.senha());

            garantirQueNaoEstaoEmUso(dados.email(), dados.login(), documento);

            Usuario usuario = Usuario.create(dados.nome(), dados.email(), dados.login(),
                    senhas.codificar(senha.valor()), endereco, tipo, documento);
            return usuarios.incluir(usuario);
        });
    }

    // Só o documento do tipo é considerado; a ausência dele é tratada pela entidade.
    private static Documento documentoExigidoPelo(TipoUsuario tipo, NovoUsuarioDTO dados) {
        if (tipo.ehDonoDeRestaurante()) {
            return dados.cnpj() == null ? null : Documento.cnpj(dados.cnpj());
        }
        return dados.cpf() == null ? null : Documento.cpf(dados.cpf());
    }

    private void garantirQueNaoEstaoEmUso(String email, String login, Documento documento) {
        if (usuarios.existeEmail(email)) {
            throw new ConflitoDeDadosException("O e-mail informado já está cadastrado.");
        }
        if (usuarios.existeLogin(login)) {
            throw new ConflitoDeDadosException("O login informado já está cadastrado.");
        }
        if (documento != null && usuarios.existeDocumento(documento.numero())) {
            throw new ConflitoDeDadosException("O documento informado já está cadastrado.");
        }
    }
}
