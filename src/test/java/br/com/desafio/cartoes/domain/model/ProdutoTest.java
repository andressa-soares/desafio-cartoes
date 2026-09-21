package br.com.desafio.cartoes.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static br.com.desafio.cartoes.domain.fixture.ProdutoFixture.umProduto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

@DisplayName("Produto")
class ProdutoTest {

    @Nested
    @DisplayName("construtor")
    class Construtor {

        @Test
        @DisplayName("mantém os valores informados quando todos são válidos")
        void criaProdutoComOsValoresInformados() {
            var produto = umProduto()
                    .doTipo(TipoCartao.CARTAO_DE_PARCEIROS)
                    .comRendaMinima("2000.00")
                    .comLimite("800.00")
                    .comAnuidadeMensal("15.00")
                    .build();

            assertThat(produto.tipo()).isEqualTo(TipoCartao.CARTAO_DE_PARCEIROS);
            assertThat(produto.rendaMinima()).isEqualByComparingTo("2000.00");
            assertThat(produto.limite()).isEqualByComparingTo("800.00");
            assertThat(produto.anuidadeMensal()).isEqualByComparingTo("15.00");
        }

        @Test
        @DisplayName("rejeita tipo nulo")
        void rejeitaTipoNulo() {
            var fixture = umProduto().doTipo(null);

            assertThatNullPointerException()
                    .isThrownBy(fixture::build)
                    .withMessage("Tipo é obrigatório.");
        }

        @Test
        @DisplayName("rejeita renda mínima nula")
        void rejeitaRendaMinimaNula() {
            var fixture = umProduto().comRendaMinima(null);

            assertThatNullPointerException()
                    .isThrownBy(fixture::build)
                    .withMessage("Renda mínima é obrigatória.");
        }

        @Test
        @DisplayName("rejeita limite nulo")
        void rejeitaLimiteNulo() {
            var fixture = umProduto().comLimite(null);

            assertThatNullPointerException()
                    .isThrownBy(fixture::build)
                    .withMessage("Limite é obrigatório.");
        }

        @Test
        @DisplayName("rejeita anuidade mensal nula")
        void rejeitaAnuidadeMensalNula() {
            var fixture = umProduto().comAnuidadeMensal(null);

            assertThatNullPointerException()
                    .isThrownBy(fixture::build)
                    .withMessage("Anuidade mensal é obrigatória.");
        }

        @Test
        @DisplayName("rejeita renda mínima negativa")
        void rejeitaRendaMinimaNegativa() {
            var fixture = umProduto().comRendaMinima("-0.01");

            assertThatIllegalArgumentException()
                    .isThrownBy(fixture::build)
                    .withMessage("Valores do produto não podem ser negativos.");
        }

        @Test
        @DisplayName("rejeita limite negativo")
        void rejeitaLimiteNegativo() {
            var fixture = umProduto().comLimite("-0.01");

            assertThatIllegalArgumentException()
                    .isThrownBy(fixture::build)
                    .withMessage("Valores do produto não podem ser negativos.");
        }

        @Test
        @DisplayName("rejeita anuidade mensal negativa")
        void rejeitaAnuidadeMensalNegativa() {
            var fixture = umProduto().comAnuidadeMensal("-0.01");

            assertThatIllegalArgumentException()
                    .isThrownBy(fixture::build)
                    .withMessage("Valores do produto não podem ser negativos.");
        }

        @Test
        @DisplayName("aceita renda mínima, limite e anuidade iguais a zero")
        void aceitaValoresZerados() {
            var fixture = umProduto()
                    .comRendaMinima("0.00")
                    .comLimite("0.00")
                    .comAnuidadeMensal("0.00");

            assertThatCode(fixture::build).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("atendeRenda")
    class AtendeRenda {

        @Test
        @DisplayName("atende quando a renda do cliente é maior que a renda mínima")
        void atendeQuandoRendaEhMaior() {
            var produto = umProduto().comRendaMinima("1000.00").build();

            var resultado = produto.atendeRenda(new BigDecimal("1000.01"));

            assertThat(resultado).isTrue();
        }

        @Test
        @DisplayName("atende quando a renda do cliente é exatamente a renda mínima")
        void atendeQuandoRendaEhExatamenteAMinima() {
            var produto = umProduto().comRendaMinima("1000.00").build();

            var resultado = produto.atendeRenda(new BigDecimal("1000.00"));

            assertThat(resultado).isTrue();
        }

        @Test
        @DisplayName("não atende quando a renda do cliente fica um centavo abaixo da mínima")
        void naoAtendeQuandoRendaFicaUmCentavoAbaixo() {
            var produto = umProduto().comRendaMinima("1000.00").build();

            var resultado = produto.atendeRenda(new BigDecimal("999.99"));

            assertThat(resultado).isFalse();
        }

        @Test
        @DisplayName("atende quando o valor é igual mas a escala é diferente")
        void atendeQuandoValorEhIgualComEscalaDiferente() {
            var produto = umProduto().comRendaMinima("1000.00").build();

            var resultado = produto.atendeRenda(new BigDecimal("1000.0000"));

            assertThat(resultado).isTrue();
        }

        @Test
        @DisplayName("atende qualquer renda quando a renda mínima é zero")
        void atendeQualquerRendaQuandoMinimaEhZero() {
            var produto = umProduto().comRendaMinima("0.00").build();

            var resultado = produto.atendeRenda(BigDecimal.ZERO);

            assertThat(resultado).isTrue();
        }
    }
}
