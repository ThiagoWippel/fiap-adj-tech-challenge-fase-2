package br.com.fiap.restaurante.domain.entity;

import br.com.fiap.restaurante.domain.exception.ValidacaoDeDominioException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Tipo de usuário. O cadastro recebe só o nome; o código é gerado a partir dele
 * e não muda quando o tipo é renomeado. É pelo código que o sistema reconhece os
 * tipos Cliente e Dono de Restaurante.
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TipoUsuario {

    public static final String CODIGO_CLIENTE = "CLIENTE";
    public static final String CODIGO_DONO_RESTAURANTE = "DONO_RESTAURANTE";

    @EqualsAndHashCode.Include
    private Long id;
    private String nome;
    private String codigo;

    private TipoUsuario() {
    }

    public static TipoUsuario create(String nome) {
        TipoUsuario tipo = new TipoUsuario();
        tipo.setNome(nome);
        tipo.codigo = gerarCodigo(tipo.nome);
        return tipo;
    }

    public static TipoUsuario create(Long id, String nome, String codigo) {
        TipoUsuario tipo = new TipoUsuario();
        tipo.id = id;
        tipo.setNome(nome);
        if (codigo == null || codigo.isBlank()) {
            throw new ValidacaoDeDominioException("O código do tipo é obrigatório.");
        }
        tipo.codigo = codigo;
        return tipo;
    }

    public void setNome(String nome) {
        if (nome == null || nome.isBlank() || nome.trim().length() < 3 || nome.trim().length() > 50) {
            throw new ValidacaoDeDominioException("O nome do tipo deve ter entre 3 e 50 caracteres.");
        }
        this.nome = nome.trim();
    }

    public boolean ehDeSistema() {
        return CODIGO_CLIENTE.equals(codigo) || ehDonoDeRestaurante();
    }

    public boolean ehDonoDeRestaurante() {
        return CODIGO_DONO_RESTAURANTE.equals(codigo);
    }

    // "Ajudante de Cozinha" vira AJUDANTE_DE_COZINHA
    private static String gerarCodigo(String nome) {
        String semAcentos = Normalizer.normalize(nome, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        String codigo = semAcentos.toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]+", "_")
                .replaceAll("^_|_$", "");
        if (codigo.isEmpty()) {
            throw new ValidacaoDeDominioException("O nome do tipo deve ter ao menos uma letra ou um número.");
        }
        return codigo;
    }
}
