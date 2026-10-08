package br.com.fiap.restaurante.infrastructure.api.autenticacao;

import br.com.fiap.restaurante.interfaceadapter.presenter.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import static br.com.fiap.restaurante.infrastructure.api.comum.ExemplosDeProblema.CREDENCIAIS_INVALIDAS;
import static br.com.fiap.restaurante.infrastructure.api.comum.ExemplosDeProblema.DADOS_INVALIDOS;

/**
 * Documentação OpenAPI do endpoint de login.
 */
@Tag(name = "Autenticação", description = "Validação de login e senha")
interface AutenticacaoApi {

    @Operation(summary = "Valida login e senha e emite um token", description = """
            Devolve quem autenticou e um JWT assinado, com a data de expiração. Nesta fase nenhum endpoint \
            exige o token. Login inexistente e senha errada recebem a mesma resposta, para não revelar quais \
            logins existem.""")
    @ApiResponse(responseCode = "200", description = "Credenciais válidas",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponse.class),
                    examples = @ExampleObject("""
                    {
                      "id": 1,
                      "nome": "Maria Silva",
                      "tipo": "CLIENTE",
                      "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwidGlwbyI6IkNMSUVOVEUifQ.assinatura",
                      "expiraEm": "2026-10-08T11:30:00"
                    }""")))
    @ApiResponse(responseCode = "400", description = "Login ou senha ausente",
            content = @Content(mediaType = "application/problem+json", examples = @ExampleObject(DADOS_INVALIDOS)))
    @ApiResponse(responseCode = "401", description = "Login ou senha inválidos",
            content = @Content(mediaType = "application/problem+json",
                    examples = @ExampleObject(CREDENCIAIS_INVALIDAS)))
    ResponseEntity<LoginResponse> autenticar(LoginRequest requisicao);
}
