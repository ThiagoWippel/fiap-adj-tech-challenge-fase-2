package br.com.fiap.restaurante.mappers;

import br.com.fiap.restaurante.dtos.request.EnderecoRequest;
import br.com.fiap.restaurante.dtos.response.EnderecoResponse;
import br.com.fiap.restaurante.dtos.response.LoginResponse;
import br.com.fiap.restaurante.dtos.response.UsuarioResponse;
import br.com.fiap.restaurante.entities.Endereco;
import br.com.fiap.restaurante.entities.Usuario;
import org.springframework.stereotype.Component;

/**
 * Conversão entre entidades e objetos de transferência.
 *
 * Escrito a mão, sem biblioteca de mapeamento automático. Bibliotecas do gênero
 * associam campos por reflexão em tempo de execução: um nome errado não impede
 * a compilação e só se manifesta quando a aplicação roda. Com poucas conversões,
 * o mapeamento explícito é mais rápido de depurar e torna visível, no próprio
 * código, que a senha não atravessa para a resposta.
 *
 * Note que getDocumento() dispensa qualquer teste de tipo: a entidade sabe
 * responder qual é o seu documento.
 */
@Component
public class UsuarioMapper {

    public UsuarioResponse paraResposta(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getLogin(),
                usuario.getTipo(),
                usuario.getDocumento(),
                paraEnderecoResposta(usuario.getEndereco()),
                usuario.getDataCriacao(),
                usuario.getDataUltimaAlteracao()
        );
    }

    public LoginResponse paraRespostaLogin(Usuario usuario) {
        return new LoginResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getTipo()
        );
    }

    public Endereco paraEntidade(EnderecoRequest requisicao, String cepNormalizado) {
        return new Endereco(
                requisicao.rua(),
                requisicao.numero(),
                requisicao.complemento(),
                requisicao.bairro(),
                requisicao.cidade(),
                requisicao.estado().toUpperCase(),
                cepNormalizado
        );
    }

    private EnderecoResponse paraEnderecoResposta(Endereco endereco) {
        if (endereco == null) {
            return null;
        }
        return new EnderecoResponse(
                endereco.getRua(),
                endereco.getNumero(),
                endereco.getComplemento(),
                endereco.getBairro(),
                endereco.getCidade(),
                endereco.getEstado(),
                endereco.getCep()
        );
    }
}
