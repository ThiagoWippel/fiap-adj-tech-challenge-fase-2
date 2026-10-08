package br.com.fiap.restaurante.infrastructure;

import br.com.fiap.restaurante.suporte.TesteDeIntegracao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

@TesteDeIntegracao
@DisplayName("Documentação da API")
class DocumentacaoIT {

    @LocalServerPort
    private int porta;

    @Test
    @DisplayName("INF-05 · a especificação OpenAPI responde em /v3/api-docs com o título e a política de versionamento")
    void deveServirAEspecificacaoOpenApi() {
        given()
                .port(porta)
        .when()
                .get("/v3/api-docs")
        .then()
                .statusCode(200)
                .body("info.title", equalTo("API de Gestão de Restaurantes"))
                .body("info.description", containsString("Versionamento"));
    }
}
