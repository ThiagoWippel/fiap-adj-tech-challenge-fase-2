package br.com.fiap.restaurante.suporte;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Deixa o banco dos testes de integração como o script de schema criou: sem
 * usuários, só com os dois tipos de sistema e com os nomes originais.
 */
public final class LimpezaDoBanco {

    private LimpezaDoBanco() {
    }

    public static void limpar(JdbcTemplate jdbc) {
        jdbc.update("DELETE FROM usuario");
        jdbc.update("DELETE FROM tipo_usuario WHERE codigo NOT IN ('CLIENTE', 'DONO_RESTAURANTE')");
        jdbc.update("UPDATE tipo_usuario SET nome = 'Cliente' WHERE codigo = 'CLIENTE'");
        jdbc.update("UPDATE tipo_usuario SET nome = 'Dono de Restaurante' WHERE codigo = 'DONO_RESTAURANTE'");
    }
}
