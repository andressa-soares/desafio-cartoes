package br.com.desafio.cartoes.domain.regra;

import br.com.desafio.cartoes.domain.model.Cliente;
import br.com.desafio.cartoes.domain.model.FaixaEtaria;
import br.com.desafio.cartoes.domain.model.TipoCartao;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Set;

/* Residente em SP não recebe o cartão de parceiros, exceto na faixa etária de
   exceção prevista, em que nada é restringido.

   A exceção está aqui, porque é exceção a esta regra: sozinha ela não descreve
   critério algum de elegibilidade, apenas desliga a restrição de SP para uma faixa de idade. */

@Component
public class RegraResidenteSp implements RegraElegibilidade {

    private static final String UF = "SP";

    private static final Set<TipoCartao> PERMITIDOS =
            Set.of(TipoCartao.CARTAO_SEM_ANUIDADE, TipoCartao.CARTAO_COM_CASHBACK);

    private final FaixaEtaria faixaExcecao;

    public RegraResidenteSp(ParametrosRegras parametros) {
        this.faixaExcecao = Objects.requireNonNull(parametros, "Parâmetros das regras são obrigatórios.")
                .faixaExcecaoResidenteSp();
    }

    @Override
    public Set<TipoCartao> aplicar(Cliente cliente, Set<TipoCartao> candidatos) {
        if (!cliente.resideEm(UF) || faixaExcecao.contem(cliente.idade())) {
            return candidatos;
        }
        return RegraElegibilidade.manterApenas(candidatos, PERMITIDOS);
    }
}
