package br.com.fiap.restaurante.interfaceadapter.datasource;

import java.time.LocalDateTime;

/**
 * Usuário no formato trocado com a origem de dados. {@code documento} vai só com
 * dígitos; o tipo do documento sai do tamanho. As datas são preenchidas pela
 * origem de dados e ignoradas na gravação.
 */
public record DadosUsuario(Long id, String nome, String email, String login, String senha, String documento,
                           DadosTipoUsuario tipo, DadosEndereco endereco, LocalDateTime dataCriacao,
                           LocalDateTime dataUltimaAlteracao) {
}
