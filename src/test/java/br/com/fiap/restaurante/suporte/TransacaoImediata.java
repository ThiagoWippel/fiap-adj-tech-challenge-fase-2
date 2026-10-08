package br.com.fiap.restaurante.suporte;

import br.com.fiap.restaurante.application.port.ITransactionManager;

import java.util.function.Supplier;

/**
 * Dublê da porta de transação para os testes de caso de uso: executa a operação
 * na hora e conta quantas vezes foi chamado.
 */
public class TransacaoImediata implements ITransactionManager {

    private int execucoes;

    @Override
    public <T> T executar(Supplier<T> operacao) {
        execucoes++;
        return operacao.get();
    }

    public int execucoes() {
        return execucoes;
    }
}
