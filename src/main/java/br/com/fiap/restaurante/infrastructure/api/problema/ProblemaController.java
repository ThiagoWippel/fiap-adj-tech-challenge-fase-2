package br.com.fiap.restaurante.infrastructure.api.problema;

import br.com.fiap.restaurante.application.exception.RecursoNaoEncontradoException;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * Descreve os tipos de problema das respostas de erro.
 *
 * <p>Faz do campo {@code type} um endereço que responde: quem recebe um erro pode
 * consultar o que aquele tipo significa. Fica fora da versão da API, porque os
 * identificadores de problema valem para todas as versões, e fora do Swagger,
 * porque não é um recurso de negócio.
 */
@Hidden
@RestController
@RequestMapping("/problemas")
public class ProblemaController {

    @GetMapping
    public List<TipoDeProblemaResponse> listar() {
        return Arrays.stream(TipoDeProblema.values()).map(TipoDeProblemaResponse::de).toList();
    }

    @GetMapping("/{identificador}")
    public TipoDeProblemaResponse descrever(@PathVariable String identificador) {
        return TipoDeProblema.porIdentificador(identificador)
                .map(TipoDeProblemaResponse::de)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Não existe tipo de problema com o identificador '" + identificador + "'."));
    }
}
