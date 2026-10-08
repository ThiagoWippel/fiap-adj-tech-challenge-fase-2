package br.com.fiap.restaurante.infrastructure.api.autenticacao;

import br.com.fiap.restaurante.application.port.IPasswordEncoder;
import br.com.fiap.restaurante.application.port.ITokenGenerator;
import br.com.fiap.restaurante.interfaceadapter.controller.AutenticacaoController;
import br.com.fiap.restaurante.interfaceadapter.datasource.IUsuarioDataSource;
import br.com.fiap.restaurante.interfaceadapter.presenter.LoginResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint de login. Valida o corpo da requisição e repassa ao controller de
 * autenticação; a documentação OpenAPI fica em {@link AutenticacaoApi}.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AutenticacaoApiController implements AutenticacaoApi {

    private final AutenticacaoController controller;

    public AutenticacaoApiController(IUsuarioDataSource usuarios, IPasswordEncoder senhas, ITokenGenerator tokens) {
        this.controller = AutenticacaoController.create(usuarios, senhas, tokens);
    }

    @Override
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> autenticar(@Valid @RequestBody LoginRequest requisicao) {
        return ResponseEntity.ok(controller.autenticar(requisicao.paraDTO()));
    }
}
