package br.com.desafio.cartoes.domain.regra;

import br.com.desafio.cartoes.domain.model.TipoCartao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Set;

import static br.com.desafio.cartoes.domain.fixture.ClienteFixture.umCliente;
import static br.com.desafio.cartoes.domain.fixture.ParametrosRegrasFixture.padrao;
import static br.com.desafio.cartoes.domain.model.TipoCartao.CARTAO_COM_CASHBACK;
import static br.com.desafio.cartoes.domain.model.TipoCartao.CARTAO_DE_PARCEIROS;
import static br.com.desafio.cartoes.domain.model.TipoCartao.CARTAO_SEM_ANUIDADE;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Combinação das regras de perfil")
class CombinacaoRegrasTest {

    private static final Set<TipoCartao> TODOS =
            Set.of(CARTAO_SEM_ANUIDADE, CARTAO_DE_PARCEIROS, CARTAO_COM_CASHBACK);

    private final RegraJovem regraJovem = new RegraJovem(padrao());
    private final RegraResidenteSp regraResidenteSp = new RegraResidenteSp(padrao());

    @Nested
    @DisplayName("aplicação em pipeline")
    class AplicacaoEmPipeline {

        @Test
        @DisplayName("mantém apenas o cartão sem anuidade para um jovem de 22 anos em SP")
        void mantemApenasSemAnuidadeParaJovemEmSp() {
            var cliente = umCliente().comIdadeEmSp(22).build();

            var resultado = regraResidenteSp.aplicar(cliente, regraJovem.aplicar(cliente, TODOS));

            assertThat(resultado).containsExactly(CARTAO_SEM_ANUIDADE);
        }

        @Test
        @DisplayName("mantém os três cartões para quem tem 27 anos e mora em SP")
        void mantemOsTresCartoesAos27AnosEmSp() {
            var cliente = umCliente().comIdadeEmSp(27).build();

            var resultado = regraResidenteSp.aplicar(cliente, regraJovem.aplicar(cliente, TODOS));

            assertThat(resultado).containsExactlyInAnyOrderElementsOf(TODOS);
        }

        @ParameterizedTest(name = "idade {0} em {1}")
        @CsvSource({
                "18,SP", "22,SP", "25,SP", "29,SP", "30,SP", "32,SP",
                "18,RJ", "22,RJ", "25,RJ", "29,RJ", "30,RJ", "32,RJ"
        })
        @DisplayName("produz o mesmo resultado em qualquer ordem de aplicação das regras")
        void produzMesmoResultadoEmQualquerOrdem(int idade, String uf) {
            var cliente = umCliente().comIdade(idade).comUf(uf).build();

            var jovemPrimeiro = regraResidenteSp.aplicar(cliente, regraJovem.aplicar(cliente, TODOS));
            var spPrimeiro = regraJovem.aplicar(cliente, regraResidenteSp.aplicar(cliente, TODOS));

            assertThat(spPrimeiro).containsExactlyInAnyOrderElementsOf(jovemPrimeiro);
        }
    }
}
