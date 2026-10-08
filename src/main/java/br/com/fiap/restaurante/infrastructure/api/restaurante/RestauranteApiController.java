package br.com.fiap.restaurante.infrastructure.api.restaurante;

import br.com.fiap.restaurante.application.dto.FiltroDeRestaurantes;
import br.com.fiap.restaurante.application.port.ITransactionManager;
import br.com.fiap.restaurante.infrastructure.api.comum.CamposOrdenaveis;
import br.com.fiap.restaurante.infrastructure.api.comum.Paginacao;
import br.com.fiap.restaurante.interfaceadapter.controller.RestauranteController;
import br.com.fiap.restaurante.interfaceadapter.datasource.IRestauranteDataSource;
import br.com.fiap.restaurante.interfaceadapter.datasource.IUsuarioDataSource;
import br.com.fiap.restaurante.interfaceadapter.presenter.PaginaResponse;
import br.com.fiap.restaurante.interfaceadapter.presenter.RestauranteResponse;
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
 * Endpoints de restaurante. Valida o corpo das requisições e repassa ao
 * controller de restaurantes; a documentação OpenAPI fica em
 * {@link RestauranteApi}.
 */
@RestController
@RequestMapping("/api/v1/restaurantes")
public class RestauranteApiController implements RestauranteApi {

    private final RestauranteController controller;

    public RestauranteApiController(IRestauranteDataSource restaurantes, IUsuarioDataSource usuarios,
                                    ITransactionManager transacao) {
        this.controller = RestauranteController.create(restaurantes, usuarios, transacao);
    }

    @Override
    @PostMapping
    public ResponseEntity<RestauranteResponse> cadastrar(@Valid @RequestBody RestauranteRequest requisicao,
                                                         UriComponentsBuilder uri) {
        RestauranteResponse restaurante = controller.cadastrar(requisicao.paraNovoDTO());
        URI local = uri.path("/api/v1/restaurantes/{id}").buildAndExpand(restaurante.id()).toUri();
        return ResponseEntity.created(local).body(restaurante);
    }

    @Override
    @GetMapping
    public ResponseEntity<PaginaResponse<RestauranteResponse>> listar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String tipoCozinha,
            @PageableDefault(sort = "nome") Pageable paginacao) {
        return ResponseEntity.ok(controller.listar(new FiltroDeRestaurantes(nome, tipoCozinha),
                Paginacao.pedido(paginacao, CamposOrdenaveis.RESTAURANTE)));
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<RestauranteResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(controller.buscarPorId(id));
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<RestauranteResponse> atualizar(@PathVariable Long id,
                                                         @Valid @RequestBody RestauranteRequest requisicao) {
        return ResponseEntity.ok(controller.atualizar(requisicao.paraAtualizacaoDTO(id)));
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        controller.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
