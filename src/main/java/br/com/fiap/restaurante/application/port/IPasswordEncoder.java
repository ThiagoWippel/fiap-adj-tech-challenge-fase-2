package br.com.fiap.restaurante.application.port;

/**
 * Porta de codificação de senha.
 *
 * <p>Os casos de uso nunca veem nem gravam a senha em texto: codificam antes de
 * gravar e conferem comparando com o valor codificado. O algoritmo fica na
 * infraestrutura, e nos testes de caso de uso esta porta é substituída por um
 * dublê.
 */
public interface IPasswordEncoder {

    /**
     * Codifica a senha em texto. Duas chamadas com a mesma senha podem gerar
     * valores diferentes.
     */
    String codificar(String senha);

    /**
     * Diz se a senha em texto corresponde ao valor codificado.
     */
    boolean confere(String senha, String senhaCodificada);
}
