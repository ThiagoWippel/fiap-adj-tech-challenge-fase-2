package br.com.fiap.restaurante.application.usecase.usuario;

import br.com.fiap.restaurante.application.exception.ConflitoDeDadosException;
import br.com.fiap.restaurante.application.gateway.IRestauranteGateway;
import br.com.fiap.restaurante.application.gateway.IUsuarioGateway;
import br.com.fiap.restaurante.application.port.ITransactionManager;

/**
 * Exclui o usuário anonimizando o registro: os dados pessoais são apagados e a
 * linha fica, para não quebrar o histórico que aponta para ela. Quem é
 * responsável por restaurante ativo precisa transferir ou excluir o restaurante
 * antes.
 */
public class ExcluirUsuarioUseCase {

    private final IUsuarioGateway usuarios;
    private final IRestauranteGateway restaurantes;
    private final ITransactionManager transacao;

    private ExcluirUsuarioUseCase(IUsuarioGateway usuarios, IRestauranteGateway restaurantes,
                                  ITransactionManager transacao) {
        this.usuarios = usuarios;
        this.restaurantes = restaurantes;
        this.transacao = transacao;
    }

    public static ExcluirUsuarioUseCase create(IUsuarioGateway usuarios, IRestauranteGateway restaurantes,
                                               ITransactionManager transacao) {
        return new ExcluirUsuarioUseCase(usuarios, restaurantes, transacao);
    }

    public void run(Long id) {
        transacao.executar(() -> {
            usuarios.buscarPorId(id).orElseThrow(() -> BuscarUsuarioPorIdUseCase.usuarioNaoEncontrado(id));
            long ativos = restaurantes.contarAtivosPorDono(id);
            if (ativos > 0) {
                throw new ConflitoDeDadosException("O usuário " + id + " é responsável por " + restaurantesAtivos(ativos)
                        + ". Transfira ou exclua " + (ativos == 1 ? "o restaurante" : "os restaurantes")
                        + " antes de excluir o usuário.");
            }
            usuarios.anonimizar(id);
        });
    }

    static String restaurantesAtivos(long quantidade) {
        return quantidade == 1 ? "1 restaurante ativo" : quantidade + " restaurantes ativos";
    }
}
