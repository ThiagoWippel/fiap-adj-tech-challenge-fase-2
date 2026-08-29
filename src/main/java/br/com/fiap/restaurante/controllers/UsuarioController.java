package br.com.fiap.restaurante.controllers;

import br.com.fiap.restaurante.dtos.request.AtualizarUsuarioRequest;
import br.com.fiap.restaurante.dtos.request.CriarUsuarioRequest;
import br.com.fiap.restaurante.dtos.request.TrocarSenhaRequest;
import br.com.fiap.restaurante.dtos.response.UsuarioResponse;
import br.com.fiap.restaurante.services.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Recursos de usuário - versão 1.
 *
 * A versão integra o caminho da rota. Entre as alternativas avaliadas -
 * cabeçalho customizado e negociação de conteúdo pelo Accept - esta é a única
 * visível na documentação Swagger e na coleção Postman, os dois artefatos onde
 * a estratégia precisa ser demonstrável.
 *
 * O controller não contém regra de negócio. Ele traduz HTTP para chamadas de
 * serviço e devolve o código de status adequado - nada além disso.
 */
@RestController
@RequestMapping("/api/v1/usuarios")
@Tag(name = "Usuarios", description = "Cadastro, consulta, atualizacao e exclusao de usuarios")
public class UsuarioController {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioController.class);

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // ------------------------------------------------------------------

    @PostMapping
    @Operation(
            summary = "Cadastra um novo usuario",
            description = """
                    Cria um usuario do tipo CLIENTE ou DONO_RESTAURANTE.

                    O documento exigido depende do tipo: CPF para cliente, CNPJ para dono de \
                    restaurante. Ambos sao gravados apenas com digitos, sem pontuacao.

                    E-mail, login e documento sao unicos no sistema."""
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Usuario cadastrado. O cabecalho Location aponta para o recurso criado.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UsuarioResponse.class),
                            examples = @ExampleObject(value = """
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
                                    }""")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Campos invalidos ou documento incompativel com o tipo informado",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "type": "https://github.com/ThiagoWippel/fiap-adj-tech-challenge-fase-1/blob/main/README.md#dados-invalidos",
                                      "title": "Dados invalidos",
                                      "status": 400,
                                      "detail": "Um ou mais campos da requisicao nao passaram na validacao",
                                      "instance": "/api/v1/usuarios",
                                      "momento": "2026-08-21T10:30:00",
                                      "erros": [
                                        { "campo": "email", "mensagem": "O e-mail informado nao e valido" },
                                        { "campo": "senha", "mensagem": "A senha deve ter entre 8 e 72 caracteres" }
                                      ]
                                    }""")
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "E-mail, login ou documento ja cadastrado",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "type": "https://github.com/ThiagoWippel/fiap-adj-tech-challenge-fase-1/blob/main/README.md#conflito-de-dados",
                                      "title": "Conflito de dados",
                                      "status": 409,
                                      "detail": "O e-mail informado ja esta cadastrado",
                                      "instance": "/api/v1/usuarios",
                                      "momento": "2026-08-21T10:30:00"
                                    }""")
                    )
            )
    })
    public ResponseEntity<UsuarioResponse> cadastrar(
            @Valid @RequestBody CriarUsuarioRequest requisicao,
            UriComponentsBuilder construtorDeUri) {

        logger.info("POST /api/v1/usuarios");
        UsuarioResponse resposta = usuarioService.cadastrar(requisicao);

        // O cabeçalho Location informa onde o recurso recém-criado pode ser
        // consultado. É a convenção REST para respostas 201 e poupa o cliente
        // de montar a URL por conta própria.
        URI localizacao = construtorDeUri
                .path("/api/v1/usuarios/{id}")
                .buildAndExpand(resposta.id())
                .toUri();

        return ResponseEntity.created(localizacao).body(resposta);
    }

    // ------------------------------------------------------------------

    @GetMapping("/{id}")
    @Operation(summary = "Consulta um usuario pelo identificador")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario inexistente",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "type": "https://github.com/ThiagoWippel/fiap-adj-tech-challenge-fase-1/blob/main/README.md#recurso-nao-encontrado",
                                      "title": "Recurso nao encontrado",
                                      "status": 404,
                                      "detail": "Usuario nao encontrado para o id 99",
                                      "instance": "/api/v1/usuarios/99",
                                      "momento": "2026-08-21T10:30:00"
                                    }""")
                    )
            )
    })
    public ResponseEntity<UsuarioResponse> buscarPorId(
            @Parameter(description = "Identificador do usuario", example = "1")
            @PathVariable Long id) {

        logger.info("GET /api/v1/usuarios/{}", id);
        return ResponseEntity.ok(usuarioService.buscarPorId(id));
    }

    // ------------------------------------------------------------------

    @GetMapping
    @Operation(
            summary = "Busca usuarios pelo nome",
            description = """
                    Retorna os usuarios cujo nome contem o termo informado, sem diferenciar \
                    maiusculas de minusculas. Omitindo o parametro, retorna todos.

                    Quando nenhum usuario corresponde ao termo, a resposta e 200 com lista \
                    vazia: a colecao filtrada existe como recurso, apenas nao contem elementos."""
    )
    @ApiResponse(responseCode = "200", description = "Lista de usuarios, possivelmente vazia")
    public ResponseEntity<List<UsuarioResponse>> buscarPorNome(
            @Parameter(description = "Termo de busca", example = "maria")
            @RequestParam(required = false) String nome) {

        logger.info("GET /api/v1/usuarios com filtro de nome: {}", (nome == null || nome.isBlank()) ? "<sem filtro>" : nome);
        return ResponseEntity.ok(usuarioService.buscarPorNome(nome));
    }

    // ------------------------------------------------------------------

    @PutMapping("/{id}")
    @Operation(
            summary = "Atualiza os dados de um usuario",
            description = """
                    Atualiza nome, e-mail, login e endereco.

                    Nao aceita senha - a troca possui endpoint proprio. Nao aceita tipo nem \
                    documento: sao imutaveis apos o cadastro, por decorrerem da identidade do \
                    usuario e nao de seus dados de contato."""
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario atualizado"),
            @ApiResponse(responseCode = "400", description = "Campos invalidos",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Usuario inexistente",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "E-mail ou login pertencente a outro usuario",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<UsuarioResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody AtualizarUsuarioRequest requisicao) {

        logger.info("PUT /api/v1/usuarios/{}", id);
        return ResponseEntity.ok(usuarioService.atualizar(id, requisicao));
    }

    // ------------------------------------------------------------------

    @PutMapping("/{id}/senha")
    @Operation(
            summary = "Troca a senha do usuario",
            description = """
                    Endpoint exclusivo para troca de senha, separado da atualizacao dos demais \
                    dados conforme exige o enunciado.

                    **Exige o envio da senha atual.** Trata-se de decisao de seguranca do \
                    projeto - o enunciado nao a menciona - adotada como protecao contra \
                    alteracao indevida em sessao deixada aberta.

                    O verbo PUT foi escolhido por substituir a senha integralmente. PATCH nao \
                    consta do material da disciplina e nao se aplica: nao ha modificacao \
                    parcial de uma senha."""
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Senha alterada"),
            @ApiResponse(responseCode = "400", description = "Nova senha fora das regras",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(
                    responseCode = "401",
                    description = "Senha atual incorreta",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "type": "https://github.com/ThiagoWippel/fiap-adj-tech-challenge-fase-1/blob/main/README.md#credenciais-invalidas",
                                      "title": "Credenciais invalidas",
                                      "status": 401,
                                      "detail": "A senha atual informada esta incorreta",
                                      "instance": "/api/v1/usuarios/1/senha",
                                      "momento": "2026-08-21T10:30:00"
                                    }""")
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Usuario inexistente",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<Void> trocarSenha(
            @PathVariable Long id,
            @Valid @RequestBody TrocarSenhaRequest requisicao) {

        logger.info("PUT /api/v1/usuarios/{}/senha", id);
        usuarioService.trocarSenha(id, requisicao);

        // 204: a operação teve êxito e não há o que devolver. Retornar
        // qualquer informação relacionada a senha seria indesejável.
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------------

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Exclui um usuario",
            description = """
                    Remove definitivamente o registro.

                    A exclusao logica foi avaliada e descartada: manter o registro inativo \
                    preservaria o e-mail ocupado na restricao de unicidade, impedindo que a \
                    mesma pessoa se recadastrasse."""
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuario excluido"),
            @ApiResponse(responseCode = "404", description = "Usuario inexistente",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        logger.info("DELETE /api/v1/usuarios/{}", id);
        usuarioService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
