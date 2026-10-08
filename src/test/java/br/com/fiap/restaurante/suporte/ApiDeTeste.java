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

    public static String cliente(String nome, String email, String login, String cpf) {
        return """
                {
                  "nome": "%s",
                  "email": "%s",
                  "login": "%s",
                  "senha": "%s",
                  "tipo": "CLIENTE",
                  "cpf": "%s",
                  "endereco": %s
                }""".formatted(nome, email, login, SENHA, cpf, ENDERECO);
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

    public static String trocaDeSenha(String senhaAtual, String novaSenha) {
        return """
                { "senhaAtual": "%s", "novaSenha": "%s" }""".formatted(senhaAtual, novaSenha);
    }
}
