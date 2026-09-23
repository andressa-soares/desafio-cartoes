package br.com.desafio.cartoes.domain.regra;

import br.com.desafio.cartoes.domain.model.Cliente;
import br.com.desafio.cartoes.domain.model.FaixaEtaria;
import br.com.desafio.cartoes.domain.model.TipoCartao;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Set;

@Component
public class RegraJovem implements RegraElegibilidade {

    private static final Set<TipoCartao> PERMITIDOS = Set.of(TipoCartao.CARTAO_SEM_ANUIDADE);

    private final FaixaEtaria faixaJovem;

    public RegraJovem(ParametrosRegras parametros) {
        this.faixaJovem = Objects.requireNonNull(parametros, "Parâmetros das regras são obrigatórios.")
                .faixaJovem();
    }

    @Override
    public Set<TipoCartao> aplicar(Cliente cliente, Set<TipoCartao> candidatos) {
        if (!faixaJovem.contem(cliente.idade())) {
            return candidatos;
        }
        return RegraElegibilidade.manterApenas(candidatos, PERMITIDOS);
    }
}
