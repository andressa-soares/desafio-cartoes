package br.com.desafio.cartoes.domain.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

// Resultado do processamento de um pedido de cartão.

public record Solicitacao(
        UUID numero,
        LocalDateTime data,
        Cliente cliente,
        List<CartaoOfertado> cartoesOfertados
) {

    public Solicitacao {
        Objects.requireNonNull(numero, "Número é obrigatório.");
        Objects.requireNonNull(data, "Data é obrigatória.");
        Objects.requireNonNull(cliente, "Cliente é obrigatório.");
        cartoesOfertados = List.copyOf(Objects.requireNonNull(cartoesOfertados, "Cartões ofertados é obrigatório."));
    }

    public boolean possuiCartaoAprovado() {
        return !cartoesOfertados.isEmpty();
    }
}