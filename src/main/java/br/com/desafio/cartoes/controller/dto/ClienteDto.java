package br.com.desafio.cartoes.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ClienteDto(
        String nome,
        String cpf,
        Integer idade,
        @JsonProperty("data_nascimento") LocalDate dataNascimento,
        String uf,
        @JsonProperty("renda_mensal") BigDecimal rendaMensal,
        String email,
        @JsonProperty("telefone_whatsapp") String telefoneWhatsapp
) {
}
