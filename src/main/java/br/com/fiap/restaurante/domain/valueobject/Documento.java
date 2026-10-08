package br.com.fiap.restaurante.domain.valueobject;

import br.com.fiap.restaurante.domain.exception.ValidacaoDeDominioException;

/**
 * CPF ou CNPJ, guardado só com dígitos e com os dígitos verificadores conferidos.
 */
public record Documento(Tipo tipo, String numero) {

    public enum Tipo { CPF, CNPJ }

    private static final int[] PESOS_CNPJ = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

    public Documento {
        if (tipo == null) {
            throw new ValidacaoDeDominioException("O documento deve ser um CPF ou um CNPJ.");
        }
        String digitos = apenasDigitos(numero);
        boolean valido = tipo == Tipo.CPF ? cpfValido(digitos) : cnpjValido(digitos);
        if (!valido) {
            throw new ValidacaoDeDominioException("O " + tipo + " informado não é válido.");
        }
        numero = digitos;
    }

    public static Documento cpf(String numero) {
        return new Documento(Tipo.CPF, numero);
    }

    public static Documento cnpj(String numero) {
        return new Documento(Tipo.CNPJ, numero);
    }

    /** Usado ao ler do banco, onde só os dígitos são guardados. */
    public static Documento de(String numero) {
        String digitos = apenasDigitos(numero);
        Tipo tipo = switch (digitos == null ? 0 : digitos.length()) {
            case 11 -> Tipo.CPF;
            case 14 -> Tipo.CNPJ;
            default -> null;
        };
        return new Documento(tipo, digitos);
    }

    public boolean ehCpf() {
        return tipo == Tipo.CPF;
    }

    public boolean ehCnpj() {
        return tipo == Tipo.CNPJ;
    }

    // Aceita a máscara (pontos, hífen e barra); qualquer outro caractere invalida.
    private static String apenasDigitos(String valor) {
        if (valor == null || !valor.matches("[\\d./-]+")) {
            return null;
        }
        return valor.replaceAll("\\D", "");
    }

    private static boolean cpfValido(String cpf) {
        if (cpf == null || cpf.length() != 11 || todosIguais(cpf)) {
            return false;
        }
        return digitoCpf(cpf, 9) == cpf.charAt(9) - '0' && digitoCpf(cpf, 10) == cpf.charAt(10) - '0';
    }

    private static int digitoCpf(String cpf, int quantidade) {
        int soma = 0;
        for (int i = 0; i < quantidade; i++) {
            soma += (cpf.charAt(i) - '0') * (quantidade + 1 - i);
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    private static boolean cnpjValido(String cnpj) {
        if (cnpj == null || cnpj.length() != 14 || todosIguais(cnpj)) {
            return false;
        }
        return digitoCnpj(cnpj, 12) == cnpj.charAt(12) - '0' && digitoCnpj(cnpj, 13) == cnpj.charAt(13) - '0';
    }

    // Para o primeiro dígito os pesos começam em 5; para o segundo, em 6.
    private static int digitoCnpj(String cnpj, int quantidade) {
        int deslocamento = PESOS_CNPJ.length - quantidade;
        int soma = 0;
        for (int i = 0; i < quantidade; i++) {
            soma += (cnpj.charAt(i) - '0') * PESOS_CNPJ[i + deslocamento];
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    private static boolean todosIguais(String digitos) {
        return digitos.chars().distinct().count() == 1;
    }
}
