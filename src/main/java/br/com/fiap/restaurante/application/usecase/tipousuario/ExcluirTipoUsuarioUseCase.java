package br.com.fiap.restaurante.application.usecase.tipousuario;

import br.com.fiap.restaurante.application.exception.ConflitoDeDadosException;
import br.com.fiap.restaurante.application.gateway.ITipoUsuarioGateway;
import br.com.fiap.restaurante.application.gateway.IUsuarioGateway;
import br.com.fiap.restaurante.application.port.ITransactionManager;
import br.com.fiap.restaurante.domain.entity.TipoUsuario;

/**
 * Exclui um tipo de usuário de vez. Os tipos de sistema e os tipos usados por
 * algum usuário ativo não podem ser excluídos. Usuários removidos não contam: a
 * anonimização já apagou o tipo deles.
 */
public class ExcluirTipoUsuarioUseCase {

    private final ITipoUsuarioGateway tipos;
    private final IUsuarioGateway usuarios;
    private final ITransactionManager transacao;

    private ExcluirTipoUsuarioUseCase(ITipoUsuarioGateway tipos, IUsuarioGateway usuarios,
                                      ITransactionManager transacao) {
        this.tipos = tipos;
        this.usuarios = usuarios;
        this.transacao = transacao;
    }

    public static ExcluirTipoUsuarioUseCase create(ITipoUsuarioGateway tipos, IUsuarioGateway usuarios,
                                                   ITransactionManager transacao) {
        return new ExcluirTipoUsuarioUseCase(tipos, usuarios, transacao);
    }

    public void run(Long id) {
        transacao.executar(() -> {
            TipoUsuario tipo = tipos.buscarPorIdParaAlterar(id)
                    .orElseThrow(() -> BuscarTipoUsuarioPorIdUseCase.tipoNaoEncontrado(id));
            if (tipo.ehDeSistema()) {
                throw new ConflitoDeDadosException(
                        "O tipo " + tipo.getNome() + " é um tipo de sistema e não pode ser excluído.");
            }
            long emUso = usuarios.contarAtivosPorTipo(id);
            if (emUso > 0) {
                throw new ConflitoDeDadosException("O tipo " + tipo.getNome() + " não pode ser excluído: "
                        + (emUso == 1 ? "1 usuário ativo o usa." : emUso + " usuários ativos o usam."));
            }
            tipos.excluir(id);
        });
    }
}
