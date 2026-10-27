package br.com.fiap.restaurante.infrastructure.persistence.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Linha da tabela de usuários")
class UsuarioEntityTest {

    @Test
    @DisplayName("EXC-04 · anonimizar apaga os dados pessoais, troca o nome e mantém o id e as datas")
    void deveAnonimizar() {
        /* arrange */
        LocalDateTime criacao = LocalDateTime.of(2026, 10, 1, 10, 0, 0);
        LocalDateTime alteracao = LocalDateTime.of(2026, 10, 5, 14, 30, 0);
        LocalDateTime remocao = LocalDateTime.of(2026, 10, 9, 8, 0, 0);
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setId(7L);
        usuario.setNome("Maria Silva");
        usuario.setEmail("maria@exemplo.com");
        usuario.setLogin("maria.silva");
        usuario.setSenha("hash-da-senha");
        usuario.setDocumento("12345678909");
        usuario.setTipo(new TipoUsuarioEntity());
        usuario.setEndereco(new EnderecoEmbeddable("Rua das Flores", "123", "Apto 45", "Centro", "Itajaí", "SC",
                "88301000"));
        usuario.setDataCriacao(criacao);
        usuario.setDataUltimaAlteracao(alteracao);

        /* act */
        usuario.anonimizar(remocao);

        /* assert */
        assertThat(usuario.getNome()).isEqualTo("Usuário removido");
        assertThat(usuario.getEmail()).isNull();
        assertThat(usuario.getLogin()).isNull();
        assertThat(usuario.getSenha()).isNull();
        assertThat(usuario.getDocumento()).isNull();
        assertThat(usuario.getTipo()).isNull();
        assertThat(usuario.getEndereco()).isNull();
        assertThat(usuario.getRemovidoEm()).isEqualTo(remocao);
        assertThat(usuario.getId()).isEqualTo(7L);
        assertThat(usuario.getDataCriacao()).isEqualTo(criacao);
        assertThat(usuario.getDataUltimaAlteracao()).isEqualTo(alteracao);
    }
}
