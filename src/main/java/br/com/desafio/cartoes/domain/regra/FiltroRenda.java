package br.com.desafio.cartoes.domain.regra;

import br.com.desafio.cartoes.domain.model.Cliente;
import br.com.desafio.cartoes.domain.model.Produto;

import java.util.List;

/* Seleciona os produtos cuja renda mínima o cliente atinge.
   É a fronteira entre análise de crédito e adequação de perfil. */

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