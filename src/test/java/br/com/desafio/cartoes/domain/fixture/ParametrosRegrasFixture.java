package br.com.desafio.cartoes.domain.fixture;

import br.com.desafio.cartoes.domain.model.FaixaEtaria;
import br.com.desafio.cartoes.domain.regra.ParametrosRegras;

/* As faixas são configuração em produção; aqui entram pelo construtor, com os
   mesmos limites do SDD, para que os testes falem a linguagem das regras. */

public final class ParametrosRegrasFixture {

    private static final FaixaEtaria FAIXA_JOVEM = new FaixaEtaria(18, 24);
    private static final FaixaEtaria FAIXA_EXCECAO_SP = new FaixaEtaria(25, 29);

    private ParametrosRegrasFixture() {
    }

    public static ParametrosRegras padrao() {
        return new ParametrosRegras(FAIXA_JOVEM, FAIXA_EXCECAO_SP);
    }
}
