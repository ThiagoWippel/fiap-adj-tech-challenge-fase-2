package br.com.fiap.restaurante.infrastructure.api;

import br.com.fiap.restaurante.suporte.ApiDeTeste;
import br.com.fiap.restaurante.suporte.LimpezaDoBanco;
import br.com.fiap.restaurante.suporte.TesteDeIntegracao;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import static br.com.fiap.restaurante.suporte.ApiDeTeste.cliente;
import static br.com.fiap.restaurante.suporte.ApiDeTeste.credenciais;
import static br.com.fiap.restaurante.suporte.ApiDeTeste.dono;
import static br.com.fiap.restaurante.suporte.ApiDeTeste.item;
import static br.com.fiap.restaurante.suporte.ApiDeTeste.restaurante;
import static br.com.fiap.restaurante.suporte.ApiDeTeste.tipo;
import static br.com.fiap.restaurante.suporte.ApiDeTeste.trocaDeSenha;
import static br.com.fiap.restaurante.suporte.ApiDeTeste.trocaDeTipo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.startsWith;

/**
 * O que a API aceita na entrada: tipos do JSON sem conversão implícita, limite de
 * tamanho em todo texto, texto sem caracteres de controle e corpo de até 1 MB.
 */
@TesteDeIntegracao
@DisplayName("Entrada da API")
class EntradaDaApiIT {

    private static final String ITENS = "/api/v1/restaurantes/{restauranteId}/itens-cardapio";

    @LocalServerPort
    private int porta;

    @Autowired
    private JdbcTemplate jdbc;

    private ApiDeTeste api;
    private long ana;
    private long cantina;

    @BeforeEach
    void preparar() {
        LimpezaDoBanco.limpar(jdbc);
        api = new ApiDeTeste(porta);
        ana = api.cadastrar(dono("Ana Souza", "ana@exemplo.com", "ana.souza", "11222333000181"));
        cantina = api.cadastrarRestaurante(restaurante("Cantina da Nona", "ITALIANA", ana, "SEGUNDA 11:00 15:00"));
    }

    @AfterEach
    void restaurar() {
        LimpezaDoBanco.limpar(jdbc);
    }

    @Test
    @DisplayName("ENT-03 · número no lugar de texto, texto no lugar de número e 1 no lugar de true devolvem 400")
    void deveRecusarConversaoImplicitaDeTipos() {
        api.requisicao().body("""
                        { "nome": 123 }""").post("/api/v1/tipos-usuario")
                .then().statusCode(400)
                .body("erros[0].campo", equalTo("nome")).body("erros[0].mensagem", equalTo("Informe um texto."));
        api.requisicao().body(item("Feijoada", "\"39.90\"", false, "fotos/feijoada.jpg")).post(ITENS, cantina)
                .then().statusCode(400)
                .body("erros[0].campo", equalTo("preco")).body("erros[0].mensagem", equalTo("Informe um número."));
        api.requisicao().body(item("Feijoada", "39.90", false, "fotos/feijoada.jpg").replace("false", "1"))
                .post(ITENS, cantina)
                .then().statusCode(400)
                .body("erros[0].campo", equalTo("apenasNoLocal"))
                .body("erros[0].mensagem", equalTo("Informe true ou false."));
    }

    @Test
    @DisplayName("ENT-03 · id com casas decimais ou em texto, mesmo com dígitos de largura dupla, devolve 400")
    void deveRecusarIdQueNaoSejaInteiro() {
        for (String donoId : new String[] {"1.5", "\"" + ana + "\"", "\"１２３\""}) {
            api.requisicao().body(restaurante("Bistrô", "FRANCESA", ana, "TERCA 18:00 23:00")
                            .replace("\"donoId\": " + ana, "\"donoId\": " + donoId))
                    .post("/api/v1/restaurantes")
                    .then().statusCode(400)
                    .body("erros[0].campo", equalTo("donoId"))
                    .body("erros[0].mensagem", equalTo("Informe um número inteiro."));
        }
    }

    @Test
    @DisplayName("ENT-04 · todo texto do corpo tem tamanho máximo, e o excesso devolve 400 apontando o campo")
    void deveLimitarOTamanhoDeTodoTexto() {
        String longo = "x".repeat(100);
        api.requisicao().body(cliente("Maria Silva", "maria@exemplo.com", "maria.silva", "12345678909")
                        .replace("\"CLIENTE\"", "\"" + longo + "\""))
                .post("/api/v1/usuarios")
                .then().statusCode(400).body("erros.campo", hasItem("tipo"));
        api.requisicao().body(credenciais(longo, longo)).post("/api/v1/auth/login")
                .then().statusCode(400).body("erros.campo", hasItem("login")).body("erros.campo", hasItem("senha"));
        api.requisicao().body(trocaDeSenha(longo, "SenhaNova456")).put("/api/v1/usuarios/{id}/senha", ana)
                .then().statusCode(400).body("erros.campo", hasItem("senhaAtual"));
        api.requisicao().body(trocaDeTipo(longo, longo)).patch("/api/v1/usuarios/{id}/tipo", ana)
                .then().statusCode(400).body("erros.campo", hasItem("tipo")).body("erros.campo", hasItem("documento"));
        api.requisicao().body(restaurante("Bistrô", longo, ana, longo + " 18:00 23:00"))
                .post("/api/v1/restaurantes")
                .then().statusCode(400)
                .body("erros.campo", hasItem("tipoCozinha")).body("erros.campo", hasItem("horarios[0].diaSemana"));
    }

    @Test
    @DisplayName("ENT-05 · texto com caractere de controle devolve 400; a descrição do item aceita quebra de linha")
    void deveRecusarCaractereDeControle() {
        api.requisicao().body(tipo("Entregador\\u0000")).post("/api/v1/tipos-usuario")
                .then().statusCode(400)
                .body("detail", equalTo("O campo nome do tipo não aceita quebra de linha nem caracteres de controle."));
        api.requisicao().body(item("Feijoada", "39.90", false, "fotos/feijoada.jpg")
                        .replace("Prato da casa,", "Prato da casa,\\n"))
                .post(ITENS, cantina)
                .then().statusCode(201);
    }

    @Test
    @DisplayName("ENT-06 · corpo acima de 1 MB devolve 413 em ProblemDetail, com ou sem Content-Length")
    void deveRecusarCorpoGrandeDemais() throws IOException {
        /* arrange */
        String corpo = "{ \"nome\": \"" + "x".repeat(1_100_000) + "\" }";

        /* act + assert */
        api.requisicao().body(corpo).post("/api/v1/tipos-usuario")
                .then().statusCode(413)
                .contentType(startsWith("application/problem+json"))
                .body("title", equalTo("Corpo grande demais"))
                .body("detail", equalTo("O corpo da requisição passa do limite de 1 MB."));
        assertThat(enviarEmPartes("/api/v1/tipos-usuario", corpo))
                .startsWith("HTTP/1.1 413")
                .contains("O corpo da requisição passa do limite de 1 MB.");
    }

    // Sem Content-Length, em partes (chunked): o tamanho só aparece durante a leitura
    private String enviarEmPartes(String caminho, String corpo) throws IOException {
        try (Socket socket = new Socket("localhost", porta)) {
            OutputStream saida = socket.getOutputStream();
            saida.write(("POST " + caminho + " HTTP/1.1\r\nHost: localhost\r\nContent-Type: application/json\r\n"
                    + "Transfer-Encoding: chunked\r\nConnection: close\r\n\r\n").getBytes(StandardCharsets.US_ASCII));
            byte[] bytes = corpo.getBytes(StandardCharsets.UTF_8);
            for (int inicio = 0; inicio < bytes.length; inicio += 64_000) {
                int tamanho = Math.min(64_000, bytes.length - inicio);
                saida.write((Integer.toHexString(tamanho) + "\r\n").getBytes(StandardCharsets.US_ASCII));
                saida.write(bytes, inicio, tamanho);
                saida.write("\r\n".getBytes(StandardCharsets.US_ASCII));
            }
            saida.write("0\r\n\r\n".getBytes(StandardCharsets.US_ASCII));
            saida.flush();
            return new String(socket.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
