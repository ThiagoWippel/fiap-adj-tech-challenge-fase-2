package br.com.fiap.restaurante.application.usecase.itemcardapio;

import br.com.fiap.restaurante.application.dto.NovoItemCardapioDTO;
import br.com.fiap.restaurante.application.gateway.IItemCardapioGateway;
import br.com.fiap.restaurante.application.gateway.IRestauranteGateway;
import br.com.fiap.restaurante.application.port.ITransactionManager;
import br.com.fiap.restaurante.domain.entity.ItemCardapio;
import br.com.fiap.restaurante.domain.valueobject.Preco;

/**
 * Cadastra um item no cardápio de um restaurante ativo. O nome é único entre os
 * itens ativos do restaurante; em outro restaurante, o mesmo nome é aceito.
 */
public class CadastrarItemCardapioUseCase {

    private final IItemCardapioGateway itens;
    private final IRestauranteGateway restaurantes;
    private final ITransactionManager transacao;

    private CadastrarItemCardapioUseCase(IItemCardapioGateway itens, IRestauranteGateway restaurantes,
                                         ITransactionManager transacao) {
        this.itens = itens;
        this.restaurantes = restaurantes;
        this.transacao = transacao;
    }

    public static CadastrarItemCardapioUseCase create(IItemCardapioGateway itens, IRestauranteGateway restaurantes,
                                                      ITransactionManager transacao) {
        return new CadastrarItemCardapioUseCase(itens, restaurantes, transacao);
    }

    public ItemCardapio run(NovoItemCardapioDTO dados) {
        return transacao.executar(() -> {
            BuscarItemCardapioUseCase.garantirRestauranteAtivo(restaurantes, dados.restauranteId());
            ItemCardapio item = ItemCardapio.create(dados.restauranteId(), dados.nome(), dados.descricao(),
                    new Preco(dados.preco()), dados.apenasNoLocal(), dados.caminhoFoto());
            if (itens.existeNomeAtivo(item.getRestauranteId(), item.getNome())) {
                throw BuscarItemCardapioUseCase.nomeEmUso(item.getRestauranteId(), item.getNome());
            }
            return itens.incluir(item);
        });
    }
}
