package br.com.desafio.cartoes.domain.regra;

import br.com.desafio.cartoes.domain.model.FaixaEtaria;

import java.util.Objects;

// Vêm da configuração: nenhuma faixa etária é literal nas regras que as consultam.
public record ParametrosRegras(FaixaEtaria faixaJovem, FaixaEtaria faixaExcecaoResidenteSp) {

    public ParametrosRegras {
        Objects.requireNonNull(faixaJovem, "Faixa jovem é obrigatória.");
        Objects.requireNonNull(faixaExcecaoResidenteSp, "Faixa de exceção do residente em SP é obrigatória.");
    }
}
