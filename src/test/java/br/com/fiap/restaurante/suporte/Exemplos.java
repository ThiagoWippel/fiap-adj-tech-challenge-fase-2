package br.com.fiap.restaurante.suporte;

import br.com.fiap.restaurante.application.dto.EnderecoDTO;
import br.com.fiap.restaurante.application.dto.TurnoDTO;
import br.com.fiap.restaurante.domain.entity.ItemCardapio;
import br.com.fiap.restaurante.domain.entity.Restaurante;
import br.com.fiap.restaurante.domain.entity.TipoUsuario;
import br.com.fiap.restaurante.domain.entity.Usuario;
import br.com.fiap.restaurante.domain.enums.DiaSemana;
import br.com.fiap.restaurante.domain.enums.TipoCozinha;
import br.com.fiap.restaurante.domain.valueobject.Documento;
import br.com.fiap.restaurante.domain.valueobject.Endereco;
import br.com.fiap.restaurante.domain.valueobject.Preco;
import br.com.fiap.restaurante.domain.valueobject.QuadroDeHorarios;
import br.com.fiap.restaurante.domain.valueobject.Turno;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Dados de exemplo usados nos testes de caso de uso e de adaptadores.
 */
public final class Exemplos {

    public static final String SENHA = "SenhaSegura123";
    public static final String SENHA_CODIFICADA = "$2a$10$hashDaSenhaSegura123ParaTestesSemValorReal0000000000000";
    public static final String CPF = "12345678909";
    public static final String CNPJ = "11222333000181";
    public static final LocalDateTime CRIACAO = LocalDateTime.of(2026, 10, 1, 10, 0, 0);
    public static final LocalDateTime ALTERACAO = LocalDateTime.of(2026, 10, 5, 14, 30, 0);

    private Exemplos() {
    }

    public static TipoUsuario cliente() {
        return TipoUsuario.create(1L, "Cliente", TipoUsuario.CODIGO_CLIENTE);
    }

    public static TipoUsuario donoDeRestaurante() {
        return TipoUsuario.create(2L, "Dono de Restaurante", TipoUsuario.CODIGO_DONO_RESTAURANTE);
    }

    public static TipoUsuario entregador() {
        return TipoUsuario.create(3L, "Entregador", "ENTREGADOR");
    }

    public static Endereco endereco() {
        return new Endereco("Rua das Flores", "123", "Apto 45", "Centro", "Itajaí", "SC", "88301000");
    }

    public static EnderecoDTO enderecoDTO() {
        return new EnderecoDTO("Rua das Flores", "123", "Apto 45", "Centro", "Itajaí", "SC", "88301-000");
    }

    public static Usuario maria() {
        return Usuario.create(7L, "Maria Silva", "maria@exemplo.com", "maria.silva", SENHA_CODIFICADA,
                endereco(), cliente(), Documento.cpf(CPF), CRIACAO, ALTERACAO);
    }

    public static List<TurnoDTO> turnosDTO() {
        return List.of(new TurnoDTO("SEXTA", "18:00", "02:00"), new TurnoDTO("SEGUNDA", "11:00", "15:00"));
    }

    public static QuadroDeHorarios horarios() {
        return new QuadroDeHorarios(List.of(new Turno(DiaSemana.SEGUNDA, LocalTime.of(11, 0), LocalTime.of(15, 0)),
                new Turno(DiaSemana.SEXTA, LocalTime.of(18, 0), LocalTime.of(2, 0))));
    }

    /** Cantina da Nona, da Ana (Dono de Restaurante). */
    public static Restaurante cantina() {
        return Restaurante.create(9L, "Cantina da Nona", endereco(), TipoCozinha.ITALIANA, horarios(), ana(),
                CRIACAO, ALTERACAO);
    }

    /** Feijoada (item 5) da Cantina da Nona (restaurante 9). */
    public static ItemCardapio feijoada() {
        return ItemCardapio.create(5L, 9L, "Feijoada", "Feijoada completa com farofa e couve.",
                new Preco(new BigDecimal("39.90")), true, "fotos/feijoada.jpg", CRIACAO, ALTERACAO);
    }

    public static Usuario joao() {
        return Usuario.create(10L, "João Pereira", "joao@exemplo.com", "joao.pereira", SENHA_CODIFICADA,
                endereco(), donoDeRestaurante(), Documento.cnpj("11444777000161"), CRIACAO, ALTERACAO);
    }

    public static Usuario ana() {
        return Usuario.create(8L, "Ana Souza", "ana@exemplo.com", "ana.souza", SENHA_CODIFICADA,
                endereco(), donoDeRestaurante(), Documento.cnpj(CNPJ), CRIACAO, ALTERACAO);
    }
}
