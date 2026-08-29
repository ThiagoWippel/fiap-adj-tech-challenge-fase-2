package br.com.fiap.restaurante.controllers;

import br.com.fiap.restaurante.dtos.response.PaginaResponse;
import br.com.fiap.restaurante.dtos.response.UsuarioResponse;
import br.com.fiap.restaurante.services.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Recursos de usuário - versão 2.
 *
 * Existe para demonstrar a estratégia de versionamento em funcionamento, e não
 * apenas descrita no relatório.
 *
 * O critério que justifica a nova versão merece registro. Acrescentar um campo
 * a uma resposta é mudança compatível: consumidores existentes seguem
 * funcionando e simplesmente ignoram o campo novo - e portanto não demanda nova
 * versão. Já alterar a resposta de um vetor para um objeto quebra todo cliente
 * que percorre o resultado, e é exatamente o caso de uso para o qual o
 * versionamento existe.
 *
 * Por isso a v2 não replica os demais endpoints: eles permanecem inalterados e
 * continuam atendidos pela v1. Duplicar rotas idênticas apenas para preencher a
 * versão seria ruído.
 */
@RestController
@RequestMapping("/api/v2/usuarios")
@Tag(name = "Usuarios v2", description = "Busca de usuarios com paginacao")
public class UsuarioControllerV2 {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioControllerV2.class);

    private final UsuarioService usuarioService;

    public UsuarioControllerV2(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    @Operation(
            summary = "Busca usuarios pelo nome, com paginacao",
            description = """
                    Mesma consulta da versao 1, com resposta em formato distinto.

                    **Mudanca incompativel em relacao a v1:** onde a versao anterior devolve um \
                    vetor de usuarios, esta devolve um objeto contendo o vetor no campo \
                    "conteudo" acompanhado dos metadados de paginacao. Um consumidor da v1 nao \
                    consegue processar esta resposta, o que caracteriza a quebra de contrato \
                    que motiva o incremento de versao."""
    )
    @ApiResponse(
            responseCode = "200",
            description = "Pagina de resultados",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = @ExampleObject(value = """
                            {
                              "conteudo": [
                                {
                                  "id": 1,
                                  "nome": "Maria Silva",
                                  "email": "maria.silva@exemplo.com",
                                  "login": "maria.silva",
                                  "tipo": "CLIENTE",
                                  "documento": "12345678909",
                                  "endereco": {
                                    "rua": "Rua das Flores",
                                    "numero": "123",
                                    "complemento": "Apto 45",
                                    "bairro": "Centro",
                                    "cidade": "Camboriu",
                                    "estado": "SC",
                                    "cep": "88340000"
                                  },
                                  "dataCriacao": "2026-08-21T10:30:00",
                                  "dataUltimaAlteracao": "2026-08-21T10:30:00"
                                }
                              ],
                              "pagina": 0,
                              "tamanho": 10,
                              "totalElementos": 1,
                              "totalPaginas": 1,
                              "ultima": true
                            }""")
            )
    )
    public ResponseEntity<PaginaResponse<UsuarioResponse>> buscarPorNome(
            @Parameter(description = "Termo de busca", example = "maria")
            @RequestParam(required = false) String nome,

            @PageableDefault(size = 10, sort = "nome") Pageable paginacao) {

        logger.info("GET /api/v2/usuarios?nome={} pagina={}", nome, paginacao.getPageNumber());
        return ResponseEntity.ok(
                PaginaResponse.de(usuarioService.buscarPorNomePaginado(nome, paginacao))
        );
    }
}
