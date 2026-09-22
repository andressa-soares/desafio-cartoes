package br.com.desafio.cartoes.config.fixture;

import br.com.desafio.cartoes.config.CartoesProperties;
import br.com.desafio.cartoes.config.CartoesProperties.Faixa;
import br.com.desafio.cartoes.config.CartoesProperties.ProdutoProperties;
import br.com.desafio.cartoes.domain.model.TipoCartao;

import java.math.BigDecimal;
import java.util.List;

/*
 * Construtor de CartoesProperties para os testes. Os valores padrão são
 * arbitrários e sem significado de negócio, no mesmo espírito de
 * br.com.desafio.cartoes.domain.fixture.ProdutoFixture.
 */
public final class CartoesPropertiesFixture {

    private int idadeMinima = 18;
    private Faixa faixaJovem = new Faixa(18, 24);
    private Faixa faixaExcecaoResidenteSp = new Faixa(25, 29);
    private List<ProdutoProperties> produtos = List.of(
            produto(TipoCartao.CARTAO_SEM_ANUIDADE, "1000.00", "500.00", "0.00"),
            produto(TipoCartao.CARTAO_DE_PARCEIROS, "2000.00", "800.00", "10.00"),
            produto(TipoCartao.CARTAO_COM_CASHBACK, "3000.00", "1200.00", "20.00")
    );

    private CartoesPropertiesFixture() {
    }

    public static CartoesPropertiesFixture umasPropriedades() {
        return new CartoesPropertiesFixture();
    }

    public static ProdutoProperties produto(TipoCartao tipo, String rendaMinima, String limite, String anuidadeMensal) {
        return new ProdutoProperties(tipo, new BigDecimal(rendaMinima), new BigDecimal(limite), new BigDecimal(anuidadeMensal));
    }

    public CartoesPropertiesFixture comIdadeMinima(int idadeMinima) {
        this.idadeMinima = idadeMinima;
        return this;
    }

    public CartoesPropertiesFixture comFaixaJovem(Faixa faixaJovem) {
        this.faixaJovem = faixaJovem;
        return this;
    }

    public CartoesPropertiesFixture comFaixaExcecaoResidenteSp(Faixa faixaExcecaoResidenteSp) {
        this.faixaExcecaoResidenteSp = faixaExcecaoResidenteSp;
        return this;
    }

    public CartoesPropertiesFixture comProdutos(List<ProdutoProperties> produtos) {
        this.produtos = produtos;
        return this;
    }

    public CartoesProperties build() {
        return new CartoesProperties(idadeMinima, faixaJovem, faixaExcecaoResidenteSp, produtos);
    }
}
