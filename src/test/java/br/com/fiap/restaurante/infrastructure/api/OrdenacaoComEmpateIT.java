package br.com.fiap.restaurante.infrastructure.api;

import br.com.fiap.restaurante.suporte.ApiDeTeste;
import br.com.fiap.restaurante.suporte.LimpezaDoBanco;
import br.com.fiap.restaurante.suporte.TesteDeIntegracao;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static br.com.fiap.restaurante.suporte.ApiDeTeste.cliente;
import static br.com.fiap.restaurante.suporte.ApiDeTeste.dono;
import static br.com.fiap.restaurante.suporte.ApiDeTeste.item;
import static br.com.fiap.restaurante.suporte.ApiDeTeste.restaurante;
import static br.com.fiap.restaurante.suporte.GeradorDeDocumentos.cnpj;
import static br.com.fiap.restaurante.suporte.GeradorDeDocumentos.cpf;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * O MySQL não garante a ordem entre registros com o mesmo valor no campo de
 * ordenação. Sem desempate, um registro pode aparecer em duas páginas e sumir de
 * outra. Cada listagem aqui tem 30 registros empatados, percorridos de 7 em 7.
 */
@TesteDeIntegracao
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Ordenação com valores repetidos")
class OrdenacaoComEmpateIT {

    private static final int QUANTIDADE = 30;

    @LocalServerPort
    private int porta;

    @Autowired
    private JdbcTemplate jdbc;

    private ApiDeTeste api;
    private Map<String, String> rotas;

    @BeforeAll
    void cadastrarDados() {
        LimpezaDoBanco.limpar(jdbc);
        api = new ApiDeTeste(porta);
        long ana = api.cadastrar(dono("Ana Souza", "ana@exemplo.com", "ana.souza", cnpj(0)));
        long cantina = 0;
        for (int i = 1; i <= QUANTIDADE; i++) {
            api.cadastrar(cliente("Maria Silva", "maria" + i + "@exemplo.com", "maria.silva" + i, cpf(i)));
            String cozinha = i % 2 == 0 ? "ITALIANA" : "PIZZARIA";
            cantina = api.cadastrarRestaurante(restaurante("Cantina", cozinha, ana, "SEGUNDA 11:00 15:00"));
        }
        for (int i = 1; i <= QUANTIDADE; i++) {
            api.cadastrarItem(cantina, item("Prato " + i, "10.00", false, "fotos/prato.jpg"));
        }
        long tipoCliente = jdbc.queryForObject("SELECT id FROM tipo_usuario WHERE codigo = 'CLIENTE'", Long.class);
        rotas = Map.of(
                "usuarios v2 por nome", "/api/v2/usuarios?nome=Maria",
                "usuários do tipo por nome", "/api/v1/tipos-usuario/" + tipoCliente + "/usuarios",
                "restaurantes por nome", "/api/v1/restaurantes",
                "restaurantes por tipo de cozinha", "/api/v1/restaurantes?sort=tipoCozinha",
                "restaurantes do usuário por nome", "/api/v1/usuarios/" + ana + "/restaurantes",
                "itens por preço", "/api/v1/restaurantes/" + cantina + "/itens-cardapio?sort=preco");
    }

    @AfterAll
    void limpar() {
        LimpezaDoBanco.limpar(jdbc);
    }

    @ParameterizedTest(name = "PAG-08 · {0}: percorrendo as páginas, cada registro aparece uma única vez")
    @ValueSource(strings = {"usuarios v2 por nome", "usuários do tipo por nome", "restaurantes por nome",
            "restaurantes por tipo de cozinha", "restaurantes do usuário por nome", "itens por preço"})
    void deveListarCadaRegistroUmaVez(String listagem) {
        /* act */
        List<Long> ids = new ArrayList<>();
        for (int pagina = 0; pagina * 7 < QUANTIDADE; pagina++) {
            ids.addAll(api.requisicao().queryParam("size", 7).queryParam("page", pagina)
                    .get(rotas.get(listagem))
                    .then().statusCode(200)
                    .extract().jsonPath().getList("conteudo.id", Long.class));
        }

        /* assert */
        assertThat(ids).hasSize(QUANTIDADE).doesNotHaveDuplicates();
    }
}
