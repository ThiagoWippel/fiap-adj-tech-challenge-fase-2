package br.com.fiap.restaurante.infrastructure;

import br.com.fiap.restaurante.suporte.TesteDeIntegracao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
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

    @Test
    @DisplayName("INF-05 · cada endpoint documenta uma resposta de sucesso e uma de erro, com exemplo")
    void deveDocumentarExemplosDeSucessoEDeErro() {
        /* arrange */
        Map<String, Map<String, Map<String, Object>>> rotas = given().port(porta)
                .get("/v3/api-docs")
                .then().statusCode(200)
                .extract().jsonPath().getMap("paths");
        List<String> operacoes = new ArrayList<>();
        List<String> semExemplo = new ArrayList<>();

        /* act */
        rotas.forEach((rota, metodos) -> metodos.forEach((metodo, operacao) -> {
            String nome = metodo.toUpperCase() + " " + rota;
            operacoes.add(nome);
            @SuppressWarnings("unchecked")
            Map<String, Map<String, Object>> respostas = (Map<String, Map<String, Object>>) operacao.get("responses");
            boolean sucesso = respostas.entrySet().stream()
                    .anyMatch(r -> r.getKey().startsWith("2") && (r.getValue().get("content") == null || temExemplo(r.getValue())));
            boolean erro = respostas.entrySet().stream()
                    .anyMatch(r -> r.getKey().startsWith("4") && temExemplo(r.getValue()));
            if (!sucesso || !erro) {
                semExemplo.add(nome);
            }
        }));

        /* assert */
        assertThat(operacoes).hasSize(26);
        assertThat(semExemplo).isEmpty();
    }

    // Respostas sem corpo (204) não têm exemplo; as que têm corpo precisam de um.
    @SuppressWarnings("unchecked")
    private static boolean temExemplo(Map<String, Object> resposta) {
        Map<String, Map<String, Object>> conteudo = (Map<String, Map<String, Object>>) resposta.get("content");
        return conteudo != null && conteudo.values().stream()
                .anyMatch(midia -> midia.containsKey("examples") || midia.containsKey("example"));
    }
}
