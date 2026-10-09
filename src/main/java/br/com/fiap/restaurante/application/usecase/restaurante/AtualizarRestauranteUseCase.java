package br.com.fiap.restaurante.application.usecase.restaurante;

import br.com.fiap.restaurante.application.dto.AtualizacaoDeRestauranteDTO;
import br.com.fiap.restaurante.application.gateway.IRestauranteGateway;
import br.com.fiap.restaurante.application.gateway.IUsuarioGateway;
import br.com.fiap.restaurante.application.port.ITransactionManager;
import br.com.fiap.restaurante.domain.entity.Restaurante;
import br.com.fiap.restaurante.domain.enums.TipoCozinha;

/**
 * Atualiza um restaurante. Os turnos são substituídos por inteiro, e um dono
 * diferente transfere o restaurante, com as mesmas exigências do cadastro.
 */
public class AtualizarRestauranteUseCase {

    private final IRestauranteGateway restaurantes;
    private final IUsuarioGateway usuarios;
    private final ITransactionManager transacao;

    private AtualizarRestauranteUseCase(IRestauranteGateway restaurantes, IUsuarioGateway usuarios,
                                        ITransactionManager transacao) {
        this.restaurantes = restaurantes;
        this.usuarios = usuarios;
        this.transacao = transacao;
    }

    public static AtualizarRestauranteUseCase create(IRestauranteGateway restaurantes, IUsuarioGateway usuarios,
                                                     ITransactionManager transacao) {
        return new AtualizarRestauranteUseCase(restaurantes, usuarios, transacao);
    }

    public Restaurante run(AtualizacaoDeRestauranteDTO dados) {
        return transacao.executar(() -> {
            Restaurante restaurante = restaurantes.buscarPorIdParaAlterar(dados.id())
                    .orElseThrow(() -> BuscarRestaurantePorIdUseCase.restauranteNaoEncontrado(dados.id()));

            if (!restaurante.getDono().getId().equals(dados.donoId())) {
                restaurante.setDono(CadastrarRestauranteUseCase.donoValido(usuarios, dados.donoId(), "transferir"));
            }
            restaurante.setNome(dados.nome());
            restaurante.setEndereco(dados.endereco() == null ? null : dados.endereco().paraEndereco());
            restaurante.setTipoCozinha(TipoCozinha.de(dados.tipoCozinha()));
            restaurante.setHorarios(CadastrarRestauranteUseCase.quadro(dados.horarios()));
            return restaurantes.atualizar(restaurante);
        });
    }
}
