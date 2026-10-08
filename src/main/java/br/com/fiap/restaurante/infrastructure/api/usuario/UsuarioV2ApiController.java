package br.com.fiap.restaurante.infrastructure.api.usuario;

import br.com.fiap.restaurante.application.port.IPasswordEncoder;
import br.com.fiap.restaurante.application.port.ITransactionManager;
import br.com.fiap.restaurante.infrastructure.api.comum.Paginacao;
import br.com.fiap.restaurante.interfaceadapter.controller.UsuarioController;
import br.com.fiap.restaurante.interfaceadapter.datasource.ITipoUsuarioDataSource;
import br.com.fiap.restaurante.interfaceadapter.datasource.IUsuarioDataSource;
import br.com.fiap.restaurante.interfaceadapter.presenter.PaginaResponse;
import br.com.fiap.restaurante.interfaceadapter.presenter.UsuarioResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

/**
 * Busca paginada de usuários (v2). Só aceita ordenar pelos campos que a resposta
 * mostra.
 */
@RestController
@RequestMapping("/api/v2/usuarios")
public class UsuarioV2ApiController implements UsuarioV2Api {

    private static final Set<String> CAMPOS_ORDENAVEIS =
            Set.of("id", "nome", "email", "login", "dataCriacao", "dataUltimaAlteracao");

    private final UsuarioController controller;

    public UsuarioV2ApiController(IUsuarioDataSource usuarios, ITipoUsuarioDataSource tipos, IPasswordEncoder senhas,
                                  ITransactionManager transacao) {
        this.controller = UsuarioController.create(usuarios, tipos, senhas, transacao);
    }

    @Override
    @GetMapping
    public ResponseEntity<PaginaResponse<UsuarioResponse>> buscarPorNome(
            @RequestParam(required = false) String nome,
            @PageableDefault(sort = "nome") Pageable paginacao) {
        return ResponseEntity.ok(controller.buscarPorNomePaginado(nome, Paginacao.pedido(paginacao, CAMPOS_ORDENAVEIS)));
    }
}
