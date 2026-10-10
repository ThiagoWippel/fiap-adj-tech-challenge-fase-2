package br.com.fiap.restaurante.infrastructure.api.comum;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Confere page, size e sort como chegaram na URL, antes de o Spring Data os
 * interpretar. Sem isso, um page inválido virava a primeira página em silêncio, e
 * um % solto no sort derrubava a listagem com erro 500. Parâmetro vazio vale como
 * ausente.
 */
public class ParametrosDePaginacao implements HandlerInterceptor {

    private static final Pattern INTEIRO = Pattern.compile("\\d{1,10}");
    private static final Pattern ORDENACAO = Pattern.compile("[A-Za-z0-9_.,]*");

    @Override
    public boolean preHandle(HttpServletRequest requisicao, HttpServletResponse resposta, Object handler) {
        if (handler instanceof HandlerMethod metodo && recebePaginacao(metodo)) {
            exigirInteiro(requisicao.getParameterValues("page"), 0,
                    "O parâmetro page deve ser um número inteiro a partir de 0.");
            exigirInteiro(requisicao.getParameterValues("size"), 1,
                    "O parâmetro size deve ser um número inteiro a partir de 1.");
            String[] ordenacoes = requisicao.getParameterValues("sort");
            if (ordenacoes != null && !Arrays.stream(ordenacoes).allMatch(o -> ORDENACAO.matcher(o).matches())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "O parâmetro sort aceita nomes de campo e a direção, como nome,desc.");
            }
        }
        return true;
    }

    private static boolean recebePaginacao(HandlerMethod metodo) {
        return Arrays.stream(metodo.getMethodParameters())
                .anyMatch(parametro -> Pageable.class.isAssignableFrom(parametro.getParameterType()));
    }

    private static void exigirInteiro(String[] valores, long minimo, String mensagem) {
        if (valores == null) {
            return;
        }
        for (String valor : valores) {
            boolean valido = valor.isEmpty() || INTEIRO.matcher(valor).matches()
                    && Long.parseLong(valor) >= minimo && Long.parseLong(valor) <= Integer.MAX_VALUE;
            if (!valido) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
            }
        }
    }
}
