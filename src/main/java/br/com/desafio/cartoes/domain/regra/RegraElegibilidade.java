package br.com.desafio.cartoes.domain.regra;

import br.com.desafio.cartoes.domain.model.Cliente;
import br.com.desafio.cartoes.domain.model.TipoCartao;

import java.util.EnumSet;
import java.util.Collections;
import java.util.Set;

// Contrato de toda implementação: só remove candidatos, nunca adiciona um tipo
// que não estava na entrada; devolve os próprios candidatos quando não se aplica
// ao cliente; não depende das demais regras, então a ordem de aplicação é
// irrelevante — o resultado final é a interseção de todas.
public interface RegraElegibilidade {

    Set<TipoCartao> aplicar(Cliente cliente, Set<TipoCartao> candidatos);

    static Set<TipoCartao> manterApenas(Set<TipoCartao> candidatos, Set<TipoCartao> permitidos) {
        var resultado = EnumSet.noneOf(TipoCartao.class);
        for (TipoCartao candidato : candidatos) {
            if (permitidos.contains(candidato)) {
                resultado.add(candidato);
            }
        }
        return Collections.unmodifiableSet(resultado);
    }
}
