package br.com.fiap.restaurante.application.port;

import java.util.function.Supplier;

/**
 * Executa uma operação dentro de uma transação. Se a operação lançar exceção,
 * tudo o que ela gravou é desfeito.
 *
 * <p>Existe porque os casos de uso não podem usar o {@code @Transactional} do
 * Spring; a implementação fica na infraestrutura.
 */
public interface ITransactionManager {

    <T> T executar(Supplier<T> operacao);

    default void executar(Runnable operacao) {
        executar(() -> {
            operacao.run();
            return null;
        });
    }
}
