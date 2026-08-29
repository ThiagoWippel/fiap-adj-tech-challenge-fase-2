package br.com.fiap.restaurante.entities;

/**
 * Tipos de usuário previstos pelo sistema.
 *
 * O enunciado exige obrigatoriamente CLIENTE e DONO_RESTAURANTE, e admite a
 * inclusão de outros tipos. Cada valor corresponde a uma subclasse de Usuário
 * e ao valor gravado na coluna discriminadora da tabela.
 */
public enum TipoUsuario {

    CLIENTE,
    DONO_RESTAURANTE
}
