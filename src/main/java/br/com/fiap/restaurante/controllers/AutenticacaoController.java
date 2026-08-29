package br.com.fiap.restaurante.controllers;

import br.com.fiap.restaurante.dtos.request.LoginRequest;
import br.com.fiap.restaurante.dtos.response.LoginResponse;
import br.com.fiap.restaurante.services.AutenticacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Validação de credenciais.
 *
 * O enunciado dispensa o uso de Spring Security e admite verificação simples
 * contra os dados do banco. A senha é conferida por comparação de hashes
 * BCrypt, jamais em texto.
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticacao", description = "Validacao de login e senha")
public class AutenticacaoController {

    private static final Logger logger = LoggerFactory.getLogger(AutenticacaoController.class);

    private final AutenticacaoService autenticacaoService;

    public AutenticacaoController(AutenticacaoService autenticacaoService) {
        this.autenticacaoService = autenticacaoService;
    }

    @PostMapping("/login")
    @Operation(
            summary = "Valida as credenciais de um usuario",
            description = """
                    Confere se login e senha correspondem a um usuario cadastrado.

                    O retorno traz apenas identificador, nome e tipo. Nao e uma consulta de \
                    perfil: a operacao responde se as credenciais conferem, e nao quem e o \
                    usuario.

                    **Nota de seguranca:** a resposta de falha e identica para login \
                    inexistente e senha incorreta, tanto em mensagem quanto em tempo de \
                    processamento. Diferenciar os dois casos permitiria descobrir quais \
                    logins existem por tentativa e erro."""
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Credenciais validas",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = LoginResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "id": 1,
                                      "nome": "Maria Silva",
                                      "tipo": "CLIENTE"
                                    }""")
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Login ou senha ausente",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(
                    responseCode = "401",
                    description = "Credenciais invalidas",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "type": "https://github.com/ThiagoWippel/fiap-adj-tech-challenge-fase-1/blob/main/README.md#credenciais-invalidas",
                                      "title": "Credenciais invalidas",
                                      "status": 401,
                                      "detail": "Login ou senha invalidos",
                                      "instance": "/api/v1/auth/login",
                                      "momento": "2026-08-21T10:30:00"
                                    }""")
                    )
            )
    })
    public ResponseEntity<LoginResponse> autenticar(@Valid @RequestBody LoginRequest requisicao) {
        logger.info("POST /api/v1/auth/login");
        return ResponseEntity.ok(autenticacaoService.autenticar(requisicao));
    }
}
