package br.com.fiap.restaurante.domain.entity;

import br.com.fiap.restaurante.domain.enums.TipoCozinha;
import br.com.fiap.restaurante.domain.exception.RegraDeNegocioException;
import br.com.fiap.restaurante.domain.exception.ValidacaoDeDominioException;
import br.com.fiap.restaurante.domain.valueobject.Endereco;
import br.com.fiap.restaurante.domain.valueobject.QuadroDeHorarios;
import br.com.fiap.restaurante.domain.valueobject.TextoLivre;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Restaurante. O dono precisa ser do tipo Dono de Restaurante, no cadastro e na
 * transferência. As datas são preenchidas pela persistência e só lidas aqui.
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Restaurante {

    @EqualsAndHashCode.Include
    private Long id;
    private String nome;
    private Endereco endereco;
    private TipoCozinha tipoCozinha;
    private QuadroDeHorarios horarios;
    private Usuario dono;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataUltimaAlteracao;

    private Restaurante() {
    }

    public static Restaurante create(String nome, Endereco endereco, TipoCozinha tipoCozinha,
                                     QuadroDeHorarios horarios, Usuario dono) {
        Restaurante restaurante = new Restaurante();
        restaurante.setNome(nome);
        restaurante.setEndereco(endereco);
        restaurante.setTipoCozinha(tipoCozinha);
        restaurante.setHorarios(horarios);
        restaurante.setDono(dono);
        return restaurante;
    }

    public static Restaurante create(Long id, String nome, Endereco endereco, TipoCozinha tipoCozinha,
                                     QuadroDeHorarios horarios, Usuario dono, LocalDateTime dataCriacao,
                                     LocalDateTime dataUltimaAlteracao) {
        Restaurante restaurante = create(nome, endereco, tipoCozinha, horarios, dono);
        restaurante.id = id;
        restaurante.dataCriacao = dataCriacao;
        restaurante.dataUltimaAlteracao = dataUltimaAlteracao;
        return restaurante;
    }

    public void setNome(String nome) {
        TextoLivre.exigirUmaLinha(nome, "nome do restaurante");
        if (nome == null || nome.trim().length() < 2 || nome.trim().length() > 120) {
            throw new ValidacaoDeDominioException("O nome do restaurante deve ter entre 2 e 120 caracteres.");
        }
        this.nome = nome.trim();
    }

    public void setEndereco(Endereco endereco) {
        if (endereco == null) {
            throw new ValidacaoDeDominioException("O endereço é obrigatório.");
        }
        this.endereco = endereco;
    }

    public void setTipoCozinha(TipoCozinha tipoCozinha) {
        if (tipoCozinha == null) {
            throw new ValidacaoDeDominioException("O tipo de cozinha é obrigatório.");
        }
        this.tipoCozinha = tipoCozinha;
    }

    /** Substitui todos os turnos. */
    public void setHorarios(QuadroDeHorarios horarios) {
        if (horarios == null) {
            throw new ValidacaoDeDominioException("Informe ao menos um turno de funcionamento.");
        }
        this.horarios = horarios;
    }

    public void setDono(Usuario dono) {
        if (dono == null) {
            throw new ValidacaoDeDominioException("O dono do restaurante é obrigatório.");
        }
        if (!dono.ehDonoDeRestaurante()) {
            throw new RegraDeNegocioException("O usuário " + dono.getId() + " não é Dono de Restaurante.");
        }
        this.dono = dono;
    }
}
