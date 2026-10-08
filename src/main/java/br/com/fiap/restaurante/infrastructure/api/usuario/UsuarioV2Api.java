package br.com.fiap.restaurante.infrastructure.api.usuario;

import br.com.fiap.restaurante.interfaceadapter.presenter.PaginaResponse;
import br.com.fiap.restaurante.interfaceadapter.presenter.UsuarioResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import static br.com.fiap.restaurante.infrastructure.api.comum.ExemplosDeProblema.ORDENACAO_INVALIDA;

/**
 * Documentação OpenAPI da busca paginada de usuários. O {@code Pageable} fica
 * oculto e os parâmetros page, size e sort são descritos um a um, em português.
 */
@Tag(name = "Usuários v2", description = "Busca de usuários com paginação")
interface UsuarioV2Api {

    @Operation(summary = "Busca usuários pelo nome, com paginação", description = """
            Mesma busca da v1, com outra resposta: em vez da lista, um objeto com a lista em "conteudo" e os \
            dados da página. Por isso é uma versão nova: quem consome a v1 não lê esta resposta. São 10 \
            itens por página por padrão, no máximo 50, em ordem de nome. O sort aceita id, nome, email, \
            login, dataCriacao e dataUltimaAlteracao.""")
    @ApiResponse(responseCode = "200", description = "Página de usuários",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PaginaResponse.class),
                    examples = @ExampleObject(ExemplosDeUsuario.PAGINA)))
    @ApiResponse(responseCode = "400", description = "Ordenação por campo não aceito",
            content = @Content(mediaType = "application/problem+json", examples = @ExampleObject(ORDENACAO_INVALIDA)))
    @Parameter(name = "page", in = ParameterIn.QUERY, description = "Número da página, a partir de 0",
            schema = @Schema(type = "integer", defaultValue = "0"))
    @Parameter(name = "size", in = ParameterIn.QUERY, description = "Itens por página, no máximo 50",
            schema = @Schema(type = "integer", defaultValue = "10"))
    @Parameter(name = "sort", in = ParameterIn.QUERY, description = "Campo e direção, como nome,desc",
            schema = @Schema(type = "string", defaultValue = "nome,asc"))
    ResponseEntity<PaginaResponse<UsuarioResponse>> buscarPorNome(
            @Parameter(description = "Trecho do nome", example = "maria") String nome,
            @Parameter(hidden = true) Pageable paginacao);
}
