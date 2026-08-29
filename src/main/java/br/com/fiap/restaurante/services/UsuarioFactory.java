package br.com.fiap.restaurante.services;

import br.com.fiap.restaurante.dtos.request.CriarUsuarioRequest;
import br.com.fiap.restaurante.entities.Cliente;
import br.com.fiap.restaurante.entities.DonoRestaurante;
import br.com.fiap.restaurante.entities.Endereco;
import br.com.fiap.restaurante.entities.Usuario;
import br.com.fiap.restaurante.services.exceptions.RegraDeNegocioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Fabrica de usuários (padrão Factory Method).
 *
 * O campo "tipo" da requisição determina qual subclasse concreta instanciar.
 * Essa é a definição do problema que o padrão resolve: decidir, em tempo de
 * execução, qual objeto de uma hierarquia criar.
 *
 * Concentrar a decisão aqui tem dois efeitos. O serviço deixa de conhecer as
 * subclasses e passa a lidar apenas com Usuário. E a inclusão de um terceiro
 * tipo altera unicamente esta classe - o restante do sistema segue intacto.
 */
@Component
public class UsuarioFactory {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioFactory.class);

    public Usuario criar(CriarUsuarioRequest requisicao, String senhaCodificada,
                         Endereco endereco, String documentoNormalizado) {

        return switch (requisicao.tipo()) {

            case CLIENTE -> {
                if (documentoNormalizado == null) {
                    logger.warn("Cadastro rejeitado: CPF ausente para usuario do tipo CLIENTE");
                    throw new RegraDeNegocioException("O CPF e obrigatorio para usuarios do tipo CLIENTE");
                }
                yield new Cliente(
                        requisicao.nome(),
                        requisicao.email(),
                        requisicao.login(),
                        senhaCodificada,
                        endereco,
                        documentoNormalizado
                );
            }

            case DONO_RESTAURANTE -> {
                if (documentoNormalizado == null) {
                    logger.warn("Cadastro rejeitado: CNPJ ausente para usuario do tipo DONO_RESTAURANTE");
                    throw new RegraDeNegocioException("O CNPJ e obrigatorio para usuarios do tipo DONO_RESTAURANTE");
                }
                yield new DonoRestaurante(
                        requisicao.nome(),
                        requisicao.email(),
                        requisicao.login(),
                        senhaCodificada,
                        endereco,
                        documentoNormalizado
                );
            }
        };
    }
}