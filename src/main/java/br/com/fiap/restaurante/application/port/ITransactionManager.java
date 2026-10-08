package br.com.fiap.restaurante.application.port;

import java.util.function.Supplier;

/**
 * Porta de transação: executa uma operação como unidade atômica.
 *
 * <p>O caso de uso declara que precisa de atomicidade sem saber como ela é
 * obtida. A anotação {@code @Transactional} do Spring não pode ser usada nos
 * casos de uso, que são Java puro; a implementação desta porta, na
 * infraestrutura, é quem usa o gerenciador de transações do Spring.
 *
 * <p>Se a operação lançar uma exceção, tudo o que ela gravou é desfeito, e a
 * exceção segue para quem chamou.
 */
public interface ITransactionManager {

    /**
     * Executa a operação numa transação e devolve o resultado dela.
     */
    <T> T executar(Supplier<T> operacao);

    /**
     * Executa numa transação uma operação que não devolve nada.
     */
    default void executar(Runnable operacao) {
        executar(() -> {
            operacao.run();
            return null;
        });
    }
}
