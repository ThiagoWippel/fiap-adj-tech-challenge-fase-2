package br.com.fiap.restaurante.domain.valueobject;

import br.com.fiap.restaurante.domain.exception.ValidacaoDeDominioException;

import java.util.Locale;
import java.util.Set;

/**
 * Endereço de usuário ou restaurante. O número é texto porque existem valores
 * como "S/N" e "123-A". A UF é guardada em maiúsculas e o CEP só com dígitos.
 */
public record Endereco(String rua, String numero, String complemento, String bairro, String cidade,
                       String estado, String cep) {

    private static final Set<String> UFS = Set.of(
            "AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS", "MG", "PA",
            "PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO");

    public Endereco {
        rua = obrigatorio(rua, "rua", 150);
        numero = obrigatorio(numero, "número", 10);
        complemento = opcional(complemento, "complemento", 60);
        bairro = obrigatorio(bairro, "bairro", 80);
        cidade = obrigatorio(cidade, "cidade", 80);
        estado = validarEstado(estado);
        cep = validarCep(cep);
    }

    private static String obrigatorio(String valor, String campo, int tamanhoMaximo) {
        TextoLivre.exigirUmaLinha(valor, campo);
        return limitar(obrigatorio(valor, campo), campo, tamanhoMaximo);
    }

    private static String obrigatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new ValidacaoDeDominioException("O campo " + campo + " é obrigatório.");
        }
        return valor.trim();
    }

    private static String opcional(String valor, String campo, int tamanhoMaximo) {
        TextoLivre.exigirUmaLinha(valor, campo);
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return limitar(valor.trim(), campo, tamanhoMaximo);
    }

    private static String limitar(String valor, String campo, int tamanhoMaximo) {
        if (valor.length() > tamanhoMaximo) {
            throw new ValidacaoDeDominioException(
                    "O campo " + campo + " deve ter no máximo " + tamanhoMaximo + " caracteres.");
        }
        return valor;
    }

    private static String validarEstado(String estado) {
        String sigla = obrigatorio(estado, "estado").toUpperCase(Locale.ROOT);
        if (!UFS.contains(sigla)) {
            throw new ValidacaoDeDominioException("O estado deve ser a sigla de uma UF brasileira, como SC.");
        }
        return sigla;
    }

    private static String validarCep(String cep) {
        String valor = obrigatorio(cep, "CEP");
        if (!valor.matches("\\d{5}-?\\d{3}")) {
            throw new ValidacaoDeDominioException("O CEP deve ter 8 dígitos.");
        }
        return valor.replace("-", "");
    }
}
