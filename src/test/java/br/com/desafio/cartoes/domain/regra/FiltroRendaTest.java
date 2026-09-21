package br.com.desafio.cartoes.domain.regra;

import br.com.desafio.cartoes.domain.model.Produto;
import br.com.desafio.cartoes.domain.model.TipoCartao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static br.com.desafio.cartoes.domain.fixture.ClienteFixture.umCliente;
import static br.com.desafio.cartoes.domain.fixture.ProdutoFixture.umProduto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("FiltroRenda")
class FiltroRendaTest {

    private static final Produto BARATO = umProduto()
            .doTipo(TipoCartao.CARTAO_SEM_ANUIDADE)
            .comRendaMinima("1000.00")
            .build();

    private static final Produto INTERMEDIARIO = umProduto()
            .doTipo(TipoCartao.CARTAO_DE_PARCEIROS)
            .comRendaMinima("2000.00")
            .build();

    private static final Produto CARO = umProduto()
            .doTipo(TipoCartao.CARTAO_COM_CASHBACK)
            .comRendaMinima("3000.00")
            .build();

    @Nested
    @DisplayName("construtor")
    class Construtor {

        @Test
        @DisplayName("rejeita lista de produtos nula")
        void rejeitaListaDeProdutosNula() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new FiltroRenda(null));
        }

        @Test
        @DisplayName("copia a lista de produtos recebida, isolando alterações na origem")
        void copiaListaDeProdutosRecebida() {
            var produtos = new ArrayList<Produto>();
            produtos.add(BARATO);
            var filtro = new FiltroRenda(produtos);

            produtos.clear();

            assertThat(filtro.aplicar(umCliente().comRendaMensal("1000.00").build()))
                    .containsExactly(BARATO);
        }
    }

    @Nested
    @DisplayName("aplicar")
    class Aplicar {

        private final FiltroRenda filtro = new FiltroRenda(List.of(BARATO, INTERMEDIARIO, CARO));

        @Test
        @DisplayName("devolve todos os produtos quando a renda atinge a maior renda mínima")
        void devolveTodosQuandoRendaAtingeATodos() {
            var cliente = umCliente().comRendaMensal("5000.00").build();

            var resultado = filtro.aplicar(cliente);

            assertThat(resultado).containsExactly(BARATO, INTERMEDIARIO, CARO);
        }

        @Test
        @DisplayName("devolve apenas os produtos cuja renda mínima o cliente atinge")
        void devolveApenasOsProdutosAtingidos() {
            var cliente = umCliente().comRendaMensal("2500.00").build();

            var resultado = filtro.aplicar(cliente);

            assertThat(resultado).containsExactly(BARATO, INTERMEDIARIO);
        }

        @Test
        @DisplayName("inclui o produto quando a renda é exatamente a renda mínima dele")
        void incluiProdutoQuandoRendaEhExatamenteAMinima() {
            var cliente = umCliente().comRendaMensal("2000.00").build();

            var resultado = filtro.aplicar(cliente);

            assertThat(resultado).containsExactly(BARATO, INTERMEDIARIO);
        }

        @Test
        @DisplayName("exclui o produto quando a renda fica um centavo abaixo da renda mínima dele")
        void excluiProdutoQuandoRendaFicaUmCentavoAbaixo() {
            var cliente = umCliente().comRendaMensal("1999.99").build();

            var resultado = filtro.aplicar(cliente);

            assertThat(resultado).containsExactly(BARATO);
        }

        @Test
        @DisplayName("compara valores e não escalas ao avaliar a renda mínima")
        void comparaValoresIndependenteDaEscala() {
            var cliente = umCliente().comRendaMensal("2000.0000").build();

            var resultado = filtro.aplicar(cliente);

            assertThat(resultado).containsExactly(BARATO, INTERMEDIARIO);
        }

        @Test
        @DisplayName("devolve lista vazia quando a renda não atinge nenhum produto")
        void devolveListaVaziaQuandoNenhumProdutoEhAtingido() {
            var cliente = umCliente().comRendaMensal("999.99").build();

            var resultado = filtro.aplicar(cliente);

            assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("devolve lista vazia quando não há produtos cadastrados")
        void devolveListaVaziaQuandoNaoHaProdutos() {
            var filtroSemProdutos = new FiltroRenda(List.of());

            var resultado = filtroSemProdutos.aplicar(umCliente().comRendaMensal("9999.00").build());

            assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("preserva a ordem original dos produtos")
        void preservaOrdemOriginalDosProdutos() {
            var filtroInvertido = new FiltroRenda(List.of(CARO, INTERMEDIARIO, BARATO));

            var resultado = filtroInvertido.aplicar(umCliente().comRendaMensal("5000.00").build());

            assertThat(resultado).containsExactly(CARO, INTERMEDIARIO, BARATO);
        }

        @Test
        @DisplayName("devolve uma lista imutável")
        void devolveListaImutavel() {
            var resultado = filtro.aplicar(umCliente().comRendaMensal("1000.00").build());

            assertThatThrownBy(() -> resultado.add(CARO))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("devolve o mesmo resultado em chamadas sucessivas para o mesmo cliente")
        void devolveMesmoResultadoEmChamadasSucessivas() {
            var cliente = umCliente().comRendaMensal("2500.00").build();

            var primeira = filtro.aplicar(cliente);
            var segunda = filtro.aplicar(cliente);

            assertThat(segunda).isEqualTo(primeira);
        }
    }
}
