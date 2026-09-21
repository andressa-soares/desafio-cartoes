package br.com.desafio.cartoes.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static br.com.desafio.cartoes.domain.fixture.ClienteFixture.umCliente;
import static br.com.desafio.cartoes.domain.fixture.ProdutoFixture.umProduto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Solicitacao")
class SolicitacaoTest {

    private static final UUID NUMERO = UUID.fromString("11111111-2222-3333-4444-555555555555");
    private static final LocalDateTime DATA = LocalDateTime.of(2026, 1, 10, 9, 30);

    private static CartaoOfertado umCartao() {
        return CartaoOfertado.aprovado(umProduto().build());
    }

    @Nested
    @DisplayName("construtor")
    class Construtor {

        @Test
        @DisplayName("mantém os dados informados quando todos são válidos")
        void criaSolicitacaoComOsDadosInformados() {
            var cliente = umCliente().build();
            var cartao = umCartao();

            var solicitacao = new Solicitacao(NUMERO, DATA, cliente, List.of(cartao));

            assertThat(solicitacao.numero()).isEqualTo(NUMERO);
            assertThat(solicitacao.data()).isEqualTo(DATA);
            assertThat(solicitacao.cliente()).isEqualTo(cliente);
            assertThat(solicitacao.cartoesOfertados()).containsExactly(cartao);
        }

        @Test
        @DisplayName("rejeita número nulo")
        void rejeitaNumeroNulo() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new Solicitacao(null, DATA, umCliente().build(), List.of()))
                    .withMessage("Número é obrigatório.");
        }

        @Test
        @DisplayName("rejeita data nula")
        void rejeitaDataNula() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new Solicitacao(NUMERO, null, umCliente().build(), List.of()))
                    .withMessage("Data é obrigatória.");
        }

        @Test
        @DisplayName("rejeita cliente nulo")
        void rejeitaClienteNulo() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new Solicitacao(NUMERO, DATA, null, List.of()))
                    .withMessage("Cliente é obrigatório.");
        }

        @Test
        @DisplayName("rejeita lista de cartões nula")
        void rejeitaListaDeCartoesNula() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new Solicitacao(NUMERO, DATA, umCliente().build(), null))
                    .withMessage("Cartões ofertados é obrigatório.");
        }

        @Test
        @DisplayName("aceita lista de cartões vazia")
        void aceitaListaDeCartoesVazia() {
            var solicitacao = new Solicitacao(NUMERO, DATA, umCliente().build(), List.of());

            assertThat(solicitacao.cartoesOfertados()).isEmpty();
        }

        @Test
        @DisplayName("copia a lista recebida, isolando alterações posteriores na origem")
        void copiaListaRecebida() {
            var cartoes = new ArrayList<CartaoOfertado>();
            cartoes.add(umCartao());
            var solicitacao = new Solicitacao(NUMERO, DATA, umCliente().build(), cartoes);

            cartoes.clear();

            assertThat(solicitacao.cartoesOfertados()).hasSize(1);
        }

        @Test
        @DisplayName("expõe uma lista de cartões imutável")
        void exponeListaImutavel() {
            var solicitacao = new Solicitacao(NUMERO, DATA, umCliente().build(), List.of(umCartao()));

            assertThatThrownBy(() -> solicitacao.cartoesOfertados().add(umCartao()))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("possuiCartaoAprovado")
    class PossuiCartaoAprovado {

        @Test
        @DisplayName("confirma quando há ao menos um cartão ofertado")
        void confirmaQuandoHaCartaoOfertado() {
            var solicitacao = new Solicitacao(NUMERO, DATA, umCliente().build(), List.of(umCartao()));

            var resultado = solicitacao.possuiCartaoAprovado();

            assertThat(resultado).isTrue();
        }

        @Test
        @DisplayName("nega quando a lista de cartões está vazia")
        void negaQuandoNaoHaCartaoOfertado() {
            var solicitacao = new Solicitacao(NUMERO, DATA, umCliente().build(), List.of());

            var resultado = solicitacao.possuiCartaoAprovado();

            assertThat(resultado).isFalse();
        }
    }
}
