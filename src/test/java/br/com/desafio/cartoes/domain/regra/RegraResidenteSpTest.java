package br.com.desafio.cartoes.domain.regra;

import br.com.desafio.cartoes.domain.model.TipoCartao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static br.com.desafio.cartoes.domain.fixture.ClienteFixture.umCliente;
import static br.com.desafio.cartoes.domain.fixture.ParametrosRegrasFixture.padrao;
import static br.com.desafio.cartoes.domain.model.TipoCartao.CARTAO_COM_CASHBACK;
import static br.com.desafio.cartoes.domain.model.TipoCartao.CARTAO_DE_PARCEIROS;
import static br.com.desafio.cartoes.domain.model.TipoCartao.CARTAO_SEM_ANUIDADE;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RegraResidenteSp")
class RegraResidenteSpTest {

    private static final Set<TipoCartao> TODOS =
            Set.of(CARTAO_SEM_ANUIDADE, CARTAO_DE_PARCEIROS, CARTAO_COM_CASHBACK);

    private final RegraResidenteSp regra = new RegraResidenteSp(padrao());

    @Nested
    @DisplayName("aplicar")
    class Aplicar {

        @ParameterizedTest(name = "idade {0}")
        @ValueSource(ints = {24, 30, 32})
        @DisplayName("mantém apenas sem anuidade e cashback em SP fora da faixa de exceção")
        void mantemSemAnuidadeECashbackEmSpForaDaExcecao(int idade) {
            var cliente = umCliente().comIdadeEmSp(idade).build();

            var resultado = regra.aplicar(cliente, TODOS);

            assertThat(resultado).containsExactlyInAnyOrder(CARTAO_SEM_ANUIDADE, CARTAO_COM_CASHBACK);
        }

        @Test
        @DisplayName("devolve os candidatos intactos na idade de início da faixa de exceção")
        void devolveCandidatosIntactosNoInicioDaExcecao() {
            var cliente = umCliente().comIdadeEmSp(25).build();

            var resultado = regra.aplicar(cliente, TODOS);

            assertThat(resultado).containsExactlyInAnyOrderElementsOf(TODOS);
        }

        @Test
        @DisplayName("devolve os candidatos intactos na idade de fim da faixa de exceção")
        void devolveCandidatosIntactosNoFimDaExcecao() {
            var cliente = umCliente().comIdadeEmSp(29).build();

            var resultado = regra.aplicar(cliente, TODOS);

            assertThat(resultado).containsExactlyInAnyOrderElementsOf(TODOS);
        }

        @ParameterizedTest(name = "idade {0}")
        @ValueSource(ints = {26, 27, 28})
        @DisplayName("devolve os candidatos intactos nas idades dentro da faixa de exceção")
        void devolveCandidatosIntactosDentroDaExcecao(int idade) {
            var cliente = umCliente().comIdadeEmSp(idade).build();

            var resultado = regra.aplicar(cliente, TODOS);

            assertThat(resultado).containsExactlyInAnyOrderElementsOf(TODOS);
        }

        @ParameterizedTest(name = "idade {0}")
        @ValueSource(ints = {18, 24, 25, 29, 30, 32})
        @DisplayName("devolve os candidatos intactos para quem não reside em SP")
        void devolveCandidatosIntactosForaDeSp(int idade) {
            var cliente = umCliente().comIdade(idade).comUf("RJ").build();

            var resultado = regra.aplicar(cliente, TODOS);

            assertThat(resultado).containsExactlyInAnyOrderElementsOf(TODOS);
        }

        @Test
        @DisplayName("trata UF em minúsculo como SP")
        void trataUfEmMinusculoComoSp() {
            var cliente = umCliente().comIdade(32).comUf("sp").build();

            var resultado = regra.aplicar(cliente, TODOS);

            assertThat(resultado).containsExactlyInAnyOrder(CARTAO_SEM_ANUIDADE, CARTAO_COM_CASHBACK);
        }

        @Test
        @DisplayName("não oferta o cartão de parceiros a quem tem 32 anos e mora em SP")
        void naoOfertaParceirosAos32AnosEmSp() {
            var cliente = umCliente().comIdadeEmSp(32).build();

            var resultado = regra.aplicar(cliente, TODOS);

            assertThat(resultado).doesNotContain(CARTAO_DE_PARCEIROS);
        }

        @Test
        @DisplayName("devolve vazio quando o único candidato em SP é o cartão de parceiros")
        void devolveVazioQuandoUnicoCandidatoEhParceiros() {
            var cliente = umCliente().comIdadeEmSp(32).build();

            var resultado = regra.aplicar(cliente, Set.of(CARTAO_DE_PARCEIROS));

            assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("devolve vazio quando não há candidatos")
        void devolveVazioQuandoNaoHaCandidatos() {
            var cliente = umCliente().comIdadeEmSp(32).build();

            var resultado = regra.aplicar(cliente, Set.of());

            assertThat(resultado).isEmpty();
        }
    }
}
