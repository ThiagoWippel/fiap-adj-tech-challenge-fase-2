package br.com.fiap.restaurante.infrastructure;

import br.com.fiap.restaurante.suporte.TesteDeIntegracao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@TesteDeIntegracao
@DisplayName("Saúde da aplicação")
class SaudeIT {

    @LocalServerPort
    private int porta;

    @Test
    @DisplayName("INF-02 · /actuator/health responde UP com o banco no ar")
    void deveResponderUp_QuandoOBancoEstiverNoAr() {
        given()
                .port(porta)
        .when()
                .get("/actuator/health")
        .then()
                .statusCode(200)
                .body("status", equalTo("UP"));
    }
}
