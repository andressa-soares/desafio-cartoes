package br.com.desafio.cartoes.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static br.com.desafio.cartoes.domain.fixture.ProdutoFixture.umProduto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

@DisplayName("CartaoOfertado")
class CartaoOfertadoTest {

    @Nested
    @DisplayName("aprovado")
    class Aprovado {

        @Test
        @DisplayName("copia tipo, anuidade e limite do produto de origem")
        void copiaDadosDoProduto() {
            var produto = umProduto()
                    .doTipo(TipoCartao.CARTAO_COM_CASHBACK)
                    .comLimite("900.00")
                    .comAnuidadeMensal("25.00")
                    .build();

            var cartao = CartaoOfertado.aprovado(produto);

            assertThat(cartao.tipo()).isEqualTo(TipoCartao.CARTAO_COM_CASHBACK);
            assertThat(cartao.anuidadeMensal()).isEqualByComparingTo("25.00");
            assertThat(cartao.limiteDisponivel()).isEqualByComparingTo("900.00");
        }

        @Test
        @DisplayName("marca o cartão como APROVADO")
        void marcaCartaoComoAprovado() {
            var cartao = CartaoOfertado.aprovado(umProduto().build());

            assertThat(cartao.status()).isEqualTo(StatusCartao.APROVADO);
        }

        @Test
        @DisplayName("usa o limite do produto como limite disponível, sem trocar com a anuidade")
        void naoTrocaLimiteComAnuidade() {
            var produto = umProduto()
                    .comLimite("500.00")
                    .comAnuidadeMensal("10.00")
                    .build();

            var cartao = CartaoOfertado.aprovado(produto);

            assertThat(cartao.limiteDisponivel()).isEqualByComparingTo(produto.limite());
            assertThat(cartao.anuidadeMensal()).isEqualByComparingTo(produto.anuidadeMensal());
        }

        @Test
        @DisplayName("falha quando o produto é nulo")
        void falhaQuandoProdutoEhNulo() {
            assertThatNullPointerException()
                    .isThrownBy(() -> CartaoOfertado.aprovado(null));
        }
    }
}
