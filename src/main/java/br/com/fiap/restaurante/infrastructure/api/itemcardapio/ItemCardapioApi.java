package br.com.fiap.restaurante.infrastructure.api.itemcardapio;

import br.com.fiap.restaurante.interfaceadapter.presenter.ItemCardapioResponse;
import br.com.fiap.restaurante.interfaceadapter.presenter.PaginaResponse;
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
import org.springframework.web.util.UriComponentsBuilder;

import static br.com.fiap.restaurante.infrastructure.api.comum.ExemplosDeProblema.DADOS_DO_ITEM_INVALIDOS;
import static br.com.fiap.restaurante.infrastructure.api.comum.ExemplosDeProblema.ITEM_NAO_ENCONTRADO;
import static br.com.fiap.restaurante.infrastructure.api.comum.ExemplosDeProblema.NOME_DE_ITEM_EM_USO;
import static br.com.fiap.restaurante.infrastructure.api.comum.ExemplosDeProblema.ORDENACAO_INVALIDA;
import static br.com.fiap.restaurante.infrastructure.api.comum.ExemplosDeProblema.RESTAURANTE_NAO_ENCONTRADO;

/**
 * Documentação OpenAPI dos endpoints de item do cardápio.
 */
@Tag(name = "Itens do cardápio", description = "Itens vendidos por um restaurante, nas rotas do próprio restaurante")
interface ItemCardapioApi {

    String JSON = "application/json";
    String PROBLEMA = "application/problem+json";

    @Operation(summary = "Cadastra um item no cardápio", description = """
            O nome é único entre os itens ativos do restaurante, sem diferenciar maiúsculas, acentos nem espaço \
            no fim; o mesmo nome em outro restaurante é aceito. O preço precisa ser maior que zero, com no \
            máximo duas casas decimais (mais casas são recusadas, não arredondadas). A foto é só o caminho, \
            terminado em .jpg, .jpeg, .png ou .webp.""")
    @ApiResponse(responseCode = "201", description = "Item cadastrado. O cabeçalho Location aponta para ele.",
            content = @Content(mediaType = JSON, schema = @Schema(implementation = ItemCardapioResponse.class),
                    examples = @ExampleObject(ExemplosDeItemCardapio.ITEM)))
    @ApiResponse(responseCode = "400", description = "Campo ausente ou inválido",
            content = @Content(mediaType = PROBLEMA, examples = @ExampleObject(DADOS_DO_ITEM_INVALIDOS)))
    @ApiResponse(responseCode = "404", description = "Restaurante inexistente ou removido",
            content = @Content(mediaType = PROBLEMA, examples = @ExampleObject(RESTAURANTE_NAO_ENCONTRADO)))
    @ApiResponse(responseCode = "409", description = "Nome de outro item ativo do restaurante",
            content = @Content(mediaType = PROBLEMA, examples = @ExampleObject(NOME_DE_ITEM_EM_USO)))
    ResponseEntity<ItemCardapioResponse> cadastrar(
            @Parameter(description = "Id do restaurante", example = "9") Long restauranteId,
            ItemCardapioRequest requisicao, UriComponentsBuilder uri);

    @Operation(summary = "Lista o cardápio de um restaurante", description = """
            Só os itens ativos, paginados: 10 por página por padrão, no máximo 50, em ordem de nome. O filtro \
            apenasNoLocal é opcional. O sort aceita id, nome, preco, dataCriacao e dataUltimaAlteracao.""")
    @ApiResponse(responseCode = "200", description = "Página de itens",
            content = @Content(mediaType = JSON, schema = @Schema(implementation = PaginaResponse.class),
                    examples = @ExampleObject(ExemplosDeItemCardapio.PAGINA)))
    @ApiResponse(responseCode = "400", description = "Ordenação por campo não aceito",
            content = @Content(mediaType = PROBLEMA, examples = @ExampleObject(ORDENACAO_INVALIDA)))
    @ApiResponse(responseCode = "404", description = "Restaurante inexistente ou removido",
            content = @Content(mediaType = PROBLEMA, examples = @ExampleObject(RESTAURANTE_NAO_ENCONTRADO)))
    @Parameter(name = "page", in = ParameterIn.QUERY, description = "Número da página, a partir de 0",
            schema = @Schema(type = "integer", defaultValue = "0"))
    @Parameter(name = "size", in = ParameterIn.QUERY, description = "Itens por página, no máximo 50",
            schema = @Schema(type = "integer", defaultValue = "10"))
    @Parameter(name = "sort", in = ParameterIn.QUERY, description = "Campo e direção, como preco,desc",
            schema = @Schema(type = "string", defaultValue = "nome,asc"))
    ResponseEntity<PaginaResponse<ItemCardapioResponse>> listar(
            @Parameter(description = "Id do restaurante", example = "9") Long restauranteId,
            @Parameter(description = "Só os itens disponíveis apenas no local (true) ou os outros (false)")
            Boolean apenasNoLocal,
            @Parameter(hidden = true) Pageable paginacao);

    @Operation(summary = "Consulta um item do cardápio", description = """
            O item precisa ser do restaurante da rota; pela rota de outro restaurante, a resposta é 404.""")
    @ApiResponse(responseCode = "200", description = "Item encontrado",
            content = @Content(mediaType = JSON, schema = @Schema(implementation = ItemCardapioResponse.class),
                    examples = @ExampleObject(ExemplosDeItemCardapio.ITEM)))
    @ApiResponse(responseCode = "404", description = "Restaurante ou item inexistente, removido, ou item de outro restaurante",
            content = @Content(mediaType = PROBLEMA, examples = @ExampleObject(ITEM_NAO_ENCONTRADO)))
    ResponseEntity<ItemCardapioResponse> buscarPorId(
            @Parameter(description = "Id do restaurante", example = "9") Long restauranteId,
            @Parameter(description = "Id do item", example = "5") Long itemId);

    @Operation(summary = "Atualiza um item do cardápio", description = """
            Substitui os dados do item. Manter o próprio nome é aceito; usar o nome de outro item ativo do \
            restaurante, não.""")
    @ApiResponse(responseCode = "200", description = "Item atualizado",
            content = @Content(mediaType = JSON, schema = @Schema(implementation = ItemCardapioResponse.class),
                    examples = @ExampleObject(ExemplosDeItemCardapio.ITEM)))
    @ApiResponse(responseCode = "400", description = "Campo ausente ou inválido",
            content = @Content(mediaType = PROBLEMA, examples = @ExampleObject(DADOS_DO_ITEM_INVALIDOS)))
    @ApiResponse(responseCode = "404", description = "Restaurante ou item inexistente, removido, ou item de outro restaurante",
            content = @Content(mediaType = PROBLEMA, examples = @ExampleObject(ITEM_NAO_ENCONTRADO)))
    @ApiResponse(responseCode = "409", description = "Nome de outro item ativo do restaurante",
            content = @Content(mediaType = PROBLEMA, examples = @ExampleObject(NOME_DE_ITEM_EM_USO)))
    ResponseEntity<ItemCardapioResponse> atualizar(
            @Parameter(description = "Id do restaurante", example = "9") Long restauranteId,
            @Parameter(description = "Id do item", example = "5") Long itemId,
            ItemCardapioRequest requisicao);

    @Operation(summary = "Exclui um item do cardápio", description = """
            A exclusão é lógica: o item some do cardápio e o nome fica livre para outro item do restaurante.""")
    @ApiResponse(responseCode = "204", description = "Item excluído")
    @ApiResponse(responseCode = "404", description = "Restaurante ou item inexistente, removido, ou item de outro restaurante",
            content = @Content(mediaType = PROBLEMA, examples = @ExampleObject(ITEM_NAO_ENCONTRADO)))
    ResponseEntity<Void> excluir(@Parameter(description = "Id do restaurante", example = "9") Long restauranteId,
                                 @Parameter(description = "Id do item", example = "5") Long itemId);
}
