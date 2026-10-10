package br.com.fiap.restaurante.infrastructure.api.comum;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Limite do corpo")
class LimiteDoCorpoTest {

    private final LimiteDoCorpo filtro = new LimiteDoCorpo();

    @Test
    @DisplayName("ENT-06 · com Content-Length acima de 1 MB, responde 413 sem chamar a aplicação")
    void deveRecusarPeloContentLength() throws Exception {
        /* arrange */
        MockHttpServletRequest requisicao = new MockHttpServletRequest("POST", "/api/v1/tipos-usuario");
        requisicao.setContent(new byte[(int) LimiteDoCorpo.LIMITE_EM_BYTES + 1]);
        MockHttpServletResponse resposta = new MockHttpServletResponse();
        MockFilterChain cadeia = new MockFilterChain();

        /* act */
        filtro.doFilter(requisicao, resposta, cadeia);

        /* assert */
        assertThat(resposta.getStatus()).isEqualTo(413);
        assertThat(cadeia.getRequest()).isNull();
    }

    @Test
    @DisplayName("ENT-06 · sem Content-Length, a leitura passa do limite e lança a exceção do corpo grande demais")
    void deveContarALeituraSemContentLength() throws Exception {
        /* arrange */
        HttpServletRequest semTamanho = semContentLength(new byte[(int) LimiteDoCorpo.LIMITE_EM_BYTES + 10]);
        AtomicReference<ServletRequest> recebida = new AtomicReference<>();

        /* act */
        filtro.doFilter(semTamanho, new MockHttpServletResponse(), (req, res) -> recebida.set(req));
        ServletInputStream entrada = recebida.get().getInputStream();

        /* assert */
        assertThat(recebida.get().getInputStream()).isSameAs(entrada);
        assertThat(entrada.read()).isZero();
        assertThat(entrada.isFinished()).isFalse();
        assertThat(entrada.isReady()).isTrue();
        assertThatThrownBy(() -> entrada.setReadListener(new OuvinteVazio()))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(entrada::readAllBytes).isInstanceOf(CorpoGrandeDemaisException.class);
    }

    @Test
    @DisplayName("ENT-06 · sem Content-Length, um corpo pequeno é lido inteiro, até o fim")
    void deveLerCorpoPequenoSemContentLength() throws Exception {
        /* arrange */
        HttpServletRequest semTamanho = semContentLength("{\"nome\":\"Entregador\"}".getBytes());
        AtomicReference<ServletRequest> recebida = new AtomicReference<>();

        /* act */
        filtro.doFilter(semTamanho, new MockHttpServletResponse(), (req, res) -> recebida.set(req));
        ServletInputStream entrada = recebida.get().getInputStream();

        /* assert */
        assertThat(entrada.readAllBytes()).hasSize(21);
        assertThat(entrada.read()).isEqualTo(-1);
    }

    private static HttpServletRequest semContentLength(byte[] corpo) {
        MockHttpServletRequest requisicao = new MockHttpServletRequest("POST", "/api/v1/tipos-usuario");
        requisicao.setContent(corpo);
        return new HttpServletRequestWrapper(requisicao) {
            @Override
            public long getContentLengthLong() {
                return -1;
            }
        };
    }

    private static final class OuvinteVazio implements ReadListener {

        @Override
        public void onDataAvailable() throws IOException {
            // sem uso
        }

        @Override
        public void onAllDataRead() throws IOException {
            // sem uso
        }

        @Override
        public void onError(Throwable erro) {
            // sem uso
        }
    }
}
