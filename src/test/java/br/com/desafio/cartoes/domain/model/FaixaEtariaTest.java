package br.com.desafio.cartoes.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

@DisplayName("FaixaEtaria")
class FaixaEtariaTest {

    @Nested
    @DisplayName("construtor")
    class Construtor {

        @Test
        @DisplayName("mantém os limites informados")
        void mantemOsLimitesInformados() {
            var faixa = new FaixaEtaria(18, 24);

            assertThat(faixa.inicio()).isEqualTo(18);
            assertThat(faixa.fimInclusivo()).isEqualTo(24);
        }

        @Test
        @DisplayName("aceita faixa de um único valor")
        void aceitaFaixaDeUmUnicoValor() {
            assertThatCode(() -> new FaixaEtaria(30, 30)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("aceita início igual a zero")
        void aceitaInicioIgualAZero() {
            assertThatCode(() -> new FaixaEtaria(0, 17)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("rejeita início negativo")
        void rejeitaInicioNegativo() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new FaixaEtaria(-1, 24))
                    .withMessage("Início da faixa etária não pode ser negativo.");
        }

        @Test
        @DisplayName("rejeita fim menor que o início")
        void rejeitaFimMenorQueInicio() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new FaixaEtaria(25, 24))
                    .withMessage("Fim da faixa etária não pode ser menor que o início.");
        }
    }

    @Nested
    @DisplayName("contem")
    class Contem {

        private final FaixaEtaria faixa = new FaixaEtaria(18, 24);

        @Test
        @DisplayName("contém a idade exatamente no início da faixa")
        void contemInicioDaFaixa() {
            var resultado = faixa.contem(18);

            assertThat(resultado).isTrue();
        }

        @Test
        @DisplayName("contém a idade exatamente no fim da faixa")
        void contemFimDaFaixa() {
            var resultado = faixa.contem(24);

            assertThat(resultado).isTrue();
        }

        @ParameterizedTest(name = "idade {0}")
        @ValueSource(ints = {19, 21, 23})
        @DisplayName("contém as idades no interior da faixa")
        void contemIdadesNoInteriorDaFaixa(int idade) {
            var resultado = faixa.contem(idade);

            assertThat(resultado).isTrue();
        }

        @Test
        @DisplayName("não contém a idade um ano abaixo do início")
        void naoContemIdadeAbaixoDoInicio() {
            var resultado = faixa.contem(17);

            assertThat(resultado).isFalse();
        }

        @Test
        @DisplayName("não contém a idade um ano acima do fim")
        void naoContemIdadeAcimaDoFim() {
            var resultado = faixa.contem(25);

            assertThat(resultado).isFalse();
        }

        @Test
        @DisplayName("contém apenas o próprio valor quando início e fim coincidem")
        void contemApenasOProprioValorNaFaixaDeUmValor() {
            var faixaDeUmValor = new FaixaEtaria(30, 30);

            assertThat(faixaDeUmValor.contem(30)).isTrue();
            assertThat(faixaDeUmValor.contem(29)).isFalse();
            assertThat(faixaDeUmValor.contem(31)).isFalse();
        }
    }
}
