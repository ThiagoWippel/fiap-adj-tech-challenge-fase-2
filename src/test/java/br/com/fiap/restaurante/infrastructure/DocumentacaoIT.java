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

    private static final Pattern ORDENACAO = Pattern.compile("Não é possível ordenar por (\\w+)\\..*");

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
    @DisplayName("INF-05 · cada endpoint documenta uma resposta de sucesso e uma de erro, com exemplo da própria rota")
    void deveDocumentarExemplosDeSucessoEDeErro() {
        /* arrange */
        JsonPath especificacao = given().port(porta).get("/v3/api-docs").then().statusCode(200).extract().jsonPath();
        Map<String, Map<String, Map<String, Object>>> rotas = especificacao.getMap("paths");
        List<String> operacoes = new ArrayList<>();
        List<String> semExemplo = new ArrayList<>();
        List<String> deOutraRota = new ArrayList<>();
        List<String> ordenacaoDiferente = new ArrayList<>();

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
            // Um exemplo copiado de outro endpoint aparece pela rota do instance ou por um campo que a requisição não tem
            Pattern daRota = Pattern.compile(rota.replaceAll("\\{[^}]+}", "[^/]+"));
            Set<String> campos = camposDaRequisicao(especificacao, operacao);
            respostas.entrySet().stream().filter(r -> r.getKey().startsWith("4")).forEach(r -> exemplos(r.getValue())
                    .forEach(exemplo -> {
                        String origem = nome + " " + r.getKey() + ": ";
                        String instancia = String.valueOf(exemplo.get("instance"));
                        if (!daRota.matcher(instancia).matches()) {
                            deOutraRota.add(origem + instancia);
                        }
                        camposComErro(exemplo).stream()
                                .filter(campo -> !campos.contains(campo))
                                .forEach(campo -> deOutraRota.add(origem + "campo " + campo));
                        // A lista de campos aceitos muda de uma listagem para outra: o exemplo é conferido na API
                        Matcher ordenacao = ORDENACAO.matcher(String.valueOf(exemplo.get("detail")));
                        if (ordenacao.matches()) {
                            String devolvido = given().port(porta).queryParam("sort", ordenacao.group(1))
                                    .get(instancia).then().statusCode(400).extract().path("detail");
                            if (!devolvido.equals(exemplo.get("detail"))) {
                                ordenacaoDiferente.add(origem + devolvido);
                            }
                        }
                    }));
        }));

        /* assert */
        assertThat(operacoes).hasSize(26);
        assertThat(semExemplo).isEmpty();
        assertThat(deOutraRota).isEmpty();
        assertThat(ordenacaoDiferente).isEmpty();
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

    // O exemplo único (example) e os nomeados (examples) de cada tipo de mídia da resposta
    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> exemplos(Map<String, Object> resposta) {
        Map<String, Map<String, Object>> conteudo = (Map<String, Map<String, Object>>) resposta.get("content");
        List<Map<String, Object>> exemplos = new ArrayList<>();
        if (conteudo != null) {
            conteudo.values().forEach(midia -> {
                if (midia.get("example") instanceof Map<?, ?> unico) {
                    exemplos.add((Map<String, Object>) unico);
                }
                if (midia.get("examples") instanceof Map<?, ?> nomeados) {
                    nomeados.values().forEach(nomeado ->
                            exemplos.add((Map<String, Object>) ((Map<String, Object>) nomeado).get("value")));
                }
            });
        }
        return exemplos;
    }

    // O que um erro de campo pode citar: os parâmetros da operação e os campos do corpo
    @SuppressWarnings("unchecked")
    private static Set<String> camposDaRequisicao(JsonPath especificacao, Map<String, Object> operacao) {
        Set<String> campos = new HashSet<>();
        ((List<Map<String, Object>>) operacao.getOrDefault("parameters", List.of()))
                .forEach(parametro -> campos.add((String) parametro.get("name")));
        Map<String, Object> corpo = (Map<String, Object>) operacao.get("requestBody");
        if (corpo != null) {
            ((Map<String, Map<String, Object>>) corpo.get("content")).values().forEach(midia -> {
                String esquema = (String) ((Map<String, Object>) midia.get("schema")).get("$ref");
                String nome = esquema.substring(esquema.lastIndexOf('/') + 1);
                campos.addAll(especificacao.<String, Object>getMap("components.schemas." + nome + ".properties").keySet());
            });
        }
        return campos;
    }

    // O campo de cada erro sem o caminho dentro dele: endereco.cep conta como endereco
    @SuppressWarnings("unchecked")
    private static List<String> camposComErro(Map<String, Object> exemplo) {
        return ((List<Map<String, String>>) exemplo.getOrDefault("erros", List.of())).stream()
                .map(erro -> erro.get("campo").split("[.\\[]")[0])
                .toList();
    }

    // Respostas sem corpo (204) não têm exemplo; as que têm corpo precisam de um.
    @SuppressWarnings("unchecked")
    private static boolean temExemplo(Map<String, Object> resposta) {
        Map<String, Map<String, Object>> conteudo = (Map<String, Map<String, Object>>) resposta.get("content");
        return conteudo != null && conteudo.values().stream()
                .anyMatch(midia -> midia.containsKey("examples") || midia.containsKey("example"));
    }
}
