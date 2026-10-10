package br.com.fiap.restaurante.domain.entity;

import br.com.fiap.restaurante.domain.exception.ValidacaoDeDominioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Tipo de usuário")
class TipoUsuarioTest {

    @Test
    @DisplayName("TIP-01 · tipo criado com o nome \"Entregador\" recebe o código ENTREGADOR")
    void deveGerarOCodigoAPartirDoNome() {
        /* act */
        TipoUsuario tipo = TipoUsuario.create("Entregador");

        /* assert */
        assertThat(tipo.getId()).isNull();
        assertThat(tipo.getNome()).isEqualTo("Entregador");
        assertThat(tipo.getCodigo()).isEqualTo("ENTREGADOR");
    }

    @ParameterizedTest(name = "TIP-02 · \"{0}\" gera o código {1}")
    @CsvSource({
            "Ajudante de Cozinha,      AJUDANTE_DE_COZINHA",
            "Gerência,                 GERENCIA",
            "'  Sócio   - Investidor ', SOCIO_INVESTIDOR",
            "Caixa 2,                  CAIXA_2"
    })
    void deveGerarCodigoSemAcentosEmMaiusculas(String nome, String codigo) {
        /* act */
        TipoUsuario tipo = TipoUsuario.create(nome);

        /* assert */
        assertThat(tipo.getCodigo()).isEqualTo(codigo);
    }

    @ParameterizedTest(name = "TIP-03 · nome \"{0}\" é recusado")
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "AB"})
    void deveRecusarNomeCurtoOuAusente(String nome) {
        /* act + assert */
        assertThatThrownBy(() -> TipoUsuario.create(nome))
                .isInstanceOf(ValidacaoDeDominioException.class)
                .hasMessage("O nome do tipo deve ter entre 3 e 50 caracteres.");
    }

    @Test
    @DisplayName("TIP-03 · nome com mais de 50 caracteres, ou sem letras nem números, é recusado")
    void deveRecusarNomeLongoOuSemLetras() {
        /* act + assert */
        assertThatThrownBy(() -> TipoUsuario.create("A".repeat(51)))
                .hasMessage("O nome do tipo deve ter entre 3 e 50 caracteres.");
        assertThatThrownBy(() -> TipoUsuario.create("---"))
                .hasMessage("O nome do tipo deve ter ao menos uma letra ou um número.");
    }

    @Test
    @DisplayName("TIP-19 · nome que gera código com mais de 50 caracteres é recusado")
    void deveRecusarNomeQueGeraCodigoLongoDemais() {
        /* arrange */
        String nome = "ß".repeat(30); // em maiúsculas, cada ß vira SS

        /* act + assert */
        assertThatThrownBy(() -> TipoUsuario.create(nome))
                .isInstanceOf(ValidacaoDeDominioException.class)
                .hasMessage("O nome do tipo gera um código com mais de 50 caracteres. Use um nome mais curto.");
    }

    @Test
    @DisplayName("TIP-04 · renomear um tipo mantém o código")
    void deveManterOCodigoAoRenomear() {
        /* arrange */
        TipoUsuario tipo = TipoUsuario.create(3L, "Entregador", "ENTREGADOR");

        /* act */
        tipo.setNome("Motoboy");

        /* assert */
        assertThat(tipo.getNome()).isEqualTo("Motoboy");
        assertThat(tipo.getCodigo()).isEqualTo("ENTREGADOR");
    }

    @Test
    @DisplayName("TIP-04 · renomear para um nome inválido falha e o tipo mantém o nome anterior")
    void deveManterONomeAnterior_QuandoORenomeFalhar() {
        /* arrange */
        TipoUsuario tipo = TipoUsuario.create(3L, "Entregador", "ENTREGADOR");

        /* act */
        assertThatThrownBy(() -> tipo.setNome("X")).isInstanceOf(ValidacaoDeDominioException.class);

        /* assert */
        assertThat(tipo.getNome()).isEqualTo("Entregador");
    }

    @Test
    @DisplayName("TIP-05 · CLIENTE e DONO_RESTAURANTE são tipos de sistema, reconhecidos pelo código")
    void deveReconhecerOsTiposDeSistemaPeloCodigo() {
        /* arrange */
        TipoUsuario cliente = TipoUsuario.create(1L, "Cliente", "CLIENTE");
        TipoUsuario dono = TipoUsuario.create(2L, "Proprietário", "DONO_RESTAURANTE");
        TipoUsuario entregador = TipoUsuario.create(3L, "Entregador", "ENTREGADOR");

        /* act + assert */
        assertThat(cliente.ehDeSistema()).isTrue();
        assertThat(cliente.ehDonoDeRestaurante()).isFalse();
        assertThat(dono.ehDeSistema()).isTrue();
        assertThat(dono.ehDonoDeRestaurante()).isTrue();
        assertThat(entregador.ehDeSistema()).isFalse();
        assertThat(entregador.ehDonoDeRestaurante()).isFalse();
    }

    @Test
    @DisplayName("TIP-05 · ao ler do banco, código ausente é recusado")
    void deveRecusarCodigoAusenteNaReconstituicao() {
        /* act + assert */
        assertThatThrownBy(() -> TipoUsuario.create(3L, "Entregador", " "))
                .isInstanceOf(ValidacaoDeDominioException.class)
                .hasMessage("O código do tipo é obrigatório.");
        assertThatThrownBy(() -> TipoUsuario.create(3L, "Entregador", null))
                .hasMessage("O código do tipo é obrigatório.");
    }

    @Test
    @DisplayName("TIP-05 · dois tipos são o mesmo quando têm o mesmo id")
    void deveCompararTiposPeloId() {
        /* act + assert */
        assertThat(TipoUsuario.create(1L, "Cliente", "CLIENTE"))
                .isEqualTo(TipoUsuario.create(1L, "Consumidor", "CLIENTE"))
                .isNotEqualTo(TipoUsuario.create(2L, "Cliente", "CLIENTE"));
    }

    @Test
    @DisplayName("ENT-05 · nome do tipo com caractere de controle é recusado")
    void deveRecusarControleNoNome() {
        /* act + assert */
        assertThatThrownBy(() -> TipoUsuario.create("Entregador\u0007"))
                .isInstanceOf(ValidacaoDeDominioException.class)
                .hasMessage("O campo nome do tipo não aceita quebra de linha nem caracteres de controle.");
    }
}
