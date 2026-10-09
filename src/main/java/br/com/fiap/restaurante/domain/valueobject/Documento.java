package br.com.fiap.restaurante.domain.valueobject;

import br.com.fiap.restaurante.domain.exception.ValidacaoDeDominioException;

/**
 * CPF ou CNPJ, guardado sem a máscara e com os dígitos verificadores conferidos.
 * O CNPJ aceita o formato alfanumérico, emitido pela Receita desde julho de 2026:
 * 12 caracteres entre dígitos e letras maiúsculas, seguidos de 2 dígitos
 * verificadores. O CNPJ só com números continua válido.
 */
public record Documento(Tipo tipo, String numero) {

    public enum Tipo { CPF, CNPJ }

    private static final int[] PESOS_CNPJ = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

    public Documento {
        if (tipo == null) {
            throw new ValidacaoDeDominioException("O documento deve ser um CPF ou um CNPJ.");
        }
        String semMascara = semMascara(numero);
        boolean valido = tipo == Tipo.CPF ? cpfValido(semMascara) : cnpjValido(semMascara);
        if (!valido) {
            throw new ValidacaoDeDominioException("O " + tipo + " informado não é válido.");
        }
        numero = semMascara;
    }

    public static Documento cpf(String numero) {
        return new Documento(Tipo.CPF, numero);
    }

    public static Documento cnpj(String numero) {
        return new Documento(Tipo.CNPJ, numero);
    }

    /** O tipo sai do tamanho sem a máscara: 11 para CPF, 14 para CNPJ. */
    public static Documento de(String numero) {
        String semMascara = semMascara(numero);
        Tipo tipo = switch (semMascara == null ? 0 : semMascara.length()) {
            case 11 -> Tipo.CPF;
            case 14 -> Tipo.CNPJ;
            default -> null;
        };
        return new Documento(tipo, semMascara);
    }

    public boolean ehCpf() {
        return tipo == Tipo.CPF;
    }

    public boolean ehCnpj() {
        return tipo == Tipo.CNPJ;
    }

    // Aceita a máscara (pontos, hífen e barra); qualquer outro caractere invalida.
    private static String semMascara(String valor) {
        if (valor == null || !valor.matches("[\\dA-Z./-]+")) {
            return null;
        }
        return valor.replaceAll("[./-]", "");
    }

    private static boolean cpfValido(String cpf) {
        if (cpf == null || !cpf.matches("\\d{11}") || todosIguais(cpf)) {
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
        if (cnpj == null || !cnpj.matches("[\\dA-Z]{12}\\d{2}") || todosIguais(cnpj)) {
            return false;
        }
        return digitoCnpj(cnpj, 12) == cnpj.charAt(12) - '0' && digitoCnpj(cnpj, 13) == cnpj.charAt(13) - '0';
    }

    // Para o primeiro dígito os pesos começam em 5; para o segundo, em 6. Cada
    // caractere vale o código ASCII menos 48: os dígitos valem eles mesmos, e de
    // A a Z vale de 17 a 42, como define a Receita para o CNPJ alfanumérico.
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
