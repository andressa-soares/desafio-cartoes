package br.com.desafio.cartoes.config;

import br.com.desafio.cartoes.domain.model.FaixaEtaria;
import br.com.desafio.cartoes.domain.model.Produto;
import br.com.desafio.cartoes.domain.model.TipoCartao;
import br.com.desafio.cartoes.domain.regra.ParametrosRegras;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/*
 * Espelha o prefixo `cartoes` do application.yml e converte o que foi lido em
 * objetos de domínio. Nenhum valor de negócio é literal aqui: tudo chega pela
 * vinculação com o yml, validado antes de qualquer conversão.
 */
@ConfigurationProperties(prefix = "cartoes")
@Validated
public record CartoesProperties(
        @Min(0) int idadeMinima,
        @NotNull @Valid Faixa faixaJovem,
        @NotNull @Valid Faixa faixaExcecaoResidenteSp,
        @NotEmpty List<@Valid ProdutoProperties> produtos
) {

    public record Faixa(@NotNull Integer inicio, @NotNull Integer fim) {
    }

    public record ProdutoProperties(
            @NotNull TipoCartao tipo,
            @NotNull BigDecimal rendaMinima,
            @NotNull BigDecimal limite,
            @NotNull BigDecimal anuidadeMensal
    ) {
    }

    /*
     * Preserva a ordem do yml, que é a ordem da resposta da API. Tipo repetido é
     * erro de configuração, não de negócio: falha cedo, na subida da aplicação.
     */
    public List<Produto> toProdutos() {
        var tiposVistos = EnumSet.noneOf(TipoCartao.class);
        var resultado = new ArrayList<Produto>(produtos.size());
        for (ProdutoProperties produto : produtos) {
            if (!tiposVistos.add(produto.tipo())) {
                throw new IllegalStateException("Tipo de cartão duplicado na configuração: " + produto.tipo());
            }
            resultado.add(new Produto(produto.tipo(), produto.rendaMinima(), produto.limite(), produto.anuidadeMensal()));
        }
        return List.copyOf(resultado);
    }

    public ParametrosRegras toParametrosRegras() {
        return new ParametrosRegras(
                new FaixaEtaria(faixaJovem.inicio(), faixaJovem.fim()),
                new FaixaEtaria(faixaExcecaoResidenteSp.inicio(), faixaExcecaoResidenteSp.fim())
        );
    }
}
