package br.com.fiap.restaurante.interfaceadapter.gateway;

import br.com.fiap.restaurante.application.dto.Pagina;
import br.com.fiap.restaurante.application.dto.PedidoDePagina;
import br.com.fiap.restaurante.application.gateway.IItemCardapioGateway;
import br.com.fiap.restaurante.domain.entity.ItemCardapio;
import br.com.fiap.restaurante.domain.valueobject.Preco;
import br.com.fiap.restaurante.interfaceadapter.datasource.DadosItemCardapio;
import br.com.fiap.restaurante.interfaceadapter.datasource.IItemCardapioDataSource;

import java.util.Optional;

/**
 * Gateway de itens do cardápio: converte entre a entidade de domínio e os dados
 * da origem de dados.
 */
public class ItemCardapioGateway implements IItemCardapioGateway {

    private final IItemCardapioDataSource dataSource;

    private ItemCardapioGateway(IItemCardapioDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static ItemCardapioGateway create(IItemCardapioDataSource dataSource) {
        return new ItemCardapioGateway(dataSource);
    }

    @Override
    public ItemCardapio incluir(ItemCardapio item) {
        return paraItem(dataSource.incluir(paraDados(item)));
    }

    @Override
    public ItemCardapio atualizar(ItemCardapio item) {
        return paraItem(dataSource.atualizar(paraDados(item)));
    }

    @Override
    public Optional<ItemCardapio> buscarPorId(Long id) {
        return dataSource.buscarPorId(id).map(ItemCardapioGateway::paraItem);
    }

    @Override
    public Pagina<ItemCardapio> listar(Long restauranteId, Boolean apenasNoLocal, PedidoDePagina pedido) {
        return dataSource.listar(restauranteId, apenasNoLocal, pedido).map(ItemCardapioGateway::paraItem);
    }

    @Override
    public boolean existeNomeAtivo(Long restauranteId, String nome) {
        return dataSource.existeNomeAtivo(restauranteId, nome);
    }

    @Override
    public boolean existeNomeAtivoEmOutroItem(Long restauranteId, String nome, Long itemId) {
        return dataSource.existeNomeAtivoEmOutroItem(restauranteId, nome, itemId);
    }

    @Override
    public void remover(Long id) {
        dataSource.remover(id);
    }

    private static DadosItemCardapio paraDados(ItemCardapio item) {
        return new DadosItemCardapio(item.getId(), item.getRestauranteId(), item.getNome(), item.getDescricao(),
                item.getPreco().valor(), item.getApenasNoLocal(), item.getCaminhoFoto(), item.getDataCriacao(),
                item.getDataUltimaAlteracao());
    }

    private static ItemCardapio paraItem(DadosItemCardapio dados) {
        return ItemCardapio.create(dados.id(), dados.restauranteId(), dados.nome(), dados.descricao(),
                new Preco(dados.preco()), dados.apenasNoLocal(), dados.caminhoFoto(), dados.dataCriacao(),
                dados.dataUltimaAlteracao());
    }
}
