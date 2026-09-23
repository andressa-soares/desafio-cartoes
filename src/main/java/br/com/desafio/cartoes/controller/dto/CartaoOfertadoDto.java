package br.com.desafio.cartoes.controller.dto;

import br.com.desafio.cartoes.domain.model.StatusCartao;
import br.com.desafio.cartoes.domain.model.TipoCartao;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record CartaoOfertadoDto(
        @JsonProperty("tipo_cartao") TipoCartao tipoCartao,
        @JsonProperty("valor_anuidade_mensal") BigDecimal valorAnuidadeMensal,
        @JsonProperty("valor_limite_disponivel") BigDecimal valorLimiteDisponivel,
        StatusCartao status
) {
}
