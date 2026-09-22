package br.com.desafio.cartoes.domain.regra;

import br.com.desafio.cartoes.domain.model.Cliente;
import br.com.desafio.cartoes.domain.model.TipoCartao;

import java.util.EnumSet;
import java.util.Collections;
import java.util.Set;

/* Regra de adequação de perfil aplicada sobre os candidatos que já passaram pelo filtro de renda.

   Contrato de toda implementação:
     Só remove candidatos, nunca adiciona um tipo que não estava na entrada;
     Devolve os próprios candidatos quando a regra não se aplica ao cliente;
     Não depende das demais regras: o resultado final é a interseção de todas,
     portanto a ordem de aplicação é irrelevante. */

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
