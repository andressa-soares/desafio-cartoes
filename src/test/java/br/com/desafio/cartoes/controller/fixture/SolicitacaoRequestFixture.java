package br.com.desafio.cartoes.controller.fixture;

import br.com.desafio.cartoes.controller.dto.ClienteDto;
import br.com.desafio.cartoes.controller.dto.SolicitacaoRequest;

import java.math.BigDecimal;
import java.time.LocalDate;

/*
 * Constrói SolicitacaoRequest para os testes de controller. comIdadeEfetiva usa o
 * mesmo ano do relógio fixo (ClockFixture), para que a idade informada bata com
 * a idade que o mapper calcula a partir da data de nascimento.
 */
public final class SolicitacaoRequestFixture {

    private static final int ANO_DO_RELOGIO_FIXO = 2026;

    private String nome = "Cliente Teste";
    private String cpf = "12345678910";
    private Integer idade = 40;
    private LocalDate dataNascimento = LocalDate.of(1986, 1, 1);
    private String uf = "BA";
    private BigDecimal rendaMensal = new BigDecimal("10000.00");
    private String email = "cliente@teste.com";
    private String telefoneWhatsapp = "11999999999";

    private SolicitacaoRequestFixture() {
    }

    public static SolicitacaoRequestFixture umaSolicitacao() {
        return new SolicitacaoRequestFixture();
    }

    public SolicitacaoRequestFixture comIdade(Integer idade) {
        this.idade = idade;
        return this;
    }

    public SolicitacaoRequestFixture comDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
        return this;
    }

    // Fixa idade e data de nascimento juntas, coerentes com o relógio fixo dos testes.
    public SolicitacaoRequestFixture comIdadeEfetiva(int idade) {
        this.idade = idade;
        this.dataNascimento = LocalDate.of(ANO_DO_RELOGIO_FIXO - idade, 1, 1);
        return this;
    }

    public SolicitacaoRequestFixture comUf(String uf) {
        this.uf = uf;
        return this;
    }

    public SolicitacaoRequestFixture comRendaMensal(String rendaMensal) {
        this.rendaMensal = new BigDecimal(rendaMensal);
        return this;
    }

    public ClienteDto clienteDto() {
        return new ClienteDto(nome, cpf, idade, dataNascimento, uf, rendaMensal, email, telefoneWhatsapp);
    }

    public SolicitacaoRequest build() {
        return new SolicitacaoRequest(clienteDto());
    }
}
