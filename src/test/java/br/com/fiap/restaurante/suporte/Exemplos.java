package br.com.fiap.restaurante.suporte;

import br.com.fiap.restaurante.application.dto.EnderecoDTO;
import br.com.fiap.restaurante.domain.entity.TipoUsuario;
import br.com.fiap.restaurante.domain.entity.Usuario;
import br.com.fiap.restaurante.domain.valueobject.Documento;
import br.com.fiap.restaurante.domain.valueobject.Endereco;

import java.time.LocalDateTime;

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

    public static Usuario ana() {
        return Usuario.create(8L, "Ana Souza", "ana@exemplo.com", "ana.souza", SENHA_CODIFICADA,
                endereco(), donoDeRestaurante(), Documento.cnpj(CNPJ), CRIACAO, ALTERACAO);
    }
}
