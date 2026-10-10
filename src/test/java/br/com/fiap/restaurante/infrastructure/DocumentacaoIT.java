package br.com.fiap.restaurante.infrastructure;

import br.com.fiap.restaurante.suporte.TesteDeIntegracao;
import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.io.File;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

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

    @Test
    @DisplayName("ACE-03 · os exemplos do Swagger não repetem e-mail, login, documento nem tipo da collection")
    void naoDeveRepetirOsDadosDaCollection() {
        /* arrange */
        JsonPath especificacao = given().port(porta).get("/v3/api-docs").then().statusCode(200).extract().jsonPath();
        List<String> doSwagger = Stream.of("CriarUsuarioRequest.properties.email",
                        "CriarUsuarioRequest.properties.login", "CriarUsuarioRequest.properties.cpf",
                        "CriarUsuarioRequest.properties.cnpj",
                        "AtualizarUsuarioRequest.properties.email", "AtualizarUsuarioRequest.properties.login",
                        "LoginRequest.properties.login", "TrocarTipoRequest.properties.documento",
                        "TipoUsuarioRequest.properties.nome")
                .map(campo -> normalizar(especificacao.getString("components.schemas." + campo + ".example")))
                .toList();
        List<String> corpos = JsonPath.from(new File("postman/tech-challenge-fase-2.postman_collection.json"))
                .getList("item.item.flatten().request.body.raw", String.class);
        Set<String> daCollection = new HashSet<>();
        Pattern unico = Pattern.compile("\"(email|login|cpf|cnpj|documento)\"\\s*:\\s*\"([^\"]*)\"");
        Pattern tipo = Pattern.compile("^\\s*\\{\\s*\"nome\"\\s*:\\s*\"([^\"]*)\"\\s*}\\s*$");

        /* act */
        corpos.stream().filter(Objects::nonNull).forEach(corpo -> {
            Matcher valor = unico.matcher(corpo);
            while (valor.find()) {
                daCollection.add(normalizar(valor.group(2)));
            }
            Matcher nomeDoTipo = tipo.matcher(corpo);
            if (nomeDoTipo.matches()) {
                daCollection.add(normalizar(nomeDoTipo.group(1)));
            }
        });

        /* assert */
        assertThat(daCollection).isNotEmpty();
        assertThat(doSwagger).doesNotContainAnyElementsOf(daCollection);
    }

    // Como o banco compara, sem maiúscula nem acento; sem pontuação, para o documento com e sem máscara coincidir
    private static String normalizar(String valor) {
        String semAcento = Normalizer.normalize(valor, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return semAcento.toLowerCase(Locale.ROOT).replaceAll("[./-]", "");
    }

    // Respostas sem corpo (204) não têm exemplo; as que têm corpo precisam de um.
    @SuppressWarnings("unchecked")
    private static boolean temExemplo(Map<String, Object> resposta) {
        Map<String, Map<String, Object>> conteudo = (Map<String, Map<String, Object>>) resposta.get("content");
        return conteudo != null && conteudo.values().stream()
                .anyMatch(midia -> midia.containsKey("examples") || midia.containsKey("example"));
    }
}
