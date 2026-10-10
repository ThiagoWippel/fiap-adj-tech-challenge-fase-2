package br.com.fiap.restaurante.infrastructure.config;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.cfg.CoercionAction;
import tools.jackson.databind.cfg.CoercionInputShape;
import tools.jackson.databind.type.LogicalType;

/**
 * JSON sem conversões implícitas. Por padrão o Jackson transforma 1.5 no texto
 * "1.5", -1 em true, 1.5 no id 1 e "１２３" (dígitos de largura dupla) no id 123;
 * aqui esses valores devolvem 400 apontando o campo.
 */
@Configuration
public class JsonConfig {

    @Bean
    public JsonMapperBuilderCustomizer tiposEstritos() {
        return construtor -> construtor
                .disable(MapperFeature.ALLOW_COERCION_OF_SCALARS)
                .disable(DeserializationFeature.ACCEPT_FLOAT_AS_INT)
                .withCoercionConfig(LogicalType.Textual, texto -> texto
                        .setCoercion(CoercionInputShape.Integer, CoercionAction.Fail)
                        .setCoercion(CoercionInputShape.Float, CoercionAction.Fail)
                        .setCoercion(CoercionInputShape.Boolean, CoercionAction.Fail))
                .withCoercionConfig(LogicalType.Boolean, logico -> logico
                        .setCoercion(CoercionInputShape.Integer, CoercionAction.Fail));
    }
}
