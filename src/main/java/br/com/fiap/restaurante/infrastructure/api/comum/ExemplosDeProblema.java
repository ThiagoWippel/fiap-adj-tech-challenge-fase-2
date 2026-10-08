package br.com.fiap.restaurante.infrastructure.api.comum;

/**
 * Exemplos de erro usados na documentação OpenAPI.
 */
public final class ExemplosDeProblema {

    public static final String DADOS_INVALIDOS = """
            {
              "type": "http://localhost:8080/problemas/dados-invalidos",
              "title": "Dados inválidos",
              "status": 400,
              "detail": "Um ou mais campos da requisição não passaram na validação.",
              "instance": "/api/v1/usuarios",
              "momento": "2026-10-08T10:30:00",
              "erros": [
                { "campo": "email", "mensagem": "O e-mail informado não é válido." },
                { "campo": "senha", "mensagem": "A senha deve ter entre 8 e 72 caracteres." }
              ]
            }""";

    public static final String REGRA_DE_NEGOCIO = """
            {
              "type": "http://localhost:8080/problemas/regra-de-negocio",
              "title": "Regra de negócio violada",
              "status": 400,
              "detail": "O CPF é obrigatório para usuários do tipo Cliente.",
              "instance": "/api/v1/usuarios",
              "momento": "2026-10-08T10:30:00"
            }""";

    public static final String ORDENACAO_INVALIDA = """
            {
              "type": "http://localhost:8080/problemas/requisicao-invalida",
              "title": "Requisição inválida",
              "status": 400,
              "detail": "Não é possível ordenar por senha. Campos aceitos: dataCriacao, dataUltimaAlteracao, email, id, login, nome.",
              "instance": "/api/v2/usuarios",
              "momento": "2026-10-08T10:30:00"
            }""";

    public static final String CREDENCIAIS_INVALIDAS = """
            {
              "type": "http://localhost:8080/problemas/credenciais-invalidas",
              "title": "Credenciais inválidas",
              "status": 401,
              "detail": "Login ou senha inválidos.",
              "instance": "/api/v1/auth/login",
              "momento": "2026-10-08T10:30:00"
            }""";

    public static final String SENHA_ATUAL_INCORRETA = """
            {
              "type": "http://localhost:8080/problemas/credenciais-invalidas",
              "title": "Credenciais inválidas",
              "status": 401,
              "detail": "A senha atual informada está incorreta.",
              "instance": "/api/v1/usuarios/1/senha",
              "momento": "2026-10-08T10:30:00"
            }""";

    public static final String USUARIO_NAO_ENCONTRADO = """
            {
              "type": "http://localhost:8080/problemas/recurso-nao-encontrado",
              "title": "Recurso não encontrado",
              "status": 404,
              "detail": "Usuário 99 não encontrado.",
              "instance": "/api/v1/usuarios/99",
              "momento": "2026-10-08T10:30:00"
            }""";

    public static final String TIPO_NAO_ENCONTRADO = """
            {
              "type": "http://localhost:8080/problemas/recurso-nao-encontrado",
              "title": "Recurso não encontrado",
              "status": 404,
              "detail": "Tipo de usuário ENTREGADOR não encontrado.",
              "instance": "/api/v1/usuarios",
              "momento": "2026-10-08T10:30:00"
            }""";

    public static final String EMAIL_EM_USO = """
            {
              "type": "http://localhost:8080/problemas/conflito-de-dados",
              "title": "Conflito de dados",
              "status": 409,
              "detail": "O e-mail informado já está cadastrado.",
              "instance": "/api/v1/usuarios",
              "momento": "2026-10-08T10:30:00"
            }""";

    private ExemplosDeProblema() {
    }
}
