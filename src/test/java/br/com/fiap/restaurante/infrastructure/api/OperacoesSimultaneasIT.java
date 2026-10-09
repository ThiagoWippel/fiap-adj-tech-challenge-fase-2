package br.com.fiap.restaurante.infrastructure.api;

import br.com.fiap.restaurante.suporte.ApiDeTeste;
import br.com.fiap.restaurante.suporte.LimpezaDoBanco;
import br.com.fiap.restaurante.suporte.TesteDeIntegracao;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static br.com.fiap.restaurante.suporte.ApiDeTeste.atualizacao;
import static br.com.fiap.restaurante.suporte.ApiDeTeste.cliente;
import static br.com.fiap.restaurante.suporte.ApiDeTeste.dono;
import static br.com.fiap.restaurante.suporte.ApiDeTeste.item;
import static br.com.fiap.restaurante.suporte.ApiDeTeste.restaurante;
import static br.com.fiap.restaurante.suporte.ApiDeTeste.tipo;
import static br.com.fiap.restaurante.suporte.ApiDeTeste.trocaDeTipo;
import static br.com.fiap.restaurante.suporte.GeradorDeDocumentos.cnpj;
import static br.com.fiap.restaurante.suporte.GeradorDeDocumentos.cpf;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Duas requisições ao mesmo tempo sobre o mesmo registro. Cada cenário dispara o
 * par várias vezes, porque a ordem em que as duas chegam ao banco muda de uma vez
 * para outra. Qualquer ordem é aceita, desde que o resultado faça sentido.
 */
@TesteDeIntegracao
@DisplayName("Operações simultâneas")
class OperacoesSimultaneasIT {

    private static final int TENTATIVAS = 10;
    private static final String USUARIOS = "/api/v1/usuarios";
    private static final String RESTAURANTES = "/api/v1/restaurantes";

    @LocalServerPort
    private int porta;

    @Autowired
    private JdbcTemplate jdbc;

    private ApiDeTeste api;
    private int sequencia;

    @BeforeEach
    void preparar() {
        LimpezaDoBanco.limpar(jdbc);
        api = new ApiDeTeste(porta);
    }

    @AfterEach
    void restaurar() {
        LimpezaDoBanco.limpar(jdbc);
    }

    @Test
    @DisplayName("CON-01 · excluir o dono e cadastrar restaurante para ele, juntos: só uma das duas dá certo")
    void deveImpedirRestauranteDeDonoExcluido() throws Exception {
        for (int i = 0; i < TENTATIVAS; i++) {
            /* arrange */
            long dono = novoDono();

            /* act */
            List<Integer> status = aoMesmoTempo(
                    () -> api.requisicao().delete(USUARIOS + "/{id}", dono).statusCode(),
                    () -> api.requisicao().body(restaurante("Cantina", "ITALIANA", dono, "SEGUNDA 11:00 15:00"))
                            .post(RESTAURANTES).statusCode());

            /* assert */
            assertThat(status).as("exclusão e cadastro").isIn(List.of(204, 404), List.of(409, 201));
        }
        api.requisicao().get(RESTAURANTES).then().statusCode(200);
    }

    @Test
    @DisplayName("CON-02 · trocar o dono para Cliente e cadastrar restaurante para ele, juntos: só uma dá certo")
    void deveImpedirRestauranteDeDonoQueVirouCliente() throws Exception {
        for (int i = 0; i < TENTATIVAS; i++) {
            /* arrange */
            long dono = novoDono();
            String troca = trocaDeTipo("CLIENTE", cpf(++sequencia));

            /* act */
            List<Integer> status = aoMesmoTempo(
                    () -> api.requisicao().body(troca).patch(USUARIOS + "/{id}/tipo", dono).statusCode(),
                    () -> api.requisicao().body(restaurante("Cantina", "ITALIANA", dono, "SEGUNDA 11:00 15:00"))
                            .post(RESTAURANTES).statusCode());

            /* assert */
            assertThat(status).as("troca de tipo e cadastro").isIn(List.of(200, 409), List.of(409, 201));
        }
        api.requisicao().get(RESTAURANTES).then().statusCode(200);
    }

    @ParameterizedTest(name = "CON-03 · excluir e atualizar {0} ao mesmo tempo: o excluído não volta e nada dá 500")
    @ValueSource(strings = {"usuário", "restaurante", "item", "tipo"})
    void deveManterAExclusao(String registro) throws Exception {
        for (int i = 0; i < TENTATIVAS; i++) {
            /* arrange */
            Alvo alvo = novo(registro);

            /* act */
            List<Integer> status = aoMesmoTempo(
                    () -> api.requisicao().delete(alvo.caminho()).statusCode(),
                    () -> api.requisicao().body(alvo.atualizacao()).put(alvo.caminho()).statusCode());

            /* assert */
            assertThat(status).as("exclusão e atualização").isIn(List.of(204, 404), List.of(204, 200));
            api.requisicao().get(alvo.caminho()).then().statusCode(404);
        }
    }

    // Caminho do registro e corpo do PUT que o altera
    private record Alvo(String caminho, String atualizacao) {
    }

    private Alvo novo(String registro) {
        int n = ++sequencia;
        return switch (registro) {
            case "usuário" -> new Alvo(USUARIOS + "/" + api.cadastrar(
                    cliente("Maria Silva", "maria" + n + "@exemplo.com", "maria" + n, cpf(n))),
                    atualizacao("Maria Souza", "souza" + n + "@exemplo.com", "souza" + n));
            case "restaurante" -> {
                long dono = novoDono();
                long id = api.cadastrarRestaurante(restaurante("Cantina", "ITALIANA", dono, "SEGUNDA 11:00 15:00"));
                yield new Alvo(RESTAURANTES + "/" + id,
                        restaurante("Cantina Nova", "PIZZARIA", dono, "TERCA 18:00 23:00"));
            }
            case "item" -> {
                long id = api.cadastrarRestaurante(restaurante("Cantina", "ITALIANA", novoDono(), "SEGUNDA 11:00 15:00"));
                long itemId = api.cadastrarItem(id, item("Feijoada", "39.90", false, "fotos/feijoada.jpg"));
                yield new Alvo(RESTAURANTES + "/" + id + "/itens-cardapio/" + itemId,
                        item("Feijoada completa", "42.00", true, "fotos/feijoada.jpg"));
            }
            default -> new Alvo("/api/v1/tipos-usuario/" + api.cadastrarTipo("Tipo " + n), tipo("Renomeado " + n));
        };
    }

    private long novoDono() {
        int n = ++sequencia;
        return api.cadastrar(dono("Dono " + n, "dono" + n + "@exemplo.com", "dono" + n, cnpj(n)));
    }

    // Solta as duas requisições no mesmo instante e devolve os status na ordem dos argumentos
    private static List<Integer> aoMesmoTempo(Callable<Integer> primeira, Callable<Integer> segunda)
            throws Exception {
        CountDownLatch largada = new CountDownLatch(1);
        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Future<Integer> a = executor.submit(() -> {
                largada.await();
                return primeira.call();
            });
            Future<Integer> b = executor.submit(() -> {
                largada.await();
                return segunda.call();
            });
            largada.countDown();
            return List.of(a.get(30, SECONDS), b.get(30, SECONDS));
        }
    }
}
