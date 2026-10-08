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
 * Aplicação inteira numa porta aleatória, com o MySQL do Testcontainers e o
 * perfil {@code test}. As classes anotadas compartilham o mesmo contexto.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(ContainersDeTeste.class)
@ActiveProfiles("test")
public @interface TesteDeIntegracao {
}
