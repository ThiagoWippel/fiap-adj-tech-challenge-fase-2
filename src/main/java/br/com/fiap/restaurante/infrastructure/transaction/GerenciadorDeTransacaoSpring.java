package br.com.fiap.restaurante.infrastructure.transaction;

import br.com.fiap.restaurante.application.port.ITransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Supplier;

/**
 * Implementação da porta de transação com o {@link TransactionTemplate}, a versão
 * programática do {@code @Transactional}.
 */
@Component
public class GerenciadorDeTransacaoSpring implements ITransactionManager {

    private final TransactionTemplate template;

    public GerenciadorDeTransacaoSpring(PlatformTransactionManager gerenciador) {
        this.template = new TransactionTemplate(gerenciador);
    }

    @Override
    public <T> T executar(Supplier<T> operacao) {
        return template.execute(status -> operacao.get());
    }
}
