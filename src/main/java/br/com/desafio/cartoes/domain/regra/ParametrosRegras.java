package br.com.desafio.cartoes.domain.regra;

import br.com.desafio.cartoes.domain.model.FaixaEtaria;

import java.util.Objects;

/* Faixas etárias que as regras de perfil consultam. Vêm da configuração, o que mantém as
   regras livres de literais de negócio e permite alterar uma faixa sem tocar no código. */

public record ParametrosRegras(FaixaEtaria faixaJovem, FaixaEtaria faixaExcecaoResidenteSp) {

    public ParametrosRegras {
        Objects.requireNonNull(faixaJovem, "Faixa jovem é obrigatória.");
        Objects.requireNonNull(faixaExcecaoResidenteSp, "Faixa de exceção do residente em SP é obrigatória.");
    }
}
