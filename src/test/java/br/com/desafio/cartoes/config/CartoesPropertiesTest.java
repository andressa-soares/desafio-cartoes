package br.com.desafio.cartoes.config;

import br.com.desafio.cartoes.config.CartoesProperties.Faixa;
import br.com.desafio.cartoes.domain.model.FaixaEtaria;
import br.com.desafio.cartoes.domain.model.Produto;
import br.com.desafio.cartoes.domain.model.TipoCartao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static br.com.desafio.cartoes.config.fixture.CartoesPropertiesFixture.produto;
import static br.com.desafio.cartoes.config.fixture.CartoesPropertiesFixture.umasPropriedades;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

@DisplayName("CartoesProperties")
class CartoesPropertiesTest {

    @Nested
    @DisplayName("toProdutos")
    class ToProdutos {

        @Test
        @DisplayName("converte todos os campos e preserva a ordem configurada")
        void convertePreservandoOrdem() {
            var propriedades = umasPropriedades()
                    .comProdutos(List.of(
                            produto(TipoCartao.CARTAO_COM_CASHBACK, "3000.00", "1200.00", "20.00"),
                            produto(TipoCartao.CARTAO_SEM_ANUIDADE, "1000.00", "500.00", "0.00")
                    ))
                    .build();

            var produtos = propriedades.toProdutos();

            assertThat(produtos).extracting(Produto::tipo)
                    .containsExactly(TipoCartao.CARTAO_COM_CASHBACK, TipoCartao.CARTAO_SEM_ANUIDADE);
            assertThat(produtos.get(0).rendaMinima()).isEqualByComparingTo("3000.00");
            assertThat(produtos.get(0).limite()).isEqualByComparingTo("1200.00");
            assertThat(produtos.get(0).anuidadeMensal()).isEqualByComparingTo("20.00");
            assertThat(produtos.get(1).rendaMinima()).isEqualByComparingTo("1000.00");
            assertThat(produtos.get(1).limite()).isEqualByComparingTo("500.00");
            assertThat(produtos.get(1).anuidadeMensal()).isEqualByComparingTo("0.00");
        }

        @Test
        @DisplayName("lança IllegalStateException citando o tipo quando há tipo repetido")
        void lancaExcecaoComTipoRepetido() {
            var propriedades = umasPropriedades()
                    .comProdutos(List.of(
                            produto(TipoCartao.CARTAO_SEM_ANUIDADE, "1000.00", "500.00", "0.00"),
                            produto(TipoCartao.CARTAO_SEM_ANUIDADE, "2000.00", "800.00", "0.00")
                    ))
                    .build();

            assertThatIllegalStateException()
                    .isThrownBy(propriedades::toProdutos)
                    .withMessageContaining("CARTAO_SEM_ANUIDADE");
        }

        @Test
        @DisplayName("propaga a exceção de domínio quando um produto tem valor negativo")
        void propagaExcecaoDeDominioComValorNegativo() {
            var propriedades = umasPropriedades()
                    .comProdutos(List.of(produto(TipoCartao.CARTAO_SEM_ANUIDADE, "-0.01", "500.00", "0.00")))
                    .build();

            assertThatIllegalArgumentException().isThrownBy(propriedades::toProdutos);
        }
    }

    @Nested
    @DisplayName("toParametrosRegras")
    class ToParametrosRegras {

        @Test
        @DisplayName("converte as duas faixas etárias configuradas")
        void convertemAsDuasFaixas() {
            var propriedades = umasPropriedades()
                    .comFaixaJovem(new Faixa(18, 24))
                    .comFaixaExcecaoResidenteSp(new Faixa(25, 29))
                    .build();

            var parametros = propriedades.toParametrosRegras();

            assertThat(parametros.faixaJovem()).isEqualTo(new FaixaEtaria(18, 24));
            assertThat(parametros.faixaExcecaoResidenteSp()).isEqualTo(new FaixaEtaria(25, 29));
        }

        @Test
        @DisplayName("propaga a exceção de domínio quando o fim da faixa é menor que o início")
        void propagaExcecaoDeDominioComFaixaInvertida() {
            var propriedades = umasPropriedades()
                    .comFaixaJovem(new Faixa(24, 18))
                    .build();

            assertThatIllegalArgumentException().isThrownBy(propriedades::toParametrosRegras);
        }
    }
}
