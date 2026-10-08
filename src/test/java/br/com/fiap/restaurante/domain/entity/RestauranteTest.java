package br.com.fiap.restaurante.domain.entity;

import br.com.fiap.restaurante.domain.enums.TipoCozinha;
import br.com.fiap.restaurante.domain.exception.RegraDeNegocioException;
import br.com.fiap.restaurante.domain.exception.ValidacaoDeDominioException;
import br.com.fiap.restaurante.domain.valueobject.Documento;
import br.com.fiap.restaurante.domain.valueobject.Endereco;
import br.com.fiap.restaurante.domain.valueobject.QuadroDeHorarios;
import br.com.fiap.restaurante.domain.valueobject.Turno;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static br.com.fiap.restaurante.domain.enums.DiaSemana.SEGUNDA;
import static br.com.fiap.restaurante.domain.enums.DiaSemana.SEXTA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Restaurante")
class RestauranteTest {

    private static final String SENHA = "$2a$10$7EqJtq98hPqEX7fNZaFWoO5ZQ9QxM0rR5c7wEJxq1jC1r7Mq0x5mG";

    private final Endereco endereco = new Endereco("Rua Hercílio Luz", "120", null, "Centro", "Itajaí", "SC", "88301000");
    private final QuadroDeHorarios horarios = new QuadroDeHorarios(List.of(
            new Turno(SEGUNDA, LocalTime.of(11, 0), LocalTime.of(15, 0))));
    private final Usuario ana = Usuario.create(8L, "Ana Souza", "ana@exemplo.com", "ana.souza", SENHA, endereco,
            TipoUsuario.create(2L, "Dono de Restaurante", "DONO_RESTAURANTE"), Documento.cnpj("11222333000181"),
            null, null);
    private final Usuario maria = Usuario.create(7L, "Maria Silva", "maria@exemplo.com", "maria.silva", SENHA,
            endereco, TipoUsuario.create(1L, "Cliente", "CLIENTE"), Documento.cpf("12345678909"), null, null);

    @Test
    @DisplayName("RES-01 · restaurante válido é criado com nome, endereço, tipo de cozinha, turnos e dono")
    void deveCriarRestauranteValido() {
        /* act */
        Restaurante restaurante = Restaurante.create("  Cantina da Nona ", endereco, TipoCozinha.ITALIANA, horarios, ana);

        /* assert */
        assertThat(restaurante.getId()).isNull();
        assertThat(restaurante.getNome()).isEqualTo("Cantina da Nona");
        assertThat(restaurante.getEndereco()).isEqualTo(endereco);
        assertThat(restaurante.getTipoCozinha()).isEqualTo(TipoCozinha.ITALIANA);
        assertThat(restaurante.getHorarios()).isEqualTo(horarios);
        assertThat(restaurante.getDono()).isEqualTo(ana);
    }

    @Test
    @DisplayName("RES-01 · ao ler do banco, o restaurante vem com id e datas")
    void deveReconstituirComIdEDatas() {
        /* arrange */
        LocalDateTime criacao = LocalDateTime.of(2026, 10, 1, 10, 0);

        /* act */
        Restaurante restaurante = Restaurante.create(9L, "Cantina da Nona", endereco, TipoCozinha.ITALIANA, horarios,
                ana, criacao, criacao.plusDays(1));

        /* assert */
        assertThat(restaurante.getId()).isEqualTo(9L);
        assertThat(restaurante.getDataCriacao()).isEqualTo(criacao);
        assertThat(restaurante.getDataUltimaAlteracao()).isEqualTo(criacao.plusDays(1));
        assertThat(restaurante).isEqualTo(Restaurante.create(9L, "Outro Nome", endereco, TipoCozinha.PIZZARIA,
                horarios, ana, null, null));
    }

    @ParameterizedTest(name = "RES-02 · nome \"{0}\" é recusado")
    @NullAndEmptySource
    @ValueSource(strings = {"A", " B ", "   "})
    void deveRecusarNomeInvalido(String nome) {
        /* act + assert */
        assertThatThrownBy(() -> Restaurante.create(nome, endereco, TipoCozinha.ITALIANA, horarios, ana))
                .isInstanceOf(ValidacaoDeDominioException.class)
                .hasMessage("O nome do restaurante deve ter entre 2 e 120 caracteres.");
    }

    @Test
    @DisplayName("RES-02 · nome com mais de 120 caracteres é recusado; com 2, aceito")
    void deveRespeitarOsLimitesDoNome() {
        /* act + assert */
        assertThatThrownBy(() -> Restaurante.create("a".repeat(121), endereco, TipoCozinha.ITALIANA, horarios, ana))
                .isInstanceOf(ValidacaoDeDominioException.class);
        assertThat(Restaurante.create("Oi", endereco, TipoCozinha.ITALIANA, horarios, ana).getNome()).isEqualTo("Oi");
    }

    @Test
    @DisplayName("RES-03 · endereço, tipo de cozinha, horários ou dono ausente é recusado")
    void deveExigirOsCamposObrigatorios() {
        /* act + assert */
        assertThatThrownBy(() -> Restaurante.create("Cantina", null, TipoCozinha.ITALIANA, horarios, ana))
                .isInstanceOf(ValidacaoDeDominioException.class).hasMessage("O endereço é obrigatório.");
        assertThatThrownBy(() -> Restaurante.create("Cantina", endereco, null, horarios, ana))
                .isInstanceOf(ValidacaoDeDominioException.class).hasMessage("O tipo de cozinha é obrigatório.");
        assertThatThrownBy(() -> Restaurante.create("Cantina", endereco, TipoCozinha.ITALIANA, null, ana))
                .isInstanceOf(ValidacaoDeDominioException.class)
                .hasMessage("Informe ao menos um turno de funcionamento.");
        assertThatThrownBy(() -> Restaurante.create("Cantina", endereco, TipoCozinha.ITALIANA, horarios, null))
                .isInstanceOf(ValidacaoDeDominioException.class).hasMessage("O dono do restaurante é obrigatório.");
    }

    @Test
    @DisplayName("RES-07 · o dono precisa ser do tipo Dono de Restaurante")
    void deveExigirDonoDoTipoDonoDeRestaurante() {
        /* act + assert */
        assertThatThrownBy(() -> Restaurante.create("Cantina", endereco, TipoCozinha.ITALIANA, horarios, maria))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessage("O usuário 7 não é Dono de Restaurante.");
    }

    @Test
    @DisplayName("RES-04 · alterar um campo para um valor inválido falha e o restaurante mantém o valor anterior")
    void deveManterOValorAnterior_QuandoAAlteracaoFalhar() {
        /* arrange */
        Restaurante restaurante = Restaurante.create("Cantina da Nona", endereco, TipoCozinha.ITALIANA, horarios, ana);

        /* act + assert */
        assertThatThrownBy(() -> restaurante.setNome("X")).isInstanceOf(ValidacaoDeDominioException.class);
        assertThatThrownBy(() -> restaurante.setDono(maria)).isInstanceOf(RegraDeNegocioException.class);
        assertThat(restaurante.getNome()).isEqualTo("Cantina da Nona");
        assertThat(restaurante.getDono()).isEqualTo(ana);
    }

    @Test
    @DisplayName("RES-08 · os setters trocam os dados e os horários inteiros")
    void deveAtualizarPelosSetters() {
        /* arrange */
        Restaurante restaurante = Restaurante.create("Cantina da Nona", endereco, TipoCozinha.ITALIANA, horarios, ana);
        QuadroDeHorarios novos = new QuadroDeHorarios(List.of(new Turno(SEXTA, LocalTime.of(18, 0), LocalTime.of(2, 0))));
        Endereco outro = new Endereco("Avenida Brasil", "S/N", null, "Centro", "Balneário Camboriú", "SC", "88330000");

        /* act */
        restaurante.setNome("Pizzaria da Nona");
        restaurante.setEndereco(outro);
        restaurante.setTipoCozinha(TipoCozinha.PIZZARIA);
        restaurante.setHorarios(novos);

        /* assert */
        assertThat(restaurante.getNome()).isEqualTo("Pizzaria da Nona");
        assertThat(restaurante.getEndereco()).isEqualTo(outro);
        assertThat(restaurante.getTipoCozinha()).isEqualTo(TipoCozinha.PIZZARIA);
        assertThat(restaurante.getHorarios().turnos()).extracting(Turno::toString).containsExactly("SEXTA 18:00–02:00");
    }
}
