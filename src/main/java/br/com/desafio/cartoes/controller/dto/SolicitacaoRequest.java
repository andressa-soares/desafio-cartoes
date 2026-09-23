package br.com.desafio.cartoes.controller.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record SolicitacaoRequest(
        @NotNull(message = "É obrigatório.")
        @Valid
        ClienteDto cliente
) {
}
