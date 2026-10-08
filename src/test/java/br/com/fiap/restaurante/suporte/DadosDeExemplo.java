package br.com.fiap.restaurante.suporte;

import br.com.fiap.restaurante.interfaceadapter.datasource.DadosEndereco;
import br.com.fiap.restaurante.interfaceadapter.datasource.DadosItemCardapio;
import br.com.fiap.restaurante.interfaceadapter.datasource.DadosRestaurante;
import br.com.fiap.restaurante.interfaceadapter.datasource.DadosTipoUsuario;
import br.com.fiap.restaurante.interfaceadapter.datasource.DadosTurno;
import br.com.fiap.restaurante.interfaceadapter.datasource.DadosUsuario;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

import static br.com.fiap.restaurante.suporte.Exemplos.ALTERACAO;
import static br.com.fiap.restaurante.suporte.Exemplos.CPF;
import static br.com.fiap.restaurante.suporte.Exemplos.CRIACAO;
import static br.com.fiap.restaurante.suporte.Exemplos.SENHA_CODIFICADA;

/**
 * Os mesmos exemplos de {@link Exemplos}, no formato das origens de dados.
 */
public final class DadosDeExemplo {

    public static final DadosTipoUsuario CLIENTE = new DadosTipoUsuario(1L, "Cliente", "CLIENTE");

    private DadosDeExemplo() {
    }

    public static final DadosTipoUsuario DONO = new DadosTipoUsuario(2L, "Dono de Restaurante", "DONO_RESTAURANTE");

    public static DadosUsuario ana() {
        return new DadosUsuario(8L, "Ana Souza", "ana@exemplo.com", "ana.souza", SENHA_CODIFICADA, "11222333000181",
                DONO, endereco(), CRIACAO, ALTERACAO);
    }

    /** Cantina da Nona, da Ana, com os turnos fora de ordem, como podem vir do banco. */
    public static DadosRestaurante cantina() {
        return new DadosRestaurante(9L, "Cantina da Nona", endereco(), "ITALIANA",
                List.of(new DadosTurno("SEXTA", LocalTime.of(18, 0), LocalTime.of(2, 0)),
                        new DadosTurno("SEGUNDA", LocalTime.of(11, 0), LocalTime.of(15, 0))),
                ana(), CRIACAO, ALTERACAO);
    }

    public static DadosItemCardapio feijoada() {
        return new DadosItemCardapio(5L, 9L, "Feijoada", "Feijoada completa com farofa e couve.",
                new BigDecimal("39.90"), true, "fotos/feijoada.jpg", CRIACAO, ALTERACAO);
    }

    private static DadosEndereco endereco() {
        return new DadosEndereco("Rua das Flores", "123", "Apto 45", "Centro", "Itajaí", "SC", "88301000");
    }

    public static DadosUsuario maria() {
        return new DadosUsuario(7L, "Maria Silva", "maria@exemplo.com", "maria.silva", SENHA_CODIFICADA, CPF,
                CLIENTE, new DadosEndereco("Rua das Flores", "123", "Apto 45", "Centro", "Itajaí", "SC", "88301000"),
                CRIACAO, ALTERACAO);
    }
}
