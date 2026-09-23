package br.com.desafio.cartoes.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record SolicitacaoResponse(
        @JsonProperty("numero_solicitacao") UUID numeroSolicitacao,
        @JsonProperty("data_solicitacao")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
        LocalDateTime dataSolicitacao,
        ClienteDto cliente,
        @JsonProperty("cartoes_ofertados") List<CartaoOfertadoDto> cartoesOfertados
) {
}
