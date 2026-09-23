package br.com.desafio.cartoes.controller.dto;

import br.com.desafio.cartoes.domain.exception.TipoErro;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.HttpStatusCode;

public record ErroResponse(
        String codigo,
        String mensagem,
        @JsonProperty("detalhe_erro") DetalheErro detalheErro
) {

    public static ErroResponse de(HttpStatusCode status, String mensagem, String app,
                                   TipoErro tipoErro, String mensagemInterna) {
        return new ErroResponse(
                String.valueOf(status.value()),
                mensagem,
                new DetalheErro(app, tipoErro, mensagemInterna)
        );
    }
}
