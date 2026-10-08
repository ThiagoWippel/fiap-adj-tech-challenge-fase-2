package br.com.fiap.restaurante.suporte;

import br.com.fiap.restaurante.interfaceadapter.datasource.DadosEndereco;
import br.com.fiap.restaurante.interfaceadapter.datasource.DadosTipoUsuario;
import br.com.fiap.restaurante.interfaceadapter.datasource.DadosUsuario;

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

    public static DadosUsuario maria() {
        return new DadosUsuario(7L, "Maria Silva", "maria@exemplo.com", "maria.silva", SENHA_CODIFICADA, CPF,
                CLIENTE, new DadosEndereco("Rua das Flores", "123", "Apto 45", "Centro", "Itajaí", "SC", "88301000"),
                CRIACAO, ALTERACAO);
    }
}
