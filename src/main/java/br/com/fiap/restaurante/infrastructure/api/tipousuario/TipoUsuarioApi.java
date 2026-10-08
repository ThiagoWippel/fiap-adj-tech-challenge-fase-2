package br.com.fiap.restaurante.infrastructure.api.tipousuario;

import br.com.fiap.restaurante.interfaceadapter.presenter.PaginaResponse;
import br.com.fiap.restaurante.interfaceadapter.presenter.TipoUsuarioResponse;
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
import org.springframework.web.util.UriComponentsBuilder;

import static br.com.fiap.restaurante.infrastructure.api.comum.ExemplosDeProblema.DADOS_INVALIDOS;
import static br.com.fiap.restaurante.infrastructure.api.comum.ExemplosDeProblema.NOME_DE_TIPO_EM_USO;
import static br.com.fiap.restaurante.infrastructure.api.comum.ExemplosDeProblema.ORDENACAO_INVALIDA;
import static br.com.fiap.restaurante.infrastructure.api.comum.ExemplosDeProblema.TIPO_DE_SISTEMA;
import static br.com.fiap.restaurante.infrastructure.api.comum.ExemplosDeProblema.TIPO_EM_USO;
import static br.com.fiap.restaurante.infrastructure.api.comum.ExemplosDeProblema.TIPO_INEXISTENTE;

/**
 * Documentação OpenAPI dos endpoints de tipo de usuário.
 */
@Tag(name = "Tipos de usuário", description = "Cadastro dos tipos de usuário e consulta dos usuários de cada tipo")
interface TipoUsuarioApi {

    String JSON = "application/json";
    String PROBLEMA = "application/problem+json";

    @Operation(summary = "Cadastra um tipo de usuário", description = """
            Recebe só o nome. O código é gerado a partir dele, sem acentos, em maiúsculas e com espaços \
            trocados por sublinhado ("Ajudante de Cozinha" vira AJUDANTE_DE_COZINHA), e nunca muda. Nome e \
            código são únicos, sem diferenciar maiúsculas nem acentos.""")
    @ApiResponse(responseCode = "201", description = "Tipo cadastrado. O cabeçalho Location aponta para ele.",
            content = @Content(mediaType = JSON, schema = @Schema(implementation = TipoUsuarioResponse.class),
                    examples = @ExampleObject(ExemplosDeTipoUsuario.TIPO)))
    @ApiResponse(responseCode = "400", description = "Nome ausente ou fora de 3 a 50 caracteres",
            content = @Content(mediaType = PROBLEMA, examples = @ExampleObject(DADOS_INVALIDOS)))
    @ApiResponse(responseCode = "409", description = "Nome já usado, ou nome que gera o código de outro tipo",
            content = @Content(mediaType = PROBLEMA, examples = @ExampleObject(NOME_DE_TIPO_EM_USO)))
    ResponseEntity<TipoUsuarioResponse> cadastrar(TipoUsuarioRequest requisicao, UriComponentsBuilder uri);

    @Operation(summary = "Lista os tipos de usuário", description = """
            Paginada: 10 por página por padrão, no máximo 50, em ordem de nome. O sort aceita id, nome e codigo.""")
    @ApiResponse(responseCode = "200", description = "Página de tipos",
            content = @Content(mediaType = JSON, schema = @Schema(implementation = PaginaResponse.class),
                    examples = @ExampleObject(ExemplosDeTipoUsuario.PAGINA)))
    @ApiResponse(responseCode = "400", description = "Ordenação por campo não aceito",
            content = @Content(mediaType = PROBLEMA, examples = @ExampleObject(ORDENACAO_INVALIDA)))
    @Parameter(name = "page", in = ParameterIn.QUERY, description = "Número da página, a partir de 0",
            schema = @Schema(type = "integer", defaultValue = "0"))
    @Parameter(name = "size", in = ParameterIn.QUERY, description = "Itens por página, no máximo 50",
            schema = @Schema(type = "integer", defaultValue = "10"))
    @Parameter(name = "sort", in = ParameterIn.QUERY, description = "Campo e direção, como nome,desc",
            schema = @Schema(type = "string", defaultValue = "nome,asc"))
    ResponseEntity<PaginaResponse<TipoUsuarioResponse>> listar(@Parameter(hidden = true) Pageable paginacao);

    @Operation(summary = "Consulta um tipo de usuário pelo id")
    @ApiResponse(responseCode = "200", description = "Tipo encontrado",
            content = @Content(mediaType = JSON, schema = @Schema(implementation = TipoUsuarioResponse.class),
                    examples = @ExampleObject(ExemplosDeTipoUsuario.TIPO)))
    @ApiResponse(responseCode = "404", description = "Tipo inexistente",
            content = @Content(mediaType = PROBLEMA, examples = @ExampleObject(TIPO_INEXISTENTE)))
    ResponseEntity<TipoUsuarioResponse> buscarPorId(@Parameter(description = "Id do tipo", example = "3") Long id);

    @Operation(summary = "Renomeia um tipo de usuário", description = """
            Muda só o nome; o código continua o mesmo. Os tipos de sistema também podem ser renomeados.""")
    @ApiResponse(responseCode = "200", description = "Tipo renomeado",
            content = @Content(mediaType = JSON, schema = @Schema(implementation = TipoUsuarioResponse.class),
                    examples = @ExampleObject(ExemplosDeTipoUsuario.TIPO)))
    @ApiResponse(responseCode = "400", description = "Nome ausente ou fora de 3 a 50 caracteres",
            content = @Content(mediaType = PROBLEMA, examples = @ExampleObject(DADOS_INVALIDOS)))
    @ApiResponse(responseCode = "404", description = "Tipo inexistente",
            content = @Content(mediaType = PROBLEMA, examples = @ExampleObject(TIPO_INEXISTENTE)))
    @ApiResponse(responseCode = "409", description = "Nome de outro tipo",
            content = @Content(mediaType = PROBLEMA, examples = @ExampleObject(NOME_DE_TIPO_EM_USO)))
    ResponseEntity<TipoUsuarioResponse> renomear(@Parameter(description = "Id do tipo", example = "3") Long id,
                                                 TipoUsuarioRequest requisicao);

    @Operation(summary = "Exclui um tipo de usuário", description = """
            A exclusão é definitiva. Cliente e Dono de Restaurante são tipos de sistema e não podem ser \
            excluídos; um tipo usado por algum usuário ativo também não.""")
    @ApiResponse(responseCode = "204", description = "Tipo excluído")
    @ApiResponse(responseCode = "404", description = "Tipo inexistente",
            content = @Content(mediaType = PROBLEMA, examples = @ExampleObject(TIPO_INEXISTENTE)))
    @ApiResponse(responseCode = "409", description = "Tipo de sistema, ou em uso por usuários ativos",
            content = @Content(mediaType = PROBLEMA, examples = {
                    @ExampleObject(name = "Em uso", value = TIPO_EM_USO),
                    @ExampleObject(name = "Tipo de sistema", value = TIPO_DE_SISTEMA)}))
    ResponseEntity<Void> excluir(@Parameter(description = "Id do tipo", example = "3") Long id);

    @Operation(summary = "Lista os usuários de um tipo", description = """
            Só os usuários ativos, paginados como as demais listagens. O sort aceita os mesmos campos da busca \
            de usuários.""")
    @ApiResponse(responseCode = "200", description = "Página de usuários do tipo",
            content = @Content(mediaType = JSON, schema = @Schema(implementation = PaginaResponse.class),
                    examples = @ExampleObject(ExemplosDeTipoUsuario.PAGINA_DE_USUARIOS)))
    @ApiResponse(responseCode = "404", description = "Tipo inexistente",
            content = @Content(mediaType = PROBLEMA, examples = @ExampleObject(TIPO_INEXISTENTE)))
    @Parameter(name = "page", in = ParameterIn.QUERY, description = "Número da página, a partir de 0",
            schema = @Schema(type = "integer", defaultValue = "0"))
    @Parameter(name = "size", in = ParameterIn.QUERY, description = "Itens por página, no máximo 50",
            schema = @Schema(type = "integer", defaultValue = "10"))
    @Parameter(name = "sort", in = ParameterIn.QUERY, description = "Campo e direção, como nome,desc",
            schema = @Schema(type = "string", defaultValue = "nome,asc"))
    ResponseEntity<PaginaResponse<UsuarioResponse>> listarUsuarios(
            @Parameter(description = "Id do tipo", example = "3") Long id,
            @Parameter(hidden = true) Pageable paginacao);
}
