package br.com.desafio.cartoes.controller.dto;

import br.com.desafio.cartoes.domain.exception.TipoErro;
import com.fasterxml.jackson.annotation.JsonProperty;

public record DetalheErro(
        String app,
        @JsonProperty("tipo_erro") TipoErro tipoErro,
        @JsonProperty("mensagem_interna") String mensagemInterna
) {
}
