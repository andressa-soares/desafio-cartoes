package br.com.desafio.cartoes.domain.fixture;

import br.com.desafio.cartoes.domain.model.Produto;
import br.com.desafio.cartoes.domain.model.TipoCartao;

import java.math.BigDecimal;
import java.util.List;

/* Renda mínima, limite e anuidade são valores de configuração em produção, então aqui entram pelo
   construtor com números arbitrários. */

public final class ProdutoFixture {

    private TipoCartao tipo = TipoCartao.CARTAO_SEM_ANUIDADE;
    private BigDecimal rendaMinima = new BigDecimal("1000.00");
    private BigDecimal limite = new BigDecimal("500.00");
    private BigDecimal anuidadeMensal = new BigDecimal("10.00");

    private ProdutoFixture() {
    }

    public static ProdutoFixture umProduto() {
        return new ProdutoFixture();
    }

    public ProdutoFixture doTipo(TipoCartao tipo) {
        this.tipo = tipo;
        return this;
    }

    public ProdutoFixture comRendaMinima(String rendaMinima) {
        this.rendaMinima = rendaMinima == null ? null : new BigDecimal(rendaMinima);
        return this;
    }

    public ProdutoFixture comLimite(String limite) {
        this.limite = limite == null ? null : new BigDecimal(limite);
        return this;
    }

    public ProdutoFixture comAnuidadeMensal(String anuidadeMensal) {
        this.anuidadeMensal = anuidadeMensal == null ? null : new BigDecimal(anuidadeMensal);
        return this;
    }

    public Produto build() {
        return new Produto(tipo, rendaMinima, limite, anuidadeMensal);
    }

    // Catálogo com os três tipos, rendas mínimas crescentes e arbitrárias, na
    // mesma ordem do domínio — usado pelos testes que precisam de um FiltroRenda real.
    public static List<Produto> catalogo() {
        return List.of(
                umProduto().doTipo(TipoCartao.CARTAO_SEM_ANUIDADE)
                        .comRendaMinima("1000.00").comLimite("500.00").comAnuidadeMensal("0.00").build(),
                umProduto().doTipo(TipoCartao.CARTAO_DE_PARCEIROS)
                        .comRendaMinima("2000.00").comLimite("800.00").comAnuidadeMensal("10.00").build(),
                umProduto().doTipo(TipoCartao.CARTAO_COM_CASHBACK)
                        .comRendaMinima("3000.00").comLimite("1200.00").comAnuidadeMensal("20.00").build()
        );
    }
}
