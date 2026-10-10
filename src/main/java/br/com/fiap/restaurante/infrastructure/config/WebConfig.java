package br.com.fiap.restaurante.infrastructure.config;

import br.com.fiap.restaurante.infrastructure.api.comum.ParametrosDePaginacao;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Registra a conferência dos parâmetros de paginação em todas as listagens.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registro) {
        registro.addInterceptor(new ParametrosDePaginacao());
    }
}
