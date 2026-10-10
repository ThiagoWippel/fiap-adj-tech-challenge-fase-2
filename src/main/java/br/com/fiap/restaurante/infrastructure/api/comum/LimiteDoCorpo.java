package br.com.fiap.restaurante.infrastructure.api.comum;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Recusa com 413 o corpo acima de 1 MB. O maior corpo legítimo, um restaurante com
 * 50 turnos, tem poucos KB. Com Content-Length, a recusa vem antes da leitura; sem
 * ele (corpo em partes), o limite vale durante a leitura.
 */
@Component
public class LimiteDoCorpo extends OncePerRequestFilter {

    public static final long LIMITE_EM_BYTES = 1024 * 1024;

    @Override
    protected void doFilterInternal(HttpServletRequest requisicao, HttpServletResponse resposta, FilterChain cadeia)
            throws ServletException, IOException {
        long tamanho = requisicao.getContentLengthLong();
        if (tamanho > LIMITE_EM_BYTES) {
            resposta.sendError(HttpStatus.CONTENT_TOO_LARGE.value());
            return;
        }
        cadeia.doFilter(tamanho < 0 ? new CorpoContado(requisicao) : requisicao, resposta);
    }

    /** Lança {@link CorpoGrandeDemaisException} quando a leitura passa do limite. */
    private static final class CorpoContado extends HttpServletRequestWrapper {

        private ServletInputStream entrada;

        CorpoContado(HttpServletRequest requisicao) {
            super(requisicao);
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            if (entrada == null) {
                entrada = new EntradaContada(super.getInputStream());
            }
            return entrada;
        }
    }

    private static final class EntradaContada extends ServletInputStream {

        private final ServletInputStream original;
        private long lidos;

        EntradaContada(ServletInputStream original) {
            this.original = original;
        }

        @Override
        public int read() throws IOException {
            int lido = original.read();
            contar(lido < 0 ? 0 : 1);
            return lido;
        }

        @Override
        public int read(byte[] destino, int inicio, int tamanho) throws IOException {
            int lidosAgora = original.read(destino, inicio, tamanho);
            contar(Math.max(lidosAgora, 0));
            return lidosAgora;
        }

        private void contar(int quantidade) throws CorpoGrandeDemaisException {
            lidos += quantidade;
            if (lidos > LIMITE_EM_BYTES) {
                throw new CorpoGrandeDemaisException();
            }
        }

        @Override
        public boolean isFinished() {
            return original.isFinished();
        }

        @Override
        public boolean isReady() {
            return original.isReady();
        }

        @Override
        public void setReadListener(ReadListener ouvinte) {
            original.setReadListener(ouvinte);
        }
    }
}
