package br.com.fiap.restaurante.infrastructure.api.problema;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tipos de problema")
class TipoDeProblemaTest {

    @Test
    @DisplayName("ERR-02 · o type é o endereço absoluto do tipo de problema na própria aplicação")
    void deveMontarOEnderecoAbsolutoDoTipo() {
        /* arrange */
        URI base = URI.create("http://localhost:8080");

        /* act */
        URI endereco = TipoDeProblema.CONFLITO_DE_DADOS.uri(base);

        /* assert */
        assertThat(endereco).hasToString("http://localhost:8080/problemas/conflito-de-dados");
    }

    @Test
    @DisplayName("ERR-02 · cada tipo é encontrado pelo seu identificador, e um identificador desconhecido não encontra nada")
    void deveEncontrarOTipoPeloIdentificador() {
        /* act + assert */
        assertThat(TipoDeProblema.porIdentificador("dados-invalidos")).contains(TipoDeProblema.DADOS_INVALIDOS);
        assertThat(TipoDeProblema.porIdentificador("inexistente")).isEmpty();
    }

    @Test
    @DisplayName("ERR-12 · o 406 tem tipo próprio, e a descrição dele diz 406")
    void deveTerTipoProprioParaO406() {
        /* act + assert */
        assertThat(TipoDeProblema.paraErroDoFramework(406)).isEqualTo(TipoDeProblema.FORMATO_NAO_DISPONIVEL);
        assertThat(TipoDeProblema.FORMATO_NAO_DISPONIVEL.status()).isEqualTo(406);
    }

    @ParameterizedTest(name = "ERR-09 · erro do framework com status {0} vira o tipo {1}")
    @CsvSource({
            "400, REQUISICAO_INVALIDA",
            "404, RECURSO_NAO_ENCONTRADO",
            "405, METODO_NAO_PERMITIDO",
            "415, MIDIA_NAO_SUPORTADA",
            "422, REQUISICAO_INVALIDA",
            "503, ERRO_INTERNO"
    })
    void deveEscolherOTipoDosErrosDoFrameworkPeloStatus(int status, TipoDeProblema esperado) {
        /* act */
        TipoDeProblema tipo = TipoDeProblema.paraErroDoFramework(status);

        /* assert */
        assertThat(tipo).isEqualTo(esperado);
    }
}
