package br.com.fiap.restaurante.infrastructure.api.tipousuario;

import br.com.fiap.restaurante.application.dto.RenomeacaoDeTipoUsuarioDTO;
import br.com.fiap.restaurante.application.port.ITransactionManager;
import br.com.fiap.restaurante.infrastructure.api.comum.CamposOrdenaveis;
import br.com.fiap.restaurante.infrastructure.api.comum.Paginacao;
import br.com.fiap.restaurante.interfaceadapter.controller.TipoUsuarioController;
import br.com.fiap.restaurante.interfaceadapter.datasource.ITipoUsuarioDataSource;
import br.com.fiap.restaurante.interfaceadapter.datasource.IUsuarioDataSource;
import br.com.fiap.restaurante.interfaceadapter.presenter.PaginaResponse;
import br.com.fiap.restaurante.interfaceadapter.presenter.TipoUsuarioResponse;
import br.com.fiap.restaurante.interfaceadapter.presenter.UsuarioResponse;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

/**
 * Endpoints de tipo de usuário. Valida o corpo das requisições e repassa ao
 * controller de tipos; a documentação OpenAPI fica em {@link TipoUsuarioApi}.
 */
@RestController
@RequestMapping("/api/v1/tipos-usuario")
public class TipoUsuarioApiController implements TipoUsuarioApi {

    private final TipoUsuarioController controller;

    public TipoUsuarioApiController(ITipoUsuarioDataSource tipos, IUsuarioDataSource usuarios,
                                    ITransactionManager transacao) {
        this.controller = TipoUsuarioController.create(tipos, usuarios, transacao);
    }

    @Override
    @PostMapping
    public ResponseEntity<TipoUsuarioResponse> cadastrar(@Valid @RequestBody TipoUsuarioRequest requisicao,
                                                         UriComponentsBuilder uri) {
        TipoUsuarioResponse tipo = controller.cadastrar(requisicao.nome());
        URI local = uri.path("/api/v1/tipos-usuario/{id}").buildAndExpand(tipo.id()).toUri();
        return ResponseEntity.created(local).body(tipo);
    }

    @Override
    @GetMapping
    public ResponseEntity<PaginaResponse<TipoUsuarioResponse>> listar(
            @PageableDefault(sort = "nome") Pageable paginacao) {
        return ResponseEntity.ok(controller.listar(Paginacao.pedido(paginacao, CamposOrdenaveis.TIPO_USUARIO)));
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<TipoUsuarioResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(controller.buscarPorId(id));
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<TipoUsuarioResponse> renomear(@PathVariable Long id,
                                                        @Valid @RequestBody TipoUsuarioRequest requisicao) {
        return ResponseEntity.ok(controller.renomear(new RenomeacaoDeTipoUsuarioDTO(id, requisicao.nome())));
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        controller.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/{id}/usuarios")
    public ResponseEntity<PaginaResponse<UsuarioResponse>> listarUsuarios(
            @PathVariable Long id, @PageableDefault(sort = "nome") Pageable paginacao) {
        return ResponseEntity.ok(controller.listarUsuarios(id, Paginacao.pedido(paginacao, CamposOrdenaveis.USUARIO)));
    }
}
