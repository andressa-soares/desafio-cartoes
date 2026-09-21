package br.com.desafio.cartoes.domain.fixture;

import br.com.desafio.cartoes.domain.model.Cliente;

import java.math.BigDecimal;
import java.time.LocalDate;

// Os valores padrão são arbitrários e sem significado de negócio.

public final class ClienteFixture {

    private String nome = "Fulano de Tal";
    private String cpf = "12345678901";
    private int idade = 40;
    private LocalDate dataNascimento = LocalDate.of(1986, 3, 15);
    private String uf = "BA";
    private BigDecimal rendaMensal = new BigDecimal("1000.00");
    private String email = "fulano@exemplo.com";
    private String telefoneWhatsapp = "71999999999";

    private ClienteFixture() {
    }

    public static ClienteFixture umCliente() {
        return new ClienteFixture();
    }

    public ClienteFixture comNome(String nome) {
        this.nome = nome;
        return this;
    }

    public ClienteFixture comCpf(String cpf) {
        this.cpf = cpf;
        return this;
    }

    public ClienteFixture comIdade(int idade) {
        this.idade = idade;
        return this;
    }

    public ClienteFixture comDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
        return this;
    }

    public ClienteFixture comUf(String uf) {
        this.uf = uf;
        return this;
    }

    public ClienteFixture comRendaMensal(String rendaMensal) {
        this.rendaMensal = rendaMensal == null ? null : new BigDecimal(rendaMensal);
        return this;
    }

    public ClienteFixture comEmail(String email) {
        this.email = email;
        return this;
    }

    public ClienteFixture comTelefoneWhatsapp(String telefoneWhatsapp) {
        this.telefoneWhatsapp = telefoneWhatsapp;
        return this;
    }

    public Cliente build() {
        return new Cliente(nome, cpf, idade, dataNascimento, uf, rendaMensal, email, telefoneWhatsapp);
    }
}
