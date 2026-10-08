package br.com.fiap.restaurante;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Inicializa a aplicação. As classes com {@code @ConfigurationProperties} são
 * encontradas pelo {@code @ConfigurationPropertiesScan}.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class RestauranteApplication {

	public static void main(String[] args) {
		SpringApplication.run(RestauranteApplication.class, args);
	}

}
