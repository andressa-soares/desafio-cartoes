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

@DisplayName("RegraJovem")
class RegraJovemTest {

    private static final Set<TipoCartao> TODOS =
            Set.of(CARTAO_SEM_ANUIDADE, CARTAO_DE_PARCEIROS, CARTAO_COM_CASHBACK);

    private final RegraJovem regra = new RegraJovem(padrao());

    @Nested
    @DisplayName("aplicar")
    class Aplicar {

        @Test
        @DisplayName("mantém apenas o cartão sem anuidade na idade de início da faixa jovem")
        void mantemApenasSemAnuidadeNoInicioDaFaixa() {
            var cliente = umCliente().comIdade(18).build();

            var resultado = regra.aplicar(cliente, TODOS);

            assertThat(resultado).containsExactly(CARTAO_SEM_ANUIDADE);
        }

        @Test
        @DisplayName("mantém apenas o cartão sem anuidade na idade de fim da faixa jovem")
        void mantemApenasSemAnuidadeNoFimDaFaixa() {
            var cliente = umCliente().comIdade(24).build();

            var resultado = regra.aplicar(cliente, TODOS);

            assertThat(resultado).containsExactly(CARTAO_SEM_ANUIDADE);
        }

        @ParameterizedTest(name = "idade {0}")
        @ValueSource(ints = {19, 21, 23})
        @DisplayName("mantém apenas o cartão sem anuidade nas idades dentro da faixa jovem")
        void mantemApenasSemAnuidadeDentroDaFaixa(int idade) {
            var cliente = umCliente().comIdade(idade).build();

            var resultado = regra.aplicar(cliente, TODOS);

            assertThat(resultado).containsExactly(CARTAO_SEM_ANUIDADE);
        }

        @Test
        @DisplayName("devolve os candidatos intactos um ano acima da faixa jovem")
        void devolveCandidatosIntactosUmAnoAcimaDaFaixa() {
            var cliente = umCliente().comIdade(25).build();

            var resultado = regra.aplicar(cliente, TODOS);

            assertThat(resultado).containsExactlyInAnyOrderElementsOf(TODOS);
        }

        @ParameterizedTest(name = "idade {0}")
        @ValueSource(ints = {30, 45, 70})
        @DisplayName("devolve os candidatos intactos para idades acima da faixa jovem")
        void devolveCandidatosIntactosAcimaDaFaixa(int idade) {
            var cliente = umCliente().comIdade(idade).build();

            var resultado = regra.aplicar(cliente, TODOS);

            assertThat(resultado).containsExactlyInAnyOrderElementsOf(TODOS);
        }

        @Test
        @DisplayName("devolve vazio quando o jovem não tem o cartão sem anuidade entre os candidatos")
        void devolveVazioQuandoSemAnuidadeNaoEstaEntreOsCandidatos() {
            var cliente = umCliente().comIdade(22).build();

            var resultado = regra.aplicar(cliente, Set.of(CARTAO_DE_PARCEIROS, CARTAO_COM_CASHBACK));

            assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("não adiciona cartões quando os candidatos já são apenas o sem anuidade")
        void naoAdicionaCartoesQuandoCandidatosJaSaoApenasSemAnuidade() {
            var cliente = umCliente().comIdade(22).build();

            var resultado = regra.aplicar(cliente, Set.of(CARTAO_SEM_ANUIDADE));

            assertThat(resultado).containsExactly(CARTAO_SEM_ANUIDADE);
        }

        @Test
        @DisplayName("devolve vazio quando não há candidatos")
        void devolveVazioQuandoNaoHaCandidatos() {
            var cliente = umCliente().comIdade(22).build();

            var resultado = regra.aplicar(cliente, Set.of());

            assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("restringe o jovem independentemente da UF")
        void restringeJovemIndependenteDaUf() {
            var cliente = umCliente().comIdadeEmSp(22).build();

            var resultado = regra.aplicar(cliente, TODOS);

            assertThat(resultado).containsExactly(CARTAO_SEM_ANUIDADE);
        }
    }
}
