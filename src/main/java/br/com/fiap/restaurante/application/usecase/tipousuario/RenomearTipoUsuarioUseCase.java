package br.com.fiap.restaurante.application.usecase.tipousuario;

import br.com.fiap.restaurante.application.dto.RenomeacaoDeTipoUsuarioDTO;
import br.com.fiap.restaurante.application.exception.ConflitoDeDadosException;
import br.com.fiap.restaurante.application.gateway.ITipoUsuarioGateway;
import br.com.fiap.restaurante.application.port.ITransactionManager;
import br.com.fiap.restaurante.domain.entity.TipoUsuario;

/**
 * Renomeia um tipo de usuário, inclusive os de sistema. O código não muda, então
 * os usuários e as regras que dependem dele não são afetados.
 */
public class RenomearTipoUsuarioUseCase {

    private final ITipoUsuarioGateway tipos;
    private final ITransactionManager transacao;

    private RenomearTipoUsuarioUseCase(ITipoUsuarioGateway tipos, ITransactionManager transacao) {
        this.tipos = tipos;
        this.transacao = transacao;
    }

    public static RenomearTipoUsuarioUseCase create(ITipoUsuarioGateway tipos, ITransactionManager transacao) {
        return new RenomearTipoUsuarioUseCase(tipos, transacao);
    }

    public TipoUsuario run(RenomeacaoDeTipoUsuarioDTO dados) {
        return transacao.executar(() -> {
            TipoUsuario tipo = tipos.buscarPorIdParaAlterar(dados.id())
                    .orElseThrow(() -> BuscarTipoUsuarioPorIdUseCase.tipoNaoEncontrado(dados.id()));
            tipo.setNome(dados.nome());
            if (tipos.existeNomeEmOutroTipo(tipo.getNome(), tipo.getId())) {
                throw nomeEmUso(tipo.getNome());
            }
            return tipos.atualizar(tipo);
        });
    }

    static ConflitoDeDadosException nomeEmUso(String nome) {
        return new ConflitoDeDadosException("Já existe um tipo de usuário com o nome " + nome + ".");
    }
}
