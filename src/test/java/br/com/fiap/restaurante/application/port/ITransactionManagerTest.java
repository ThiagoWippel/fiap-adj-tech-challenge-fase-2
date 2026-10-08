package br.com.fiap.restaurante.application.port;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Porta de transação")
class ITransactionManagerTest {

    @Test
    @DisplayName("INF-08 · a versão sem retorno executa a operação dentro da mesma transação")
    void deveExecutarOperacaoSemRetornoDentroDaTransacao() {
        /* arrange */
        List<String> passos = new ArrayList<>();
        ITransactionManager transacao = new ITransactionManager() {
            @Override
            public <T> T executar(Supplier<T> operacao) {
                passos.add("abre a transação");
                T resultado = operacao.get();
                passos.add("confirma a transação");
                return resultado;
            }
        };

        /* act */
        transacao.executar(() -> {
            passos.add("executa a operação");
        });

        /* assert */
        assertThat(passos).containsExactly("abre a transação", "executa a operação", "confirma a transação");
    }
}
