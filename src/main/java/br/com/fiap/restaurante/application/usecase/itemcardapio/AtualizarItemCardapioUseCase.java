package br.com.fiap.restaurante.application.usecase.itemcardapio;

import br.com.fiap.restaurante.application.dto.AtualizacaoDeItemCardapioDTO;
import br.com.fiap.restaurante.application.gateway.IItemCardapioGateway;
import br.com.fiap.restaurante.application.gateway.IRestauranteGateway;
import br.com.fiap.restaurante.application.port.ITransactionManager;
import br.com.fiap.restaurante.domain.entity.ItemCardapio;
import br.com.fiap.restaurante.domain.valueobject.Preco;

/**
 * Atualiza um item pela rota do restaurante dele. Manter o próprio nome é aceito;
 * usar o nome de outro item ativo do restaurante, não.
 */
public class AtualizarItemCardapioUseCase {

    private final IItemCardapioGateway itens;
    private final IRestauranteGateway restaurantes;
    private final ITransactionManager transacao;

    private AtualizarItemCardapioUseCase(IItemCardapioGateway itens, IRestauranteGateway restaurantes,
                                         ITransactionManager transacao) {
        this.itens = itens;
        this.restaurantes = restaurantes;
        this.transacao = transacao;
    }

    public static AtualizarItemCardapioUseCase create(IItemCardapioGateway itens, IRestauranteGateway restaurantes,
                                                      ITransactionManager transacao) {
        return new AtualizarItemCardapioUseCase(itens, restaurantes, transacao);
    }

    public ItemCardapio run(AtualizacaoDeItemCardapioDTO dados) {
        return transacao.executar(() -> {
            ItemCardapio item = BuscarItemCardapioUseCase.itemDoRestauranteParaAlterar(itens, restaurantes,
                    dados.restauranteId(), dados.itemId());
            item.setNome(dados.nome());
            item.setDescricao(dados.descricao());
            item.setPreco(new Preco(dados.preco()));
            item.setApenasNoLocal(dados.apenasNoLocal());
            item.setCaminhoFoto(dados.caminhoFoto());
            if (itens.existeNomeAtivoEmOutroItem(item.getRestauranteId(), item.getNome(), item.getId())) {
                throw BuscarItemCardapioUseCase.nomeEmUso(item.getRestauranteId(), item.getNome());
            }
            return itens.atualizar(item);
        });
    }
}
