package br.com.desafio.cartoes.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

// A 'idade' é calculada a partir de 'dataNascimento'.

public record Cliente(
        String nome,
        String cpf,
        int idade,
        LocalDate dataNascimento,
        String uf,
        BigDecimal rendaMensal,
        String email,
        String telefoneWhatsapp
) {

    public Cliente {
        Objects.requireNonNull(nome, "Nome é obrigatório.");
        Objects.requireNonNull(cpf, "CPF é obrigatório.");
        Objects.requireNonNull(dataNascimento, "Data de nascimento é obrigatória.");
        Objects.requireNonNull(uf, "UF é obrigatória.");
        Objects.requireNonNull(rendaMensal, "Renda mensal é obrigatória.");
        Objects.requireNonNull(email, "E-mail é obrigatório.");
        Objects.requireNonNull(telefoneWhatsapp, "Telefone WhatsApp é obrigatório.");
        if (idade < 0) {
            throw new IllegalArgumentException("Idade não pode ser negativa.");
        }
        if (rendaMensal.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Renda mensal não pode ser negativa.");
        }
    }

    public boolean resideEm(String uf) {
        return this.uf.equalsIgnoreCase(uf);
    }

    public boolean temIdadeEntre(int inicio, int fimInclusivo) {
        return idade >= inicio && idade <= fimInclusivo;
    }
}