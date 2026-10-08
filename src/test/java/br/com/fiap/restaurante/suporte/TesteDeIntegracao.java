package br.com.fiap.restaurante.suporte;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca um teste de integração: aplicação inteira de pé, numa porta aleatória,
 * contra o MySQL do Testcontainers, no perfil {@code test}.
 *
 * <p>Todas as classes anotadas compartilham a mesma configuração, e por isso o
 * mesmo contexto do Spring e o mesmo contêiner.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(ContainersDeTeste.class)
@ActiveProfiles("test")
public @interface TesteDeIntegracao {
}
