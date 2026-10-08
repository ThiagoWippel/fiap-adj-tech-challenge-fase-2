package br.com.fiap.restaurante.infrastructure.api.itemcardapio;

import br.com.fiap.restaurante.application.port.ITransactionManager;
import br.com.fiap.restaurante.infrastructure.api.comum.CamposOrdenaveis;
import br.com.fiap.restaurante.infrastructure.api.comum.Paginacao;
import br.com.fiap.restaurante.interfaceadapter.controller.ItemCardapioController;
import br.com.fiap.restaurante.interfaceadapter.datasource.IItemCardapioDataSource;
import br.com.fiap.restaurante.interfaceadapter.datasource.IRestauranteDataSource;
import br.com.fiap.restaurante.interfaceadapter.presenter.ItemCardapioResponse;
import br.com.fiap.restaurante.interfaceadapter.presenter.PaginaResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

/**
 * Endpoints de item do cardápio, aninhados na rota do restaurante. Valida o corpo
 * das requisições e repassa ao controller de itens; a documentação OpenAPI fica
 * em {@link ItemCardapioApi}.
 */
@RestController
@RequestMapping("/api/v1/restaurantes/{restauranteId}/itens-cardapio")
public class ItemCardapioApiController implements ItemCardapioApi {

    private final ItemCardapioController controller;

    public ItemCardapioApiController(IItemCardapioDataSource itens, IRestauranteDataSource restaurantes,
                                     ITransactionManager transacao) {
        this.controller = ItemCardapioController.create(itens, restaurantes, transacao);
    }

    @Override
    @PostMapping
    public ResponseEntity<ItemCardapioResponse> cadastrar(@PathVariable Long restauranteId,
                                                          @Valid @RequestBody ItemCardapioRequest requisicao,
                                                          UriComponentsBuilder uri) {
        ItemCardapioResponse item = controller.cadastrar(requisicao.paraNovoDTO(restauranteId));
        URI local = uri.path("/api/v1/restaurantes/{restauranteId}/itens-cardapio/{itemId}")
                .buildAndExpand(restauranteId, item.id()).toUri();
        return ResponseEntity.created(local).body(item);
    }

    @Override
    @GetMapping
    public ResponseEntity<PaginaResponse<ItemCardapioResponse>> listar(
            @PathVariable Long restauranteId,
            @RequestParam(required = false) Boolean apenasNoLocal,
            @PageableDefault(sort = "nome") Pageable paginacao) {
        return ResponseEntity.ok(controller.listar(restauranteId, apenasNoLocal,
                Paginacao.pedido(paginacao, CamposOrdenaveis.ITEM_CARDAPIO)));
    }

    @Override
    @GetMapping("/{itemId}")
    public ResponseEntity<ItemCardapioResponse> buscarPorId(@PathVariable Long restauranteId,
                                                            @PathVariable Long itemId) {
        return ResponseEntity.ok(controller.buscarPorId(restauranteId, itemId));
    }

    @Override
    @PutMapping("/{itemId}")
    public ResponseEntity<ItemCardapioResponse> atualizar(@PathVariable Long restauranteId,
                                                          @PathVariable Long itemId,
                                                          @Valid @RequestBody ItemCardapioRequest requisicao) {
        return ResponseEntity.ok(controller.atualizar(requisicao.paraAtualizacaoDTO(restauranteId, itemId)));
    }

    @Override
    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> excluir(@PathVariable Long restauranteId, @PathVariable Long itemId) {
        controller.excluir(restauranteId, itemId);
        return ResponseEntity.noContent().build();
    }
}
