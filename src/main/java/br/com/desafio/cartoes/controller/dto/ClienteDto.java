package br.com.desafio.cartoes.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;

// As mensagens não repetem o nome do campo: o GlobalExceptionHandler já o
// prefixa ("campo: mensagem") ao montar mensagem_interna.
public record ClienteDto(

        @NotBlank(message = "Não pode estar em branco.")
        String nome,

        @NotBlank(message = "Não pode estar em branco.")
        String cpf,

        @NotNull(message = "É obrigatória.")
        Integer idade,

        @JsonProperty("data_nascimento")
        @NotNull(message = "É obrigatória.")
        LocalDate dataNascimento,

        @NotBlank(message = "Não pode estar em branco.")
        String uf,

        @JsonProperty("renda_mensal")
        @NotNull(message = "É obrigatória.")
        @PositiveOrZero(message = "Não pode ser negativa.")
        BigDecimal rendaMensal,

        @NotBlank(message = "Não pode estar em branco.")
        @Email(message = "Deve ter um formato válido.")
        String email,

        @JsonProperty("telefone_whatsapp")
        @NotBlank(message = "Não pode estar em branco.")
        String telefoneWhatsapp
) {
}
