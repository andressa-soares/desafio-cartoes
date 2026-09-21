package br.com.desafio.cartoes.domain.model;

import java.math.BigDecimal;

public record CartaoOfertado(
        TipoCartao tipo,
        BigDecimal anuidadeMensal,
        BigDecimal limiteDisponivel,
        StatusCartao status
) {

    public static CartaoOfertado aprovado(Produto produto) {
        return new CartaoOfertado(
                produto.tipo(),
                produto.anuidadeMensal(),
                produto.limite(),
                StatusCartao.APROVADO
        );
    }
}