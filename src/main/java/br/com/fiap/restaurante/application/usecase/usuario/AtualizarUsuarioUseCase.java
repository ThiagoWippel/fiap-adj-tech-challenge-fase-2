package br.com.fiap.restaurante.application.usecase.usuario;

import br.com.fiap.restaurante.application.dto.AtualizacaoDeUsuarioDTO;
import br.com.fiap.restaurante.application.exception.ConflitoDeDadosException;
import br.com.fiap.restaurante.application.gateway.IUsuarioGateway;
import br.com.fiap.restaurante.application.port.ITransactionManager;
import br.com.fiap.restaurante.domain.entity.Usuario;

/**
 * Atualiza nome, e-mail, login e endereço. Senha e tipo têm operações próprias.
 */
public class AtualizarUsuarioUseCase {

    private final IUsuarioGateway usuarios;
    private final ITransactionManager transacao;

    private AtualizarUsuarioUseCase(IUsuarioGateway usuarios, ITransactionManager transacao) {
        this.usuarios = usuarios;
        this.transacao = transacao;
    }

    public static AtualizarUsuarioUseCase create(IUsuarioGateway usuarios, ITransactionManager transacao) {
        return new AtualizarUsuarioUseCase(usuarios, transacao);
    }

    public Usuario run(AtualizacaoDeUsuarioDTO dados) {
        return transacao.executar(() -> {
            Usuario usuario = usuarios.buscarPorId(dados.id())
                    .orElseThrow(() -> BuscarUsuarioPorIdUseCase.usuarioNaoEncontrado(dados.id()));

            if (usuarios.existeEmailEmOutroUsuario(dados.email(), dados.id())) {
                throw new ConflitoDeDadosException("O e-mail informado já está cadastrado.");
            }
            if (usuarios.existeLoginEmOutroUsuario(dados.login(), dados.id())) {
                throw new ConflitoDeDadosException("O login informado já está cadastrado.");
            }

            usuario.setNome(dados.nome());
            usuario.setEmail(dados.email());
            usuario.setLogin(dados.login());
            usuario.setEndereco(dados.endereco() == null ? null : dados.endereco().paraEndereco());
            return usuarios.atualizar(usuario);
        });
    }
}
