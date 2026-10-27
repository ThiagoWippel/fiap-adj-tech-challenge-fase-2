package br.com.fiap.restaurante.infrastructure.api.problema;

import br.com.fiap.restaurante.application.exception.RecursoNaoEncontradoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Descrição dos tipos de problema")
class ProblemaControllerTest {

    private final ProblemaController controller = new ProblemaController();

    @Test
    @DisplayName("ERR-02 · a lista traz todos os tipos, e cada um é descrito pelo seu identificador")
    void deveDescreverCadaTipo() {
        /* act */
        TipoDeProblemaResponse conflito = controller.descrever("conflito-de-dados");

        /* assert */
        assertThat(controller.listar()).extracting(TipoDeProblemaResponse::identificador)
                .hasSize(TipoDeProblema.values().length)
                .contains("dados-invalidos", "erro-interno");
        assertThat(conflito.titulo()).isEqualTo("Conflito de dados");
        assertThat(conflito.status()).isEqualTo(409);
        assertThat(conflito.descricao()).startsWith("A operação conflita com o estado atual dos dados");
    }

    @Test
    @DisplayName("ERR-02 · um identificador desconhecido é recurso não encontrado")
    void deveRecusarIdentificadorDesconhecido() {
        /* act + assert */
        assertThatThrownBy(() -> controller.descrever("nao-existe"))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Não existe tipo de problema com o identificador 'nao-existe'.");
    }
}
