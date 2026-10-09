package br.com.fiap.restaurante.application.usecase.usuario;

import br.com.fiap.restaurante.application.dto.TrocaDeSenhaDTO;
import br.com.fiap.restaurante.application.exception.CredenciaisInvalidasException;
import br.com.fiap.restaurante.application.gateway.IUsuarioGateway;
import br.com.fiap.restaurante.application.port.IPasswordEncoder;
import br.com.fiap.restaurante.application.port.ITransactionManager;
import br.com.fiap.restaurante.domain.entity.Usuario;
import br.com.fiap.restaurante.domain.valueobject.SenhaEmTexto;

/**
 * Troca a senha do usuário, depois de conferir a senha atual.
 */
public class TrocarSenhaUseCase {

    private final IUsuarioGateway usuarios;
    private final IPasswordEncoder senhas;
    private final ITransactionManager transacao;

    private TrocarSenhaUseCase(IUsuarioGateway usuarios, IPasswordEncoder senhas, ITransactionManager transacao) {
        this.usuarios = usuarios;
        this.senhas = senhas;
        this.transacao = transacao;
    }

    public static TrocarSenhaUseCase create(IUsuarioGateway usuarios, IPasswordEncoder senhas,
                                            ITransactionManager transacao) {
        return new TrocarSenhaUseCase(usuarios, senhas, transacao);
    }

    public void run(TrocaDeSenhaDTO dados) {
        transacao.executar(() -> {
            Usuario usuario = usuarios.buscarPorIdParaAlterar(dados.id())
                    .orElseThrow(() -> BuscarUsuarioPorIdUseCase.usuarioNaoEncontrado(dados.id()));

            if (!senhas.confere(dados.senhaAtual(), usuario.getSenha())) {
                throw new CredenciaisInvalidasException("A senha atual informada está incorreta.");
            }

            SenhaEmTexto novaSenha = new SenhaEmTexto(dados.novaSenha());
            usuario.setSenha(senhas.codificar(novaSenha.valor()));
            usuarios.atualizar(usuario);
        });
    }
}
