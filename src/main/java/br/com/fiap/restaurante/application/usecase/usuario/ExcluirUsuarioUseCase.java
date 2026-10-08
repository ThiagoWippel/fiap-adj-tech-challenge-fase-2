package br.com.fiap.restaurante.application.usecase.usuario;

import br.com.fiap.restaurante.application.gateway.IUsuarioGateway;
import br.com.fiap.restaurante.application.port.ITransactionManager;

/**
 * Exclui o usuário anonimizando o registro: os dados pessoais são apagados e a
 * linha fica, para não quebrar o histórico que aponta para ela.
 */
public class ExcluirUsuarioUseCase {

    private final IUsuarioGateway usuarios;
    private final ITransactionManager transacao;

    private ExcluirUsuarioUseCase(IUsuarioGateway usuarios, ITransactionManager transacao) {
        this.usuarios = usuarios;
        this.transacao = transacao;
    }

    public static ExcluirUsuarioUseCase create(IUsuarioGateway usuarios, ITransactionManager transacao) {
        return new ExcluirUsuarioUseCase(usuarios, transacao);
    }

    public void run(Long id) {
        transacao.executar(() -> {
            usuarios.buscarPorId(id).orElseThrow(() -> BuscarUsuarioPorIdUseCase.usuarioNaoEncontrado(id));
            usuarios.anonimizar(id);
        });
    }
}
