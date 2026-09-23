package br.com.desafio.cartoes.service;

import br.com.desafio.cartoes.domain.exception.RegraNegocioException;
import br.com.desafio.cartoes.domain.exception.TipoErro;
import br.com.desafio.cartoes.domain.model.CartaoOfertado;
import br.com.desafio.cartoes.domain.model.StatusCartao;
import br.com.desafio.cartoes.domain.model.TipoCartao;
import br.com.desafio.cartoes.domain.regra.FiltroRenda;
import br.com.desafio.cartoes.domain.regra.RegraElegibilidade;
import br.com.desafio.cartoes.domain.regra.RegraJovem;
import br.com.desafio.cartoes.domain.regra.RegraResidenteSp;
import br.com.desafio.cartoes.fixture.ClockFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

import static br.com.desafio.cartoes.domain.fixture.ClienteFixture.umCliente;
import static br.com.desafio.cartoes.domain.fixture.ParametrosRegrasFixture.padrao;
import static br.com.desafio.cartoes.domain.fixture.ProdutoFixture.catalogo;
import static br.com.desafio.cartoes.fixture.ClockFixture.DATA_HORA_FIXA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("SolicitacaoCartaoService")
class SolicitacaoCartaoServiceTest {

    private final FiltroRenda filtroRenda = new FiltroRenda(catalogo());
    private final Clock relogioFixo = ClockFixture.fixo();

    @Nested
    @DisplayName("solicitar")
    class Solicitar {

        @Test
        @DisplayName("lança RegraNegocioException com tipo RENDA_INSUFICIENTE quando a renda fica abaixo de todos os mínimos")
        void lancaExcecaoQuandoRendaAbaixoDeTodosOsMinimos() {
            var service = new SolicitacaoCartaoService(filtroRenda, List.of(), relogioFixo);
            var cliente = umCliente().comRendaMensal("0.01").build();

            assertThatExceptionOfType(RegraNegocioException.class)
                    .isThrownBy(() -> service.solicitar(cliente))
                    .satisfies(excecao -> assertThat(excecao.tipoErro()).isEqualTo(TipoErro.RENDA_INSUFICIENTE));
        }

        @Test
        @DisplayName("devolve os três cartões na ordem do catálogo quando a renda é alta e não há regras restritivas")
        void devolveTresCartoesNaOrdemDoCatalogoSemRegrasRestritivas() {
            var service = new SolicitacaoCartaoService(filtroRenda, List.of(), relogioFixo);
            var cliente = umCliente().comRendaMensal("5000.00").build();

            var solicitacao = service.solicitar(cliente);

            assertThat(solicitacao.cartoesOfertados())
                    .extracting(CartaoOfertado::tipo)
                    .containsExactly(TipoCartao.CARTAO_SEM_ANUIDADE, TipoCartao.CARTAO_DE_PARCEIROS, TipoCartao.CARTAO_COM_CASHBACK);
        }

        @Test
        @DisplayName("aplica as regras reais: 32 anos em SP com renda 8000 recebe sem anuidade e cashback")
        void aplicaRegrasReaisParaClienteDe32AnosEmSp() {
            List<RegraElegibilidade> regras = List.of(new RegraJovem(padrao()), new RegraResidenteSp(padrao()));
            var service = new SolicitacaoCartaoService(filtroRenda, regras, relogioFixo);
            var cliente = umCliente().comIdadeEmSp(32).comRendaMensal("8000.00").build();

            var solicitacao = service.solicitar(cliente);

            assertThat(solicitacao.cartoesOfertados())
                    .extracting(CartaoOfertado::tipo)
                    .containsExactlyInAnyOrder(TipoCartao.CARTAO_SEM_ANUIDADE, TipoCartao.CARTAO_COM_CASHBACK);
        }

        @Test
        @DisplayName("devolve solicitação sem cartões quando uma regra fictícia zera os candidatos")
        void devolveSolicitacaoSemCartoesComRegraQueZeraCandidatos() {
            RegraElegibilidade regraQueZera = (cliente, candidatos) -> Set.of();
            var service = new SolicitacaoCartaoService(filtroRenda, List.of(regraQueZera), relogioFixo);
            var cliente = umCliente().comRendaMensal("5000.00").build();

            var solicitacao = service.solicitar(cliente);

            assertThat(solicitacao.possuiCartaoAprovado()).isFalse();
            assertThat(solicitacao.cartoesOfertados()).isEmpty();
        }

        @Test
        @DisplayName("invoca todas as regras da lista")
        void invocaTodasAsRegrasDaLista() {
            RegraElegibilidade regraUm = mock(RegraElegibilidade.class);
            RegraElegibilidade regraDois = mock(RegraElegibilidade.class);
            when(regraUm.aplicar(any(), any())).thenAnswer(chamada -> chamada.getArgument(1));
            when(regraDois.aplicar(any(), any())).thenAnswer(chamada -> chamada.getArgument(1));
            var service = new SolicitacaoCartaoService(filtroRenda, List.of(regraUm, regraDois), relogioFixo);
            var cliente = umCliente().comRendaMensal("5000.00").build();

            service.solicitar(cliente);

            verify(regraUm).aplicar(eq(cliente), any());
            verify(regraDois).aplicar(eq(cliente), any());
        }

        @Test
        @DisplayName("sem regras na lista, o resultado é igual ao do filtro de renda")
        void semRegrasResultadoIgualAoFiltroDeRenda() {
            var service = new SolicitacaoCartaoService(filtroRenda, List.of(), relogioFixo);
            var cliente = umCliente().comRendaMensal("2500.00").build();

            var solicitacao = service.solicitar(cliente);
            var produtosPelaRenda = filtroRenda.aplicar(cliente);

            assertThat(solicitacao.cartoesOfertados())
                    .extracting(CartaoOfertado::tipo)
                    .containsExactlyElementsOf(produtosPelaRenda.stream().map(produto -> produto.tipo()).toList());
        }

        @Test
        @DisplayName("data da solicitação usa o relógio injetado, truncada em milissegundos")
        void dataDaSolicitacaoUsaRelogioTruncadoEmMilissegundos() {
            Instant instanteComNanos = DATA_HORA_FIXA.atZone(ZoneId.systemDefault()).toInstant().plusNanos(456_789);
            Clock relogioComNanos = Clock.fixed(instanteComNanos, ZoneId.systemDefault());
            var service = new SolicitacaoCartaoService(filtroRenda, List.of(), relogioComNanos);
            var cliente = umCliente().comRendaMensal("5000.00").build();

            var solicitacao = service.solicitar(cliente);

            assertThat(solicitacao.data()).isEqualTo(DATA_HORA_FIXA);
        }

        @Test
        @DisplayName("preenche o número da solicitação e gera valores diferentes entre chamadas")
        void preencheNumeroDaSolicitacaoDiferenteEntreChamadas() {
            var service = new SolicitacaoCartaoService(filtroRenda, List.of(), relogioFixo);
            var cliente = umCliente().comRendaMensal("5000.00").build();

            var primeira = service.solicitar(cliente);
            var segunda = service.solicitar(cliente);

            assertThat(primeira.numero()).isNotNull();
            assertThat(segunda.numero()).isNotNull();
            assertThat(primeira.numero()).isNotEqualTo(segunda.numero());
        }

        @Test
        @DisplayName("cartões carregam limite, anuidade e status APROVADO do produto de origem")
        void cartoesCarregamLimiteAnuidadeEStatusDoProduto() {
            var service = new SolicitacaoCartaoService(filtroRenda, List.of(), relogioFixo);
            var cliente = umCliente().comRendaMensal("5000.00").build();

            var solicitacao = service.solicitar(cliente);
            var produtos = filtroRenda.aplicar(cliente);

            for (int i = 0; i < produtos.size(); i++) {
                var produto = produtos.get(i);
                var cartao = solicitacao.cartoesOfertados().get(i);
                assertThat(cartao.limiteDisponivel()).isEqualByComparingTo(produto.limite());
                assertThat(cartao.anuidadeMensal()).isEqualByComparingTo(produto.anuidadeMensal());
                assertThat(cartao.status()).isEqualTo(StatusCartao.APROVADO);
            }
        }
    }
}
