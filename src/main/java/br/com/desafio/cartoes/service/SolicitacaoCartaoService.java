package br.com.desafio.cartoes.service;

import br.com.desafio.cartoes.domain.exception.RegraNegocioException;
import br.com.desafio.cartoes.domain.model.CartaoOfertado;
import br.com.desafio.cartoes.domain.model.Cliente;
import br.com.desafio.cartoes.domain.model.Produto;
import br.com.desafio.cartoes.domain.model.Solicitacao;
import br.com.desafio.cartoes.domain.model.TipoCartao;
import br.com.desafio.cartoes.domain.regra.FiltroRenda;
import br.com.desafio.cartoes.domain.regra.RegraElegibilidade;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

// As regras chegam via injeção de todas as implementações de RegraElegibilidade
// (@Component): adicionar uma regra nova não exige alterar este service.
@Service
public class SolicitacaoCartaoService {

    private static final String TIPO_ERRO_RENDA_INSUFICIENTE = "RENDA_INSUFICIENTE";
    private static final String MENSAGEM_RENDA_INSUFICIENTE =
            "Renda mensal não atende à renda mínima de nenhum produto.";

    private final FiltroRenda filtroRenda;
    private final List<RegraElegibilidade> regras;
    private final Clock clock;

    public SolicitacaoCartaoService(FiltroRenda filtroRenda, List<RegraElegibilidade> regras, Clock clock) {
        this.filtroRenda = filtroRenda;
        this.regras = List.copyOf(regras);
        this.clock = clock;
    }

    public Solicitacao solicitar(Cliente cliente) {
        List<Produto> candidatos = filtroRenda.aplicar(cliente);
        if (candidatos.isEmpty()) {
            throw new RegraNegocioException(TIPO_ERRO_RENDA_INSUFICIENTE, MENSAGEM_RENDA_INSUFICIENTE);
        }

        Set<TipoCartao> tiposAprovados = candidatos.stream()
                .map(Produto::tipo)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(TipoCartao.class)));
        for (RegraElegibilidade regra : regras) {
            tiposAprovados = regra.aplicar(cliente, tiposAprovados);
        }

        Set<TipoCartao> tiposFinais = tiposAprovados;
        List<CartaoOfertado> cartoes = candidatos.stream()
                .filter(produto -> tiposFinais.contains(produto.tipo()))
                .map(CartaoOfertado::aprovado)
                .toList();

        return new Solicitacao(
                UUID.randomUUID(),
                LocalDateTime.now(clock).truncatedTo(ChronoUnit.MILLIS),
                cliente,
                cartoes
        );
    }
}
