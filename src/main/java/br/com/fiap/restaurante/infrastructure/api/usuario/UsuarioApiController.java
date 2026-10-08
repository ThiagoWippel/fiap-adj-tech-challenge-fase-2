package br.com.fiap.restaurante.infrastructure.api.usuario;

import br.com.fiap.restaurante.application.port.IPasswordEncoder;
import br.com.fiap.restaurante.application.port.ITransactionManager;
import br.com.fiap.restaurante.infrastructure.api.comum.CamposOrdenaveis;
import br.com.fiap.restaurante.infrastructure.api.comum.Paginacao;
import br.com.fiap.restaurante.interfaceadapter.controller.UsuarioController;
import br.com.fiap.restaurante.interfaceadapter.datasource.IRestauranteDataSource;
import br.com.fiap.restaurante.interfaceadapter.datasource.ITipoUsuarioDataSource;
import br.com.fiap.restaurante.interfaceadapter.datasource.IUsuarioDataSource;
import br.com.fiap.restaurante.interfaceadapter.presenter.PaginaResponse;
import br.com.fiap.restaurante.interfaceadapter.presenter.RestauranteResponse;
import br.com.fiap.restaurante.interfaceadapter.presenter.UsuarioResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Endpoints de usuário da v1. Valida o corpo das requisições e repassa ao
 * controller de usuário; a documentação OpenAPI fica em {@link UsuarioApi}.
 */
@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioApiController implements UsuarioApi {

    private final UsuarioController controller;

    public UsuarioApiController(IUsuarioDataSource usuarios, ITipoUsuarioDataSource tipos,
                                IRestauranteDataSource restaurantes, IPasswordEncoder senhas,
                                ITransactionManager transacao) {
        this.controller = UsuarioController.create(usuarios, tipos, restaurantes, senhas, transacao);
    }

    @Override
    @PostMapping
    public ResponseEntity<UsuarioResponse> cadastrar(@Valid @RequestBody CriarUsuarioRequest requisicao,
                                                     UriComponentsBuilder uri) {
        UsuarioResponse usuario = controller.cadastrar(requisicao.paraDTO());
        URI local = uri.path("/api/v1/usuarios/{id}").buildAndExpand(usuario.id()).toUri();
        return ResponseEntity.created(local).body(usuario);
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(controller.buscarPorId(id));
    }

    @Override
    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> buscarPorNome(@RequestParam(required = false) String nome) {
        return ResponseEntity.ok(controller.buscarPorNome(nome));
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> atualizar(@PathVariable Long id,
                                                     @Valid @RequestBody AtualizarUsuarioRequest requisicao) {
        return ResponseEntity.ok(controller.atualizar(requisicao.paraDTO(id)));
    }

    @Override
    @PutMapping("/{id}/senha")
    public ResponseEntity<Void> trocarSenha(@PathVariable Long id, @Valid @RequestBody TrocarSenhaRequest requisicao) {
        controller.trocarSenha(requisicao.paraDTO(id));
        return ResponseEntity.noContent().build();
    }

    @Override
    @PatchMapping("/{id}/tipo")
    public ResponseEntity<UsuarioResponse> trocarTipo(@PathVariable Long id,
                                                      @Valid @RequestBody TrocarTipoRequest requisicao) {
        return ResponseEntity.ok(controller.trocarTipo(requisicao.paraDTO(id)));
    }

    @Override
    @GetMapping("/{id}/restaurantes")
    public ResponseEntity<PaginaResponse<RestauranteResponse>> listarRestaurantes(
            @PathVariable Long id, @PageableDefault(sort = "nome") Pageable paginacao) {
        return ResponseEntity.ok(controller.listarRestaurantes(id,
                Paginacao.pedido(paginacao, CamposOrdenaveis.RESTAURANTE)));
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        controller.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
