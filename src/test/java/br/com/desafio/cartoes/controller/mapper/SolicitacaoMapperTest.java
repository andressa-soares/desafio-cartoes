package br.com.desafio.cartoes.controller.mapper;

import br.com.desafio.cartoes.config.CartoesProperties;
import br.com.desafio.cartoes.controller.dto.CartaoOfertadoDto;
import br.com.desafio.cartoes.controller.exception.RequisicaoInvalidaException;
import br.com.desafio.cartoes.domain.model.CartaoOfertado;
import br.com.desafio.cartoes.domain.model.Solicitacao;
import br.com.desafio.cartoes.domain.model.StatusCartao;
import br.com.desafio.cartoes.domain.model.TipoCartao;
import br.com.desafio.cartoes.fixture.ClockFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static br.com.desafio.cartoes.config.fixture.CartoesPropertiesFixture.umasPropriedades;
import static br.com.desafio.cartoes.controller.fixture.SolicitacaoRequestFixture.umaSolicitacao;
import static br.com.desafio.cartoes.domain.fixture.ClienteFixture.umCliente;
import static br.com.desafio.cartoes.domain.fixture.ProdutoFixture.umProduto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

@DisplayName("SolicitacaoMapper")
class SolicitacaoMapperTest {

    private static final LocalDate HOJE = ClockFixture.DATA_HORA_FIXA.toLocalDate();

    private final Clock relogioFixo = ClockFixture.fixo();
    private final CartoesProperties propriedades = umasPropriedades().comIdadeMinima(18).build();
    private final SolicitacaoMapper mapper = new SolicitacaoMapper(relogioFixo, propriedades);

    @Nested
    @DisplayName("toCliente")
    class ToCliente {

        @Test
        @DisplayName("calcula a idade a partir da data de nascimento")
        void calculaIdadeAPartirDaDataDeNascimento() {
            var dto = umaSolicitacao().comDataNascimento(LocalDate.of(1990, 1, 1)).comIdade(36).clienteDto();

            var cliente = mapper.toCliente(dto);

            assertThat(cliente.idade()).isEqualTo(36);
        }

        @Test
        @DisplayName("idade já está completa quando o aniversário é hoje")
        void idadeJaCompletaQuandoAniversarioEhHoje() {
            var dataNascimento = HOJE.minusYears(30);
            var dto = umaSolicitacao().comDataNascimento(dataNascimento).comIdade(30).clienteDto();

            var cliente = mapper.toCliente(dto);

            assertThat(cliente.idade()).isEqualTo(30);
        }

        @Test
        @DisplayName("idade ainda não está completa quando o aniversário é amanhã")
        void idadeAindaNaoCompletaQuandoAniversarioEhAmanha() {
            var dataNascimento = HOJE.minusYears(30).plusDays(1);
            var dto = umaSolicitacao().comDataNascimento(dataNascimento).comIdade(29).clienteDto();

            var cliente = mapper.toCliente(dto);

            assertThat(cliente.idade()).isEqualTo(29);
        }

        @Test
        @DisplayName("calcula a idade correta para quem nasceu em 29 de fevereiro, em ano não bissexto")
        void calculaIdadeParaNascidoEm29DeFevereiro() {
            var dto = umaSolicitacao().comDataNascimento(LocalDate.of(2000, 2, 29)).comIdade(26).clienteDto();

            var cliente = mapper.toCliente(dto);

            assertThat(cliente.idade()).isEqualTo(26);
        }
    }

    @Nested
    @DisplayName("toCliente: idade mínima e coerência")
    class IdadeMinimaECoerencia {

        @Test
        @DisplayName("cliente com 17 anos lança RequisicaoInvalidaException citando data_nascimento")
        void clienteCom17AnosLancaExcecaoCitandoDataNascimento() {
            var dto = umaSolicitacao().comDataNascimento(HOJE.minusYears(17)).comIdade(17).clienteDto();

            assertThatExceptionOfType(RequisicaoInvalidaException.class)
                    .isThrownBy(() -> mapper.toCliente(dto))
                    .withMessageContaining("data_nascimento");
        }

        @Test
        @DisplayName("cliente que completa 18 anos hoje é aceito")
        void clienteQueCompleta18AnosHojeEhAceito() {
            var dto = umaSolicitacao().comDataNascimento(HOJE.minusYears(18)).comIdade(18).clienteDto();

            var cliente = mapper.toCliente(dto);

            assertThat(cliente.idade()).isEqualTo(18);
        }

        @Test
        @DisplayName("cliente que completa 18 anos amanhã é rejeitado")
        void clienteQueCompleta18AnosAmanhaEhRejeitado() {
            var dto = umaSolicitacao().comDataNascimento(HOJE.minusYears(18).plusDays(1)).comIdade(17).clienteDto();

            assertThatExceptionOfType(RequisicaoInvalidaException.class)
                    .isThrownBy(() -> mapper.toCliente(dto))
                    .withMessageContaining("data_nascimento");
        }

        @Test
        @DisplayName("idade informada divergente da calculada lança exceção citando idade")
        void idadeDivergenteLancaExcecaoCitandoIdade() {
            var dto = umaSolicitacao().comDataNascimento(LocalDate.of(1990, 1, 1)).comIdade(99).clienteDto();

            assertThatExceptionOfType(RequisicaoInvalidaException.class)
                    .isThrownBy(() -> mapper.toCliente(dto))
                    .withMessageContaining("idade");
        }

        @Test
        @DisplayName("idade mínima configurada como 21 rejeita cliente de 20 anos")
        void idadeMinima21RejeitaClienteDe20Anos() {
            var mapper21 = new SolicitacaoMapper(relogioFixo, umasPropriedades().comIdadeMinima(21).build());
            var dto = umaSolicitacao().comDataNascimento(HOJE.minusYears(20)).comIdade(20).clienteDto();

            assertThatExceptionOfType(RequisicaoInvalidaException.class)
                    .isThrownBy(() -> mapper21.toCliente(dto));
        }

        @Test
        @DisplayName("idade mínima configurada como 21 aceita cliente de 21 anos")
        void idadeMinima21AceitaClienteDe21Anos() {
            var mapper21 = new SolicitacaoMapper(relogioFixo, umasPropriedades().comIdadeMinima(21).build());
            var dto = umaSolicitacao().comDataNascimento(HOJE.minusYears(21)).comIdade(21).clienteDto();

            var cliente = mapper21.toCliente(dto);

            assertThat(cliente.idade()).isEqualTo(21);
        }

        @Test
        @DisplayName("mensagens não contêm a data de nascimento nem a idade informada")
        void mensagensNaoContemDadosDoCliente() {
            var dataNascimento = HOJE.minusYears(17);
            var dtoMenorDeIdade = umaSolicitacao().comDataNascimento(dataNascimento).comIdade(17).clienteDto();
            var dtoIdadeDivergente = umaSolicitacao().comDataNascimento(LocalDate.of(1990, 1, 1)).comIdade(37).clienteDto();

            var excecaoIdadeMinima = catchThrowableOfType(
                    RequisicaoInvalidaException.class, () -> mapper.toCliente(dtoMenorDeIdade));
            var excecaoIdadeDivergente = catchThrowableOfType(
                    RequisicaoInvalidaException.class, () -> mapper.toCliente(dtoIdadeDivergente));

            assertThat(excecaoIdadeMinima.getMessage()).doesNotContain(dataNascimento.toString());
            assertThat(excecaoIdadeDivergente.getMessage()).doesNotContain("37");
        }
    }

    @Nested
    @DisplayName("toResponse")
    class ToResponse {

        @Test
        @DisplayName("formata os valores monetários em escala 2")
        void formataValoresMonetariosEmEscala2() {
            var cliente = umCliente().comRendaMensal("4000").build();
            var cartao = new CartaoOfertado(
                    TipoCartao.CARTAO_SEM_ANUIDADE, new BigDecimal("0"), new BigDecimal("1000"), StatusCartao.APROVADO);
            var solicitacao = new Solicitacao(UUID.randomUUID(), ClockFixture.DATA_HORA_FIXA, cliente, List.of(cartao));

            var resposta = mapper.toResponse(solicitacao);

            assertThat(resposta.cliente().rendaMensal()).isEqualByComparingTo("4000.00");
            assertThat(resposta.cliente().rendaMensal().scale()).isEqualTo(2);
            assertThat(resposta.cartoesOfertados().get(0).valorAnuidadeMensal()).isEqualByComparingTo("0.00");
            assertThat(resposta.cartoesOfertados().get(0).valorAnuidadeMensal().scale()).isEqualTo(2);
            assertThat(resposta.cartoesOfertados().get(0).valorLimiteDisponivel()).isEqualByComparingTo("1000.00");
            assertThat(resposta.cartoesOfertados().get(0).valorLimiteDisponivel().scale()).isEqualTo(2);
        }

        @Test
        @DisplayName("preserva todos os campos do cliente e a ordem dos cartões")
        void preservaCamposDoClienteEOrdemDosCartoes() {
            var cliente = umCliente().build();
            var cartaoUm = CartaoOfertado.aprovado(umProduto().doTipo(TipoCartao.CARTAO_COM_CASHBACK).build());
            var cartaoDois = CartaoOfertado.aprovado(umProduto().doTipo(TipoCartao.CARTAO_SEM_ANUIDADE).build());
            var solicitacao = new Solicitacao(
                    UUID.randomUUID(), ClockFixture.DATA_HORA_FIXA, cliente, List.of(cartaoUm, cartaoDois));

            var resposta = mapper.toResponse(solicitacao);

            assertThat(resposta.cliente().nome()).isEqualTo(cliente.nome());
            assertThat(resposta.cliente().cpf()).isEqualTo(cliente.cpf());
            assertThat(resposta.cliente().idade()).isEqualTo(cliente.idade());
            assertThat(resposta.cliente().dataNascimento()).isEqualTo(cliente.dataNascimento());
            assertThat(resposta.cliente().uf()).isEqualTo(cliente.uf());
            assertThat(resposta.cliente().email()).isEqualTo(cliente.email());
            assertThat(resposta.cliente().telefoneWhatsapp()).isEqualTo(cliente.telefoneWhatsapp());
            assertThat(resposta.cartoesOfertados())
                    .extracting(CartaoOfertadoDto::tipoCartao)
                    .containsExactly(TipoCartao.CARTAO_COM_CASHBACK, TipoCartao.CARTAO_SEM_ANUIDADE);
        }
    }
}
