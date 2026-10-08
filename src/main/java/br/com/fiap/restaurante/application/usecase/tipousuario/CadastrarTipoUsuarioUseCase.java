package br.com.fiap.restaurante.application.usecase.tipousuario;

import br.com.fiap.restaurante.application.exception.ConflitoDeDadosException;
import br.com.fiap.restaurante.application.gateway.ITipoUsuarioGateway;
import br.com.fiap.restaurante.application.port.ITransactionManager;
import br.com.fiap.restaurante.domain.entity.TipoUsuario;

/**
 * Cadastra um tipo de usuário a partir do nome. O código é gerado pela entidade e
 * precisa ser único, assim como o nome.
 */
public class CadastrarTipoUsuarioUseCase {

    private final ITipoUsuarioGateway tipos;
    private final ITransactionManager transacao;

    private CadastrarTipoUsuarioUseCase(ITipoUsuarioGateway tipos, ITransactionManager transacao) {
        this.tipos = tipos;
        this.transacao = transacao;
    }

    public static CadastrarTipoUsuarioUseCase create(ITipoUsuarioGateway tipos, ITransactionManager transacao) {
        return new CadastrarTipoUsuarioUseCase(tipos, transacao);
    }

    public TipoUsuario run(String nome) {
        return transacao.executar(() -> {
            TipoUsuario tipo = TipoUsuario.create(nome);
            if (tipos.existeNome(tipo.getNome())) {
                throw RenomearTipoUsuarioUseCase.nomeEmUso(tipo.getNome());
            }
            // Nomes diferentes podem gerar o mesmo código, como "Ajudante de Cozinha" e "Ajudante-de-Cozinha"
            tipos.buscarPorCodigo(tipo.getCodigo()).ifPresent(existente -> {
                throw new ConflitoDeDadosException("O nome " + tipo.getNome() + " gera o código " + tipo.getCodigo()
                        + ", que já pertence ao tipo " + existente.getNome() + ".");
            });
            return tipos.incluir(tipo);
        });
    }
}
