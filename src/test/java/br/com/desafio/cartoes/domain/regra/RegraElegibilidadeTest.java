package br.com.desafio.cartoes.domain.regra;

import br.com.desafio.cartoes.domain.model.TipoCartao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.Set;

import static br.com.desafio.cartoes.domain.model.TipoCartao.CARTAO_COM_CASHBACK;
import static br.com.desafio.cartoes.domain.model.TipoCartao.CARTAO_DE_PARCEIROS;
import static br.com.desafio.cartoes.domain.model.TipoCartao.CARTAO_SEM_ANUIDADE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("RegraElegibilidade")
class RegraElegibilidadeTest {

    private static final Set<TipoCartao> TODOS =
            Set.of(CARTAO_SEM_ANUIDADE, CARTAO_DE_PARCEIROS, CARTAO_COM_CASHBACK);

    @Nested
    @DisplayName("manterApenas")
    class ManterApenas {

        @Test
        @DisplayName("devolve a interseção entre candidatos e permitidos")
        void devolveIntersecao() {
            var resultado = RegraElegibilidade.manterApenas(
                    TODOS, Set.of(CARTAO_SEM_ANUIDADE, CARTAO_COM_CASHBACK));

            assertThat(resultado).containsExactlyInAnyOrder(CARTAO_SEM_ANUIDADE, CARTAO_COM_CASHBACK);
        }

        @Test
        @DisplayName("devolve vazio quando não há candidatos")
        void devolveVazioQuandoNaoHaCandidatos() {
            var resultado = RegraElegibilidade.manterApenas(Set.of(), Set.of(CARTAO_SEM_ANUIDADE));

            assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("devolve vazio quando nenhum candidato está entre os permitidos")
        void devolveVazioQuandoNenhumCandidatoEhPermitido() {
            var resultado = RegraElegibilidade.manterApenas(
                    Set.of(CARTAO_DE_PARCEIROS), Set.of(CARTAO_SEM_ANUIDADE));

            assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("não adiciona permitidos que não estavam entre os candidatos")
        void naoAdicionaPermitidoAusenteDosCandidatos() {
            var resultado = RegraElegibilidade.manterApenas(Set.of(CARTAO_SEM_ANUIDADE), TODOS);

            assertThat(resultado).containsExactly(CARTAO_SEM_ANUIDADE);
        }

        @Test
        @DisplayName("devolve um conjunto imutável")
        void devolveConjuntoImutavel() {
            var resultado = RegraElegibilidade.manterApenas(TODOS, Set.of(CARTAO_SEM_ANUIDADE));

            assertThatThrownBy(() -> resultado.add(CARTAO_DE_PARCEIROS))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("não altera o conjunto de candidatos recebido")
        void naoAlteraCandidatosRecebidos() {
            var candidatos = new LinkedHashSet<>(TODOS);

            RegraElegibilidade.manterApenas(candidatos, Set.of(CARTAO_SEM_ANUIDADE));

            assertThat(candidatos).containsExactlyInAnyOrderElementsOf(TODOS);
        }
    }
}
