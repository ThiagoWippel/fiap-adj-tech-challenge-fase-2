package br.com.fiap.restaurante.infrastructure.api.restaurante;

import br.com.fiap.restaurante.interfaceadapter.presenter.PaginaResponse;
import br.com.fiap.restaurante.interfaceadapter.presenter.RestauranteResponse;
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

import static br.com.fiap.restaurante.infrastructure.api.comum.ExemplosDeProblema.COZINHA_INVALIDA;
import static br.com.fiap.restaurante.infrastructure.api.comum.ExemplosDeProblema.DADOS_INVALIDOS;
import static br.com.fiap.restaurante.infrastructure.api.comum.ExemplosDeProblema.DONO_NAO_E_DONO;
import static br.com.fiap.restaurante.infrastructure.api.comum.ExemplosDeProblema.ORDENACAO_INVALIDA;
import static br.com.fiap.restaurante.infrastructure.api.comum.ExemplosDeProblema.RESTAURANTE_NAO_ENCONTRADO;
import static br.com.fiap.restaurante.infrastructure.api.comum.ExemplosDeProblema.TURNOS_SOBREPOSTOS;
import static br.com.fiap.restaurante.infrastructure.api.comum.ExemplosDeProblema.USUARIO_NAO_ENCONTRADO;

/**
 * Documentação OpenAPI dos endpoints de restaurante.
 */
@Tag(name = "Restaurantes", description = "Cadastro de restaurantes, com tipo de cozinha, horários e dono")
interface RestauranteApi {

    String JSON = "application/json";
    String PROBLEMA = "application/problem+json";

    @Operation(summary = "Cadastra um restaurante", description = """
            O dono precisa ser um usuário ativo do tipo Dono de Restaurante. Os horários são turnos por dia da \
            semana, vários por dia, sem sobreposição; fechamento antes da abertura quer dizer que o turno termina \
            no dia seguinte (sexta 18:00–02:00 vai até as 2h de sábado). Dia sem turno é dia fechado. Um \
            restaurante 24 horas cadastra 00:00–23:59.""")
    @ApiResponse(responseCode = "201", description = "Restaurante cadastrado. O cabeçalho Location aponta para ele.",
            content = @Content(mediaType = JSON, schema = @Schema(implementation = RestauranteResponse.class),
                    examples = @ExampleObject(ExemplosDeRestaurante.RESTAURANTE)))
    @ApiResponse(responseCode = "400", description = "Campo inválido, tipo de cozinha fora da lista ou turnos sobrepostos",
            content = @Content(mediaType = PROBLEMA, examples = {
                    @ExampleObject(name = "Campos inválidos", value = DADOS_INVALIDOS),
                    @ExampleObject(name = "Tipo de cozinha", value = COZINHA_INVALIDA),
                    @ExampleObject(name = "Turnos sobrepostos", value = TURNOS_SOBREPOSTOS)}))
    @ApiResponse(responseCode = "404", description = "Dono inexistente",
            content = @Content(mediaType = PROBLEMA, examples = @ExampleObject(USUARIO_NAO_ENCONTRADO)))
    @ApiResponse(responseCode = "409", description = "O dono não é do tipo Dono de Restaurante",
            content = @Content(mediaType = PROBLEMA, examples = @ExampleObject(DONO_NAO_E_DONO)))
    ResponseEntity<RestauranteResponse> cadastrar(RestauranteRequest requisicao, UriComponentsBuilder uri);

    @Operation(summary = "Lista os restaurantes", description = """
            Só os restaurantes ativos, paginados: 10 por página por padrão, no máximo 50, em ordem de nome. \
            Filtros opcionais: trecho do nome, sem diferenciar maiúsculas nem acentos, e tipo de cozinha. O sort \
            aceita id, nome, tipoCozinha, dataCriacao e dataUltimaAlteracao.""")
    @ApiResponse(responseCode = "200", description = "Página de restaurantes",
            content = @Content(mediaType = JSON, schema = @Schema(implementation = PaginaResponse.class),
                    examples = @ExampleObject(ExemplosDeRestaurante.PAGINA)))
    @ApiResponse(responseCode = "400", description = "Tipo de cozinha fora da lista, ou ordenação por campo não aceito",
            content = @Content(mediaType = PROBLEMA, examples = {
                    @ExampleObject(name = "Tipo de cozinha", value = COZINHA_INVALIDA),
                    @ExampleObject(name = "Ordenação", value = ORDENACAO_INVALIDA)}))
    @Parameter(name = "page", in = ParameterIn.QUERY, description = "Número da página, a partir de 0",
            schema = @Schema(type = "integer", defaultValue = "0"))
    @Parameter(name = "size", in = ParameterIn.QUERY, description = "Itens por página, no máximo 50",
            schema = @Schema(type = "integer", defaultValue = "10"))
    @Parameter(name = "sort", in = ParameterIn.QUERY, description = "Campo e direção, como nome,desc",
            schema = @Schema(type = "string", defaultValue = "nome,asc"))
    ResponseEntity<PaginaResponse<RestauranteResponse>> listar(
            @Parameter(description = "Trecho do nome", example = "nona") String nome,
            @Parameter(description = "Tipo de cozinha", example = "ITALIANA") String tipoCozinha,
            @Parameter(hidden = true) Pageable paginacao);

    @Operation(summary = "Consulta um restaurante pelo id")
    @ApiResponse(responseCode = "200", description = "Restaurante encontrado",
            content = @Content(mediaType = JSON, schema = @Schema(implementation = RestauranteResponse.class),
                    examples = @ExampleObject(ExemplosDeRestaurante.RESTAURANTE)))
    @ApiResponse(responseCode = "404", description = "Restaurante inexistente ou removido",
            content = @Content(mediaType = PROBLEMA, examples = @ExampleObject(RESTAURANTE_NAO_ENCONTRADO)))
    ResponseEntity<RestauranteResponse> buscarPorId(@Parameter(description = "Id do restaurante", example = "9") Long id);

    @Operation(summary = "Atualiza um restaurante", description = """
            Substitui os dados e a lista inteira de turnos. Um donoId diferente transfere o restaurante, com as \
            mesmas exigências do cadastro.""")
    @ApiResponse(responseCode = "200", description = "Restaurante atualizado",
            content = @Content(mediaType = JSON, schema = @Schema(implementation = RestauranteResponse.class),
                    examples = @ExampleObject(ExemplosDeRestaurante.RESTAURANTE)))
    @ApiResponse(responseCode = "400", description = "Campo inválido, tipo de cozinha fora da lista ou turnos sobrepostos",
            content = @Content(mediaType = PROBLEMA, examples = @ExampleObject(TURNOS_SOBREPOSTOS)))
    @ApiResponse(responseCode = "404", description = "Restaurante ou novo dono inexistente",
            content = @Content(mediaType = PROBLEMA, examples = @ExampleObject(RESTAURANTE_NAO_ENCONTRADO)))
    @ApiResponse(responseCode = "409", description = "O novo dono não é do tipo Dono de Restaurante",
            content = @Content(mediaType = PROBLEMA, examples = @ExampleObject(DONO_NAO_E_DONO)))
    ResponseEntity<RestauranteResponse> atualizar(@Parameter(description = "Id do restaurante", example = "9") Long id,
                                                  RestauranteRequest requisicao);

    @Operation(summary = "Exclui um restaurante", description = """
            A exclusão é lógica: o restaurante some das consultas, e a linha fica no banco para o histórico de \
            pedidos e avaliações das próximas fases.""")
    @ApiResponse(responseCode = "204", description = "Restaurante excluído")
    @ApiResponse(responseCode = "404", description = "Restaurante inexistente ou já removido",
            content = @Content(mediaType = PROBLEMA, examples = @ExampleObject(RESTAURANTE_NAO_ENCONTRADO)))
    ResponseEntity<Void> excluir(@Parameter(description = "Id do restaurante", example = "9") Long id);
}
