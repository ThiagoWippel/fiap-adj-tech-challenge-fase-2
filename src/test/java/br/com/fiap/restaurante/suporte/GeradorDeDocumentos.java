package br.com.fiap.restaurante.suporte;

/**
 * CPFs e CNPJs válidos para testes que cadastram muitos usuários. Números
 * diferentes geram documentos diferentes.
 */
public final class GeradorDeDocumentos {

    private static final int[] PESOS_CNPJ = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

    private GeradorDeDocumentos() {
    }

    public static String cpf(int numero) {
        String base = String.valueOf(100_000_000 + numero);
        String comPrimeiro = base + digitoCpf(base);
        return comPrimeiro + digitoCpf(comPrimeiro);
    }

    public static String cnpj(int numero) {
        String base = (10_000_000 + numero) + "0001";
        String comPrimeiro = base + digitoCnpj(base);
        return comPrimeiro + digitoCnpj(comPrimeiro);
    }

    private static int digitoCpf(String digitos) {
        int soma = 0;
        for (int i = 0; i < digitos.length(); i++) {
            soma += (digitos.charAt(i) - '0') * (digitos.length() + 1 - i);
        }
        return resto(soma);
    }

    private static int digitoCnpj(String digitos) {
        int deslocamento = PESOS_CNPJ.length - digitos.length();
        int soma = 0;
        for (int i = 0; i < digitos.length(); i++) {
            soma += (digitos.charAt(i) - '0') * PESOS_CNPJ[i + deslocamento];
        }
        return resto(soma);
    }

    private static int resto(int soma) {
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}
