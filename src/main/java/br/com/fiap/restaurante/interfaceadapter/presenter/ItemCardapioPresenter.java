package br.com.fiap.restaurante.interfaceadapter.presenter;

import br.com.fiap.restaurante.application.dto.Pagina;
import br.com.fiap.restaurante.domain.entity.ItemCardapio;

/**
 * Converte itens do cardápio na resposta da API.
 */
public final class ItemCardapioPresenter {

    private ItemCardapioPresenter() {
    }

    public static ItemCardapioResponse paraResposta(ItemCardapio item) {
        return new ItemCardapioResponse(item.getId(), item.getRestauranteId(), item.getNome(), item.getDescricao(),
                item.getPreco().valor(), item.getApenasNoLocal(), item.getCaminhoFoto(),
                UsuarioPresenter.emSegundos(item.getDataCriacao()),
                UsuarioPresenter.emSegundos(item.getDataUltimaAlteracao()));
    }

    public static PaginaResponse<ItemCardapioResponse> paraPagina(Pagina<ItemCardapio> pagina) {
        return new PaginaResponse<>(pagina.conteudo().stream().map(ItemCardapioPresenter::paraResposta).toList(),
                pagina.numero(), pagina.tamanho(), pagina.totalElementos(), pagina.totalPaginas(), pagina.ultima());
    }
}
