package br.com.fiap.restaurante.suporte;

import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

/**
 * Atalhos de REST-Assured para os testes de ponta a ponta.
 */
public final class ApiDeTeste {

    public static final String SENHA = "SenhaSegura123";

    private static final String ENDERECO = """
            {
                "rua": "Rua das Flores",
                "numero": "123",
                "complemento": "Apto 45",
                "bairro": "Centro",
                "cidade": "Itajaí",
                "estado": "sc",
                "cep": "88301-000"
              }""";

    private final int porta;

    public ApiDeTeste(int porta) {
        this.porta = porta;
    }

    public RequestSpecification requisicao() {
        return given().port(porta).contentType(ContentType.JSON);
    }

    /** Cadastra pela API e devolve o id. */
    public long cadastrar(String corpo) {
        return requisicao().body(corpo)
                .post("/api/v1/usuarios")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");
    }

    /** Cadastra o tipo pela API e devolve o id. */
    public long cadastrarTipo(String nome) {
        return requisicao().body(tipo(nome))
                .post("/api/v1/tipos-usuario")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");
    }

    /** Cadastra o restaurante pela API e devolve o id. */
    public long cadastrarRestaurante(String corpo) {
        return requisicao().body(corpo)
                .post("/api/v1/restaurantes")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");
    }

    /**
     * Restaurante com os turnos informados, cada um como "DIA HH:mm HH:mm", por
     * exemplo "SEXTA 18:00 02:00".
     */
    public static String restaurante(String nome, String tipoCozinha, long donoId, String... turnos) {
        StringBuilder horarios = new StringBuilder();
        for (String turno : turnos) {
            String[] partes = turno.split(" ");
            horarios.append(horarios.isEmpty() ? "" : ", ").append("""
                    { "diaSemana": "%s", "abertura": "%s", "fechamento": "%s" }""".formatted(partes[0], partes[1], partes[2]));
        }
        return """
                {
                  "nome": "%s",
                  "endereco": %s,
                  "tipoCozinha": "%s",
                  "donoId": %d,
                  "horarios": [%s]
                }""".formatted(nome, ENDERECO, tipoCozinha, donoId, horarios);
    }

    /** Cadastra o item no restaurante pela API e devolve o id. */
    public long cadastrarItem(long restauranteId, String corpo) {
        return requisicao().body(corpo)
                .post("/api/v1/restaurantes/{id}/itens-cardapio", restauranteId)
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");
    }

    /** O preço vai como número JSON, do jeito que é escrito aqui (39.9, 42.00...). */
    public static String item(String nome, String preco, boolean apenasNoLocal, String caminhoFoto) {
        return """
                {
                  "nome": "%s",
                  "descricao": "Prato da casa, servido com acompanhamentos.",
                  "preco": %s,
                  "apenasNoLocal": %s,
                  "caminhoFoto": "%s"
                }""".formatted(nome, preco, apenasNoLocal, caminhoFoto);
    }

    public static String cliente(String nome, String email, String login, String cpf) {
        return usuarioComCpf("CLIENTE", nome, email, login, cpf);
    }

    /** Usuário de qualquer tipo que exige CPF, inclusive os criados pelo CRUD. */
    public static String usuarioComCpf(String tipo, String nome, String email, String login, String cpf) {
        return """
                {
                  "nome": "%s",
                  "email": "%s",
                  "login": "%s",
                  "senha": "%s",
                  "tipo": "%s",
                  "cpf": "%s",
                  "endereco": %s
                }""".formatted(nome, email, login, SENHA, tipo, cpf, ENDERECO);
    }

    public static String dono(String nome, String email, String login, String cnpj) {
        return """
                {
                  "nome": "%s",
                  "email": "%s",
                  "login": "%s",
                  "senha": "%s",
                  "tipo": "DONO_RESTAURANTE",
                  "cnpj": "%s",
                  "endereco": %s
                }""".formatted(nome, email, login, SENHA, cnpj, ENDERECO);
    }

    public static String atualizacao(String nome, String email, String login) {
        return """
                {
                  "nome": "%s",
                  "email": "%s",
                  "login": "%s",
                  "endereco": %s
                }""".formatted(nome, email, login, ENDERECO);
    }

    public static String credenciais(String login, String senha) {
        return """
                { "login": "%s", "senha": "%s" }""".formatted(login, senha);
    }

    public static String tipo(String nome) {
        return """
                { "nome": "%s" }""".formatted(nome);
    }

    public static String trocaDeTipo(String tipo, String documento) {
        return """
                { "tipo": "%s", "documento": "%s" }""".formatted(tipo, documento);
    }

    public static String trocaDeSenha(String senhaAtual, String novaSenha) {
        return """
                { "senhaAtual": "%s", "novaSenha": "%s" }""".formatted(senhaAtual, novaSenha);
    }
}
