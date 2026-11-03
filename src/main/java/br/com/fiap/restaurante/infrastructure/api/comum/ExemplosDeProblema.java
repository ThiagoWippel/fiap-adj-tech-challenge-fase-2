package br.com.fiap.restaurante.infrastructure.api.comum;

/**
 * Exemplos de erro usados na documentação OpenAPI. Cada exemplo traz o instance e os
 * campos da operação em que aparece, por isso operações parecidas não dividem o mesmo.
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
              "detail": "Tipo de usuário GARCOM não encontrado.",
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

    public static final String TIPO_INEXISTENTE = """
            {
              "type": "http://localhost:8080/problemas/recurso-nao-encontrado",
              "title": "Recurso não encontrado",
              "status": 404,
              "detail": "Tipo de usuário 99 não encontrado.",
              "instance": "/api/v1/tipos-usuario/99",
              "momento": "2026-10-08T10:30:00"
            }""";

    public static final String NOME_DE_TIPO_EM_USO = """
            {
              "type": "http://localhost:8080/problemas/conflito-de-dados",
              "title": "Conflito de dados",
              "status": 409,
              "detail": "Já existe um tipo de usuário com o nome Garçom.",
              "instance": "/api/v1/tipos-usuario",
              "momento": "2026-10-08T10:30:00"
            }""";

    public static final String TIPO_EM_USO = """
            {
              "type": "http://localhost:8080/problemas/conflito-de-dados",
              "title": "Conflito de dados",
              "status": 409,
              "detail": "O tipo Garçom não pode ser excluído: 2 usuários ativos o usam.",
              "instance": "/api/v1/tipos-usuario/3",
              "momento": "2026-10-08T10:30:00"
            }""";

    public static final String TIPO_DE_SISTEMA = """
            {
              "type": "http://localhost:8080/problemas/conflito-de-dados",
              "title": "Conflito de dados",
              "status": 409,
              "detail": "O tipo Cliente é um tipo de sistema e não pode ser excluído.",
              "instance": "/api/v1/tipos-usuario/1",
              "momento": "2026-10-08T10:30:00"
            }""";

    public static final String DOCUMENTO_DO_TIPO = """
            {
              "type": "http://localhost:8080/problemas/regra-de-negocio",
              "title": "Regra de negócio violada",
              "status": 400,
              "detail": "Usuário do tipo Dono de Restaurante deve informar CNPJ.",
              "instance": "/api/v1/usuarios/1/tipo",
              "momento": "2026-10-08T10:30:00"
            }""";

    public static final String DOCUMENTO_EM_USO = """
            {
              "type": "http://localhost:8080/problemas/conflito-de-dados",
              "title": "Conflito de dados",
              "status": 409,
              "detail": "O documento informado já está cadastrado.",
              "instance": "/api/v1/usuarios/1/tipo",
              "momento": "2026-10-08T10:30:00"
            }""";

    public static final String RESTAURANTE_NAO_ENCONTRADO = """
            {
              "type": "http://localhost:8080/problemas/recurso-nao-encontrado",
              "title": "Recurso não encontrado",
              "status": 404,
              "detail": "Restaurante 99 não encontrado.",
              "instance": "/api/v1/restaurantes/99",
              "momento": "2026-10-08T10:30:00"
            }""";

    public static final String DONO_NAO_E_DONO = """
            {
              "type": "http://localhost:8080/problemas/conflito-de-dados",
              "title": "Conflito de dados",
              "status": 409,
              "detail": "O usuário 7 não é Dono de Restaurante. Troque o tipo dele antes de cadastrar o restaurante.",
              "instance": "/api/v1/restaurantes",
              "momento": "2026-10-08T10:30:00"
            }""";

    public static final String TURNOS_SOBREPOSTOS = """
            {
              "type": "http://localhost:8080/problemas/regra-de-negocio",
              "title": "Regra de negócio violada",
              "status": 400,
              "detail": "Os turnos SEXTA 18:00–02:00 e SABADO 01:00–10:00 se sobrepõem.",
              "instance": "/api/v1/restaurantes",
              "momento": "2026-10-08T10:30:00"
            }""";

    public static final String COZINHA_INVALIDA = """
            {
              "type": "http://localhost:8080/problemas/dados-invalidos",
              "title": "Dados inválidos",
              "status": 400,
              "detail": "O tipo de cozinha TAILANDESA não existe. Valores aceitos: BRASILEIRA, ITALIANA, PIZZARIA, JAPONESA, CHINESA, ARABE, MEXICANA, PORTUGUESA, FRANCESA, HAMBURGUERIA, LANCHES, CHURRASCARIA, FRUTOS_DO_MAR, VEGETARIANA, VEGANA, DOCES_E_SOBREMESAS, CAFETERIA, OUTRA.",
              "instance": "/api/v1/restaurantes",
              "momento": "2026-10-08T10:30:00"
            }""";

    public static final String USUARIO_COM_RESTAURANTE = """
            {
              "type": "http://localhost:8080/problemas/conflito-de-dados",
              "title": "Conflito de dados",
              "status": 409,
              "detail": "O usuário 7 é responsável por 1 restaurante ativo. Transfira ou exclua o restaurante antes de excluir o usuário.",
              "instance": "/api/v1/usuarios/7",
              "momento": "2026-10-08T10:30:00"
            }""";

    public static final String DONO_COM_RESTAURANTE = """
            {
              "type": "http://localhost:8080/problemas/conflito-de-dados",
              "title": "Conflito de dados",
              "status": 409,
              "detail": "O usuário 7 é responsável por 1 restaurante ativo e só deixa de ser Dono de Restaurante depois de transferir ou excluir o restaurante.",
              "instance": "/api/v1/usuarios/7/tipo",
              "momento": "2026-10-08T10:30:00"
            }""";

    public static final String ITEM_NAO_ENCONTRADO = """
            {
              "type": "http://localhost:8080/problemas/recurso-nao-encontrado",
              "title": "Recurso não encontrado",
              "status": 404,
              "detail": "Item 5 não encontrado no restaurante 9.",
              "instance": "/api/v1/restaurantes/9/itens-cardapio/5",
              "momento": "2026-10-08T10:30:00"
            }""";

    public static final String NOME_DE_ITEM_EM_USO = """
            {
              "type": "http://localhost:8080/problemas/conflito-de-dados",
              "title": "Conflito de dados",
              "status": 409,
              "detail": "O restaurante 9 já tem um item ativo chamado Feijoada.",
              "instance": "/api/v1/restaurantes/9/itens-cardapio",
              "momento": "2026-10-08T10:30:00"
            }""";

    public static final String DADOS_DO_ITEM_INVALIDOS = """
            {
              "type": "http://localhost:8080/problemas/dados-invalidos",
              "title": "Dados inválidos",
              "status": 400,
              "detail": "Um ou mais campos da requisição não passaram na validação.",
              "instance": "/api/v1/restaurantes/9/itens-cardapio",
              "momento": "2026-10-08T10:30:00",
              "erros": [
                { "campo": "apenasNoLocal", "mensagem": "Informe se o item está disponível só para consumo no local." },
                { "campo": "preco", "mensagem": "O preço deve ter no máximo duas casas decimais." }
              ]
            }""";

    public static final String CODIFICACAO_INVALIDA = """
            {
              "type": "http://localhost:8080/problemas/requisicao-invalida",
              "title": "Requisição inválida",
              "status": 400,
              "detail": "Um parâmetro da URL tem codificação inválida. Confira os caracteres com %.",
              "instance": "/api/v1/usuarios",
              "momento": "2026-10-08T10:30:00"
            }""";

    public static final String DADOS_DA_ATUALIZACAO_INVALIDOS = """
            {
              "type": "http://localhost:8080/problemas/dados-invalidos",
              "title": "Dados inválidos",
              "status": 400,
              "detail": "Um ou mais campos da requisição não passaram na validação.",
              "instance": "/api/v1/usuarios/1",
              "momento": "2026-10-08T10:30:00",
              "erros": [
                { "campo": "email", "mensagem": "O e-mail informado não é válido." },
                { "campo": "login", "mensagem": "O login deve ter de 4 a 50 caracteres: letras, números, ponto, hífen ou sublinhado." }
              ]
            }""";

    public static final String EMAIL_EM_USO_NA_ATUALIZACAO = """
            {
              "type": "http://localhost:8080/problemas/conflito-de-dados",
              "title": "Conflito de dados",
              "status": 409,
              "detail": "O e-mail informado já está cadastrado.",
              "instance": "/api/v1/usuarios/1",
              "momento": "2026-10-08T10:30:00"
            }""";

    public static final String NOVA_SENHA_INVALIDA = """
            {
              "type": "http://localhost:8080/problemas/dados-invalidos",
              "title": "Dados inválidos",
              "status": 400,
              "detail": "Um ou mais campos da requisição não passaram na validação.",
              "instance": "/api/v1/usuarios/1/senha",
              "momento": "2026-10-08T10:30:00",
              "erros": [
                { "campo": "novaSenha", "mensagem": "A nova senha deve ter entre 8 e 72 caracteres." }
              ]
            }""";

    public static final String USUARIO_NAO_ENCONTRADO_NA_SENHA = """
            {
              "type": "http://localhost:8080/problemas/recurso-nao-encontrado",
              "title": "Recurso não encontrado",
              "status": 404,
              "detail": "Usuário 99 não encontrado.",
              "instance": "/api/v1/usuarios/99/senha",
              "momento": "2026-10-08T10:30:00"
            }""";

    public static final String DADOS_DA_TROCA_DE_TIPO_INVALIDOS = """
            {
              "type": "http://localhost:8080/problemas/dados-invalidos",
              "title": "Dados inválidos",
              "status": 400,
              "detail": "Um ou mais campos da requisição não passaram na validação.",
              "instance": "/api/v1/usuarios/1/tipo",
              "momento": "2026-10-08T10:30:00",
              "erros": [
                { "campo": "documento", "mensagem": "O documento é obrigatório." },
                { "campo": "tipo", "mensagem": "O tipo é obrigatório." }
              ]
            }""";

    public static final String USUARIO_NAO_ENCONTRADO_NA_TROCA_DE_TIPO = """
            {
              "type": "http://localhost:8080/problemas/recurso-nao-encontrado",
              "title": "Recurso não encontrado",
              "status": 404,
              "detail": "Usuário 99 não encontrado.",
              "instance": "/api/v1/usuarios/99/tipo",
              "momento": "2026-10-08T10:30:00"
            }""";

    public static final String USUARIO_NAO_ENCONTRADO_NOS_RESTAURANTES = """
            {
              "type": "http://localhost:8080/problemas/recurso-nao-encontrado",
              "title": "Recurso não encontrado",
              "status": 404,
              "detail": "Usuário 99 não encontrado.",
              "instance": "/api/v1/usuarios/99/restaurantes",
              "momento": "2026-10-08T10:30:00"
            }""";

    public static final String LOGIN_INCOMPLETO = """
            {
              "type": "http://localhost:8080/problemas/dados-invalidos",
              "title": "Dados inválidos",
              "status": 400,
              "detail": "Um ou mais campos da requisição não passaram na validação.",
              "instance": "/api/v1/auth/login",
              "momento": "2026-10-08T10:30:00",
              "erros": [
                { "campo": "login", "mensagem": "O login é obrigatório." },
                { "campo": "senha", "mensagem": "A senha é obrigatória." }
              ]
            }""";

    public static final String NOME_DE_TIPO_INVALIDO = """
            {
              "type": "http://localhost:8080/problemas/dados-invalidos",
              "title": "Dados inválidos",
              "status": 400,
              "detail": "Um ou mais campos da requisição não passaram na validação.",
              "instance": "/api/v1/tipos-usuario",
              "momento": "2026-10-08T10:30:00",
              "erros": [
                { "campo": "nome", "mensagem": "O nome do tipo deve ter entre 3 e 50 caracteres." }
              ]
            }""";

    public static final String NOME_DE_TIPO_INVALIDO_NA_RENOMEACAO = """
            {
              "type": "http://localhost:8080/problemas/dados-invalidos",
              "title": "Dados inválidos",
              "status": 400,
              "detail": "Um ou mais campos da requisição não passaram na validação.",
              "instance": "/api/v1/tipos-usuario/3",
              "momento": "2026-10-08T10:30:00",
              "erros": [
                { "campo": "nome", "mensagem": "O nome do tipo deve ter entre 3 e 50 caracteres." }
              ]
            }""";

    public static final String NOME_DE_TIPO_EM_USO_NA_RENOMEACAO = """
            {
              "type": "http://localhost:8080/problemas/conflito-de-dados",
              "title": "Conflito de dados",
              "status": 409,
              "detail": "Já existe um tipo de usuário com o nome Garçom.",
              "instance": "/api/v1/tipos-usuario/3",
              "momento": "2026-10-08T10:30:00"
            }""";

    public static final String ORDENACAO_DE_TIPOS_INVALIDA = """
            {
              "type": "http://localhost:8080/problemas/requisicao-invalida",
              "title": "Requisição inválida",
              "status": 400,
              "detail": "Não é possível ordenar por dataCriacao. Campos aceitos: codigo, id, nome.",
              "instance": "/api/v1/tipos-usuario",
              "momento": "2026-10-08T10:30:00"
            }""";

    public static final String TIPO_INEXISTENTE_NOS_USUARIOS = """
            {
              "type": "http://localhost:8080/problemas/recurso-nao-encontrado",
              "title": "Recurso não encontrado",
              "status": 404,
              "detail": "Tipo de usuário 99 não encontrado.",
              "instance": "/api/v1/tipos-usuario/99/usuarios",
              "momento": "2026-10-08T10:30:00"
            }""";

    public static final String DADOS_DO_RESTAURANTE_INVALIDOS = """
            {
              "type": "http://localhost:8080/problemas/dados-invalidos",
              "title": "Dados inválidos",
              "status": 400,
              "detail": "Um ou mais campos da requisição não passaram na validação.",
              "instance": "/api/v1/restaurantes",
              "momento": "2026-10-08T10:30:00",
              "erros": [
                { "campo": "endereco.cep", "mensagem": "O CEP deve ter 8 dígitos." },
                { "campo": "horarios", "mensagem": "Informe de 1 a 50 turnos de funcionamento." }
              ]
            }""";

    public static final String DONO_NAO_ENCONTRADO = """
            {
              "type": "http://localhost:8080/problemas/recurso-nao-encontrado",
              "title": "Recurso não encontrado",
              "status": 404,
              "detail": "Usuário 99 não encontrado.",
              "instance": "/api/v1/restaurantes",
              "momento": "2026-10-08T10:30:00"
            }""";

    public static final String ORDENACAO_DE_RESTAURANTES_INVALIDA = """
            {
              "type": "http://localhost:8080/problemas/requisicao-invalida",
              "title": "Requisição inválida",
              "status": 400,
              "detail": "Não é possível ordenar por dono. Campos aceitos: dataCriacao, dataUltimaAlteracao, id, nome, tipoCozinha.",
              "instance": "/api/v1/restaurantes",
              "momento": "2026-10-08T10:30:00"
            }""";

    public static final String TURNOS_SOBREPOSTOS_NA_ATUALIZACAO = """
            {
              "type": "http://localhost:8080/problemas/regra-de-negocio",
              "title": "Regra de negócio violada",
              "status": 400,
              "detail": "Os turnos SEXTA 18:00–02:00 e SABADO 01:00–10:00 se sobrepõem.",
              "instance": "/api/v1/restaurantes/9",
              "momento": "2026-10-08T10:30:00"
            }""";

    public static final String DONO_NAO_E_DONO_NA_TRANSFERENCIA = """
            {
              "type": "http://localhost:8080/problemas/conflito-de-dados",
              "title": "Conflito de dados",
              "status": 409,
              "detail": "O usuário 7 não é Dono de Restaurante. Troque o tipo dele antes de transferir o restaurante.",
              "instance": "/api/v1/restaurantes/9",
              "momento": "2026-10-08T10:30:00"
            }""";

    public static final String RESTAURANTE_NAO_ENCONTRADO_NO_CARDAPIO = """
            {
              "type": "http://localhost:8080/problemas/recurso-nao-encontrado",
              "title": "Recurso não encontrado",
              "status": 404,
              "detail": "Restaurante 99 não encontrado.",
              "instance": "/api/v1/restaurantes/99/itens-cardapio",
              "momento": "2026-10-08T10:30:00"
            }""";

    public static final String ORDENACAO_DO_CARDAPIO_INVALIDA = """
            {
              "type": "http://localhost:8080/problemas/requisicao-invalida",
              "title": "Requisição inválida",
              "status": 400,
              "detail": "Não é possível ordenar por descricao. Campos aceitos: dataCriacao, dataUltimaAlteracao, id, nome, preco.",
              "instance": "/api/v1/restaurantes/9/itens-cardapio",
              "momento": "2026-10-08T10:30:00"
            }""";

    public static final String DADOS_DO_ITEM_INVALIDOS_NA_ATUALIZACAO = """
            {
              "type": "http://localhost:8080/problemas/dados-invalidos",
              "title": "Dados inválidos",
              "status": 400,
              "detail": "Um ou mais campos da requisição não passaram na validação.",
              "instance": "/api/v1/restaurantes/9/itens-cardapio/5",
              "momento": "2026-10-08T10:30:00",
              "erros": [
                { "campo": "apenasNoLocal", "mensagem": "Informe se o item está disponível só para consumo no local." },
                { "campo": "preco", "mensagem": "O preço deve ter no máximo duas casas decimais." }
              ]
            }""";

    public static final String NOME_DE_ITEM_EM_USO_NA_ATUALIZACAO = """
            {
              "type": "http://localhost:8080/problemas/conflito-de-dados",
              "title": "Conflito de dados",
              "status": 409,
              "detail": "O restaurante 9 já tem um item ativo chamado Feijoada.",
              "instance": "/api/v1/restaurantes/9/itens-cardapio/5",
              "momento": "2026-10-08T10:30:00"
            }""";

    private ExemplosDeProblema() {
    }
}
