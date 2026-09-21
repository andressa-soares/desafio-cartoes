package br.com.desafio.cartoes.domain.regra;

import br.com.desafio.cartoes.domain.model.Cliente;
import br.com.desafio.cartoes.domain.model.Produto;

import java.util.List;

/**
 * Primeira etapa da análise: seleciona os produtos cuja renda mínima o
 * cliente atinge. É a fronteira entre análise de crédito e adequação de
 * perfil. Se nenhum produto passar, a solicitação é recusada (422); a
 * decisão de recusar é do service, este filtro apenas devolve a lista.
 */
public class FiltroRenda {

    private final List<Produto> produtos;

    public FiltroRenda(List<Produto> produtos) {
        this.produtos = List.copyOf(produtos);
    }

    public List<Produto> aplicar(Cliente cliente) {
        return produtos.stream()
                .filter(produto -> produto.atendeRenda(cliente.rendaMensal()))
                .toList();
    }
}