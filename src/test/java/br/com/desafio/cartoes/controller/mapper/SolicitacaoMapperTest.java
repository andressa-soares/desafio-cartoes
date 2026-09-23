package br.com.desafio.cartoes.controller.mapper;

import br.com.desafio.cartoes.controller.dto.CartaoOfertadoDto;
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

import static br.com.desafio.cartoes.controller.fixture.SolicitacaoRequestFixture.umaSolicitacao;
import static br.com.desafio.cartoes.domain.fixture.ClienteFixture.umCliente;
import static br.com.desafio.cartoes.domain.fixture.ProdutoFixture.umProduto;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SolicitacaoMapper")
class SolicitacaoMapperTest {

    private final Clock relogioFixo = ClockFixture.fixo();
    private final SolicitacaoMapper mapper = new SolicitacaoMapper(relogioFixo);

    @Nested
    @DisplayName("toCliente")
    class ToCliente {

        @Test
        @DisplayName("calcula a idade a partir da data de nascimento")
        void calculaIdadeAPartirDaDataDeNascimento() {
            var dto = umaSolicitacao().comDataNascimento(LocalDate.of(1990, 1, 1)).clienteDto();

            var cliente = mapper.toCliente(dto);

            assertThat(cliente.idade()).isEqualTo(36);
        }

        @Test
        @DisplayName("idade já está completa quando o aniversário é hoje")
        void idadeJaCompletaQuandoAniversarioEhHoje() {
            var dataNascimento = ClockFixture.DATA_HORA_FIXA.toLocalDate().minusYears(30);
            var dto = umaSolicitacao().comDataNascimento(dataNascimento).clienteDto();

            var cliente = mapper.toCliente(dto);

            assertThat(cliente.idade()).isEqualTo(30);
        }

        @Test
        @DisplayName("idade ainda não está completa quando o aniversário é amanhã")
        void idadeAindaNaoCompletaQuandoAniversarioEhAmanha() {
            var dataNascimento = ClockFixture.DATA_HORA_FIXA.toLocalDate().minusYears(30).plusDays(1);
            var dto = umaSolicitacao().comDataNascimento(dataNascimento).clienteDto();

            var cliente = mapper.toCliente(dto);

            assertThat(cliente.idade()).isEqualTo(29);
        }

        @Test
        @DisplayName("calcula a idade correta para quem nasceu em 29 de fevereiro, em ano não bissexto")
        void calculaIdadeParaNascidoEm29DeFevereiro() {
            var dto = umaSolicitacao().comDataNascimento(LocalDate.of(2000, 2, 29)).clienteDto();

            var cliente = mapper.toCliente(dto);

            assertThat(cliente.idade()).isEqualTo(26);
        }

        @Test
        @DisplayName("ignora o campo idade do DTO quando diverge da data de nascimento")
        void ignoraCampoIdadeDivergenteDaDataDeNascimento() {
            var dto = umaSolicitacao().comIdade(99).comDataNascimento(LocalDate.of(1990, 1, 1)).clienteDto();

            var cliente = mapper.toCliente(dto);

            assertThat(cliente.idade()).isEqualTo(36);
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
