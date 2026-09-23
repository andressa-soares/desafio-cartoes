package br.com.desafio.cartoes.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

public record Produto(
        TipoCartao tipo,
        BigDecimal rendaMinima,
        BigDecimal limite,
        BigDecimal anuidadeMensal
) {

    public Produto {
        Objects.requireNonNull(tipo, "Tipo é obrigatório.");
        Objects.requireNonNull(rendaMinima, "Renda mínima é obrigatória.");
        Objects.requireNonNull(limite, "Limite é obrigatório.");
        Objects.requireNonNull(anuidadeMensal, "Anuidade mensal é obrigatória.");
        if (ehNegativo(rendaMinima) || ehNegativo(limite) || ehNegativo(anuidadeMensal)) {
            throw new IllegalArgumentException("Valores do produto não podem ser negativos.");
        }
    }

    private static boolean ehNegativo(BigDecimal valor) {
        return valor.compareTo(BigDecimal.ZERO) < 0;
    }

    // rendaMinima é inclusiva: renda igual já atende.
    public boolean atendeRenda(BigDecimal rendaMensal) {
        return rendaMensal.compareTo(rendaMinima) >= 0;
    }
}