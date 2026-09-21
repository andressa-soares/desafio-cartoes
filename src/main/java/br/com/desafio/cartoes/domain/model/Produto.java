package br.com.desafio.cartoes.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

// Os valores não são fixos no código, pois chegam via configuração.

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

    // Indica se a renda informada atinge a renda mínima do produto (inclusive).

    public boolean atendeRenda(BigDecimal rendaMensal) {
        return rendaMensal.compareTo(rendaMinima) >= 0;
    }
}