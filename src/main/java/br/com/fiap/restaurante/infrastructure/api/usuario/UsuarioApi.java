package br.com.fiap.restaurante.infrastructure.api.usuario;

import br.com.fiap.restaurante.infrastructure.api.restaurante.ExemplosDeRestaurante;
import br.com.fiap.restaurante.interfaceadapter.presenter.PaginaResponse;
import br.com.fiap.restaurante.interfaceadapter.presenter.RestauranteResponse;
import br.com.fiap.restaurante.interfaceadapter.presenter.UsuarioResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

import static br.com.fiap.restaurante.infrastructure.api.comum.ExemplosDeProblema.DADOS_INVALIDOS;
import static br.com.fiap.restaurante.infrastructure.api.comum.ExemplosDeProblema.DOCUMENTO_DO_TIPO;
import static br.com.fiap.restaurante.infrastructure.api.comum.ExemplosDeProblema.DOCUMENTO_EM_USO;
import static br.com.fiap.restaurante.infrastructure.api.comum.ExemplosDeProblema.DONO_COM_RESTAURANTE;
import static br.com.fiap.restaurante.infrastructure.api.comum.ExemplosDeProblema.EMAIL_EM_USO;
import static br.com.fiap.restaurante.infrastructure.api.comum.ExemplosDeProblema.REGRA_DE_NEGOCIO;
import static br.com.fiap.restaurante.infrastructure.api.comum.ExemplosDeProblema.SENHA_ATUAL_INCORRETA;
import static br.com.fiap.restaurante.infrastructure.api.comum.ExemplosDeProblema.TIPO_NAO_ENCONTRADO;
import static br.com.fiap.restaurante.infrastructure.api.comum.ExemplosDeProblema.USUARIO_COM_RESTAURANTE;
import static br.com.fiap.restaurante.infrastructure.api.comum.ExemplosDeProblema.USUARIO_NAO_ENCONTRADO;

/**
 * Documentação OpenAPI dos endpoints de usuário. Fica separada para o
 * controller mostrar só o mapeamento das rotas.
 */
@Tag(name = "Usuários", description = "Cadastro, consulta, atualização, troca de senha e exclusão de usuários")
interface UsuarioApi {

    String JSON = "application/json";
    String PROBLEMA = "application/problem+json";

    @Operation(summary = "Cadastra um usuário", description = """
            O tipo é o código de um tipo de usuário, como CLIENTE ou DONO_RESTAURANTE. Dono de restaurante \
            informa CNPJ; os demais tipos informam CPF. Os documentos são aceitos com ou sem pontuação e \
            gravados só com dígitos. E-mail, login e documento são únicos.""")
    @ApiResponse(responseCode = "201", description = "Usuário cadastrado. O cabeçalho Location aponta para ele.",
            content = @Content(mediaType = JSON, schema = @Schema(implementation = UsuarioResponse.class),
                    examples = @ExampleObject(ExemplosDeUsuario.USUARIO)))
    @ApiResponse(responseCode = "400", description = "Campo inválido, ou documento que não corresponde ao tipo",
            content = @Content(mediaType = PROBLEMA, examples = {
                    @ExampleObject(name = "Campos inválidos", value = DADOS_INVALIDOS),
                    @ExampleObject(name = "Documento exigido pelo tipo", value = REGRA_DE_NEGOCIO)}))
    @ApiResponse(responseCode = "404", description = "Código de tipo inexistente",
            content = @Content(mediaType = PROBLEMA, examples = @ExampleObject(TIPO_NAO_ENCONTRADO)))
    @ApiResponse(responseCode = "409", description = "E-mail, login ou documento já cadastrado",
            content = @Content(mediaType = PROBLEMA, examples = @ExampleObject(EMAIL_EM_USO)))
    ResponseEntity<UsuarioResponse> cadastrar(CriarUsuarioRequest requisicao, UriComponentsBuilder uri);

    @Operation(summary = "Consulta um usuário pelo id")
    @ApiResponse(responseCode = "200", description = "Usuário encontrado",
            content = @Content(mediaType = JSON, schema = @Schema(implementation = UsuarioResponse.class),
                    examples = @ExampleObject(ExemplosDeUsuario.USUARIO)))
    @ApiResponse(responseCode = "404", description = "Usuário inexistente ou removido",
            content = @Content(mediaType = PROBLEMA, examples = @ExampleObject(USUARIO_NAO_ENCONTRADO)))
    ResponseEntity<UsuarioResponse> buscarPorId(@Parameter(description = "Id do usuário", example = "1") Long id);

    @Operation(summary = "Busca usuários pelo nome", description = """
            Devolve os usuários cujo nome contém o termo, sem diferenciar maiúsculas nem acentos, em ordem \
            alfabética. Sem o termo, devolve todos. Nenhum resultado é uma lista vazia, não um 404. A versão \
            paginada fica em /api/v2/usuarios.""")
    @ApiResponse(responseCode = "200", description = "Lista de usuários, possivelmente vazia",
            content = @Content(mediaType = JSON,
                    array = @ArraySchema(schema = @Schema(implementation = UsuarioResponse.class)),
                    examples = @ExampleObject(ExemplosDeUsuario.LISTA)))
    @ApiResponse(responseCode = "400", description = "Parâmetro em formato inválido",
            content = @Content(mediaType = PROBLEMA, examples = @ExampleObject(DADOS_INVALIDOS)))
    ResponseEntity<List<UsuarioResponse>> buscarPorNome(
            @Parameter(description = "Trecho do nome", example = "maria") String nome);

    @Operation(summary = "Atualiza os dados de um usuário", description = """
            Altera nome, e-mail, login e endereço. A senha e o tipo têm endpoints próprios.""")
    @ApiResponse(responseCode = "200", description = "Usuário atualizado",
            content = @Content(mediaType = JSON, schema = @Schema(implementation = UsuarioResponse.class),
                    examples = @ExampleObject(ExemplosDeUsuario.USUARIO)))
    @ApiResponse(responseCode = "400", description = "Campo inválido",
            content = @Content(mediaType = PROBLEMA, examples = @ExampleObject(DADOS_INVALIDOS)))
    @ApiResponse(responseCode = "404", description = "Usuário inexistente ou removido",
            content = @Content(mediaType = PROBLEMA, examples = @ExampleObject(USUARIO_NAO_ENCONTRADO)))
    @ApiResponse(responseCode = "409", description = "E-mail ou login de outro usuário",
            content = @Content(mediaType = PROBLEMA, examples = @ExampleObject(EMAIL_EM_USO)))
    ResponseEntity<UsuarioResponse> atualizar(@Parameter(description = "Id do usuário", example = "1") Long id,
                                              AtualizarUsuarioRequest requisicao);

    @Operation(summary = "Troca a senha", description = """
            Exige a senha atual. A nova senha deve ter de 8 a 72 caracteres.""")
    @ApiResponse(responseCode = "204", description = "Senha trocada")
    @ApiResponse(responseCode = "400", description = "Nova senha fora das regras",
            content = @Content(mediaType = PROBLEMA, examples = @ExampleObject(DADOS_INVALIDOS)))
    @ApiResponse(responseCode = "401", description = "Senha atual incorreta",
            content = @Content(mediaType = PROBLEMA, examples = @ExampleObject(SENHA_ATUAL_INCORRETA)))
    @ApiResponse(responseCode = "404", description = "Usuário inexistente ou removido",
            content = @Content(mediaType = PROBLEMA, examples = @ExampleObject(USUARIO_NAO_ENCONTRADO)))
    ResponseEntity<Void> trocarSenha(@Parameter(description = "Id do usuário", example = "1") Long id,
                                     TrocarSenhaRequest requisicao);

    @Operation(summary = "Troca o tipo do usuário", description = """
            A troca acontece na mesma conta: id, login, senha e data de criação continuam iguais; mudam o tipo \
            e o documento. Dono de Restaurante informa CNPJ; os demais tipos, CPF. O documento antigo é \
            descartado. Pedir o tipo e o documento que o usuário já tem devolve 200 sem alterar nada.""")
    @ApiResponse(responseCode = "200", description = "Tipo trocado",
            content = @Content(mediaType = JSON, schema = @Schema(implementation = UsuarioResponse.class),
                    examples = @ExampleObject(ExemplosDeUsuario.USUARIO_DONO)))
    @ApiResponse(responseCode = "400", description = "Documento ausente, inválido ou incompatível com o tipo",
            content = @Content(mediaType = PROBLEMA, examples = {
                    @ExampleObject(name = "Documento do tipo", value = DOCUMENTO_DO_TIPO),
                    @ExampleObject(name = "Campos inválidos", value = DADOS_INVALIDOS)}))
    @ApiResponse(responseCode = "404", description = "Usuário ou tipo inexistente",
            content = @Content(mediaType = PROBLEMA, examples = @ExampleObject(USUARIO_NAO_ENCONTRADO)))
    @ApiResponse(responseCode = "409", description = """
            Documento usado por outro usuário, ou Dono de Restaurante com restaurante ativo tentando mudar para \
            outro tipo""",
            content = @Content(mediaType = PROBLEMA, examples = {
                    @ExampleObject(name = "Documento em uso", value = DOCUMENTO_EM_USO),
                    @ExampleObject(name = "Dono com restaurante ativo", value = DONO_COM_RESTAURANTE)}))
    ResponseEntity<UsuarioResponse> trocarTipo(@Parameter(description = "Id do usuário", example = "1") Long id,
                                               TrocarTipoRequest requisicao);

    @Operation(summary = "Lista os restaurantes de um usuário", description = """
            Só os restaurantes ativos do usuário, paginados como as demais listagens.""")
    @ApiResponse(responseCode = "200", description = "Página de restaurantes",
            content = @Content(mediaType = JSON, schema = @Schema(implementation = PaginaResponse.class),
                    examples = @ExampleObject(ExemplosDeRestaurante.PAGINA)))
    @ApiResponse(responseCode = "404", description = "Usuário inexistente ou removido",
            content = @Content(mediaType = PROBLEMA, examples = @ExampleObject(USUARIO_NAO_ENCONTRADO)))
    @Parameter(name = "page", in = ParameterIn.QUERY, description = "Número da página, a partir de 0",
            schema = @Schema(type = "integer", defaultValue = "0"))
    @Parameter(name = "size", in = ParameterIn.QUERY, description = "Itens por página, no máximo 50",
            schema = @Schema(type = "integer", defaultValue = "10"))
    @Parameter(name = "sort", in = ParameterIn.QUERY, description = "Campo e direção, como nome,desc",
            schema = @Schema(type = "string", defaultValue = "nome,asc"))
    ResponseEntity<PaginaResponse<RestauranteResponse>> listarRestaurantes(
            @Parameter(description = "Id do usuário", example = "7") Long id,
            @Parameter(hidden = true) Pageable paginacao);

    @Operation(summary = "Exclui um usuário", description = """
            Anonimiza o registro: o nome vira "Usuário removido" e os dados pessoais são apagados. Depois \
            disso o usuário não aparece nas buscas, não faz login, e o e-mail, o login e o documento ficam \
            livres para um novo cadastro. Quem é responsável por restaurante ativo precisa transferir ou \
            excluir o restaurante antes.""")
    @ApiResponse(responseCode = "204", description = "Usuário excluído")
    @ApiResponse(responseCode = "404", description = "Usuário inexistente ou já removido",
            content = @Content(mediaType = PROBLEMA, examples = @ExampleObject(USUARIO_NAO_ENCONTRADO)))
    @ApiResponse(responseCode = "409", description = "Responsável por restaurante ativo",
            content = @Content(mediaType = PROBLEMA, examples = @ExampleObject(USUARIO_COM_RESTAURANTE)))
    ResponseEntity<Void> excluir(@Parameter(description = "Id do usuário", example = "1") Long id);
}
