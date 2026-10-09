package br.com.fiap.restaurante.domain.entity;

import br.com.fiap.restaurante.domain.exception.ValidacaoDeDominioException;
import br.com.fiap.restaurante.domain.valueobject.Preco;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Item do cardápio de um restaurante. O item não muda de restaurante. A foto é só
 * o caminho de onde ela estaria: o enunciado dispensa o upload.
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ItemCardapio {

    private static final Pattern IMAGEM = Pattern.compile("(?i).+\\.(jpg|jpeg|png|webp)");
    private static final Pattern ESQUEMA = Pattern.compile("^[A-Za-z][A-Za-z0-9+.-]*:");
    private static final Pattern URL_WEB = Pattern.compile("(?i)^https?://\\S+");

    @EqualsAndHashCode.Include
    private Long id;
    private Long restauranteId;
    private String nome;
    private String descricao;
    private Preco preco;
    private Boolean apenasNoLocal;
    private String caminhoFoto;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataUltimaAlteracao;

    private ItemCardapio() {
    }

    public static ItemCardapio create(Long restauranteId, String nome, String descricao, Preco preco,
                                      Boolean apenasNoLocal, String caminhoFoto) {
        if (restauranteId == null) {
            throw new ValidacaoDeDominioException("O restaurante do item é obrigatório.");
        }
        ItemCardapio item = new ItemCardapio();
        item.restauranteId = restauranteId;
        item.setNome(nome);
        item.setDescricao(descricao);
        item.setPreco(preco);
        item.setApenasNoLocal(apenasNoLocal);
        item.setCaminhoFoto(caminhoFoto);
        return item;
    }

    public static ItemCardapio create(Long id, Long restauranteId, String nome, String descricao, Preco preco,
                                      Boolean apenasNoLocal, String caminhoFoto, LocalDateTime dataCriacao,
                                      LocalDateTime dataUltimaAlteracao) {
        ItemCardapio item = create(restauranteId, nome, descricao, preco, apenasNoLocal, caminhoFoto);
        item.id = id;
        item.dataCriacao = dataCriacao;
        item.dataUltimaAlteracao = dataUltimaAlteracao;
        return item;
    }

    public boolean pertenceA(Long outroRestauranteId) {
        return restauranteId.equals(outroRestauranteId);
    }

    /** Tira os espaços das pontas e junta os repetidos: "  Feijoada   completa " vira "Feijoada completa". */
    public void setNome(String nome) {
        String limpo = nome == null ? "" : nome.trim().replaceAll("\\s+", " ");
        if (limpo.length() < 2 || limpo.length() > 100) {
            throw new ValidacaoDeDominioException("O nome do item deve ter entre 2 e 100 caracteres.");
        }
        this.nome = limpo;
    }

    public void setDescricao(String descricao) {
        if (descricao == null || descricao.isBlank()) {
            throw new ValidacaoDeDominioException("A descrição do item é obrigatória.");
        }
        if (descricao.trim().length() > 500) {
            throw new ValidacaoDeDominioException("A descrição do item deve ter no máximo 500 caracteres.");
        }
        this.descricao = descricao.trim();
    }

    public void setPreco(Preco preco) {
        if (preco == null) {
            throw new ValidacaoDeDominioException("O preço é obrigatório.");
        }
        this.preco = preco;
    }

    // Sem valor padrão: o restaurante precisa dizer
    public void setApenasNoLocal(Boolean apenasNoLocal) {
        if (apenasNoLocal == null) {
            throw new ValidacaoDeDominioException("Informe se o item está disponível só para consumo no local.");
        }
        this.apenasNoLocal = apenasNoLocal;
    }

    /**
     * Aceita caminho relativo ou URL http ou https, terminado em .jpg, .jpeg, .png
     * ou .webp. O caminho não pode subir de pasta: quem for buscar a foto vai
     * seguir esse caminho.
     */
    public void setCaminhoFoto(String caminhoFoto) {
        if (caminhoFoto == null || caminhoFoto.isBlank()) {
            throw new ValidacaoDeDominioException("O caminho da foto é obrigatório.");
        }
        String limpo = caminhoFoto.trim();
        if (limpo.length() > 255) {
            throw new ValidacaoDeDominioException("O caminho da foto deve ter no máximo 255 caracteres.");
        }
        if (!IMAGEM.matcher(limpo).matches()) {
            throw new ValidacaoDeDominioException("O caminho da foto deve terminar em .jpg, .jpeg, .png ou .webp.");
        }
        if (ESQUEMA.matcher(limpo).find() && !URL_WEB.matcher(limpo).matches()) {
            throw new ValidacaoDeDominioException("A foto deve ser um caminho relativo ou uma URL http ou https.");
        }
        if (Arrays.asList(limpo.split("[/\\\\]")).contains("..")) {
            throw new ValidacaoDeDominioException("O caminho da foto não pode subir de pasta com \"..\".");
        }
        this.caminhoFoto = limpo;
    }
}
