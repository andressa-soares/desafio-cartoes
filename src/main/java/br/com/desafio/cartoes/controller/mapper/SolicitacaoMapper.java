package br.com.desafio.cartoes.controller.mapper;

import br.com.desafio.cartoes.controller.dto.CartaoOfertadoDto;
import br.com.desafio.cartoes.controller.dto.ClienteDto;
import br.com.desafio.cartoes.controller.dto.SolicitacaoResponse;
import br.com.desafio.cartoes.domain.model.CartaoOfertado;
import br.com.desafio.cartoes.domain.model.Cliente;
import br.com.desafio.cartoes.domain.model.Solicitacao;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;

// data_nascimento é a fonte da verdade para a idade: o campo idade do request é
// lido no DTO mas ignorado em toCliente, nunca repassado ao domínio.
@Component
public class SolicitacaoMapper {

    private static final int ESCALA_VALOR_MONETARIO = 2;

    private final Clock clock;

    public SolicitacaoMapper(Clock clock) {
        this.clock = clock;
    }

    public Cliente toCliente(ClienteDto dto) {
        int idade = Period.between(dto.dataNascimento(), LocalDate.now(clock)).getYears();
        return new Cliente(
                dto.nome(),
                dto.cpf(),
                idade,
                dto.dataNascimento(),
                dto.uf(),
                dto.rendaMensal(),
                dto.email(),
                dto.telefoneWhatsapp()
        );
    }

    public SolicitacaoResponse toResponse(Solicitacao solicitacao) {
        return new SolicitacaoResponse(
                solicitacao.numero(),
                solicitacao.data(),
                toClienteDto(solicitacao.cliente()),
                solicitacao.cartoesOfertados().stream().map(this::toCartaoOfertadoDto).toList()
        );
    }

    private ClienteDto toClienteDto(Cliente cliente) {
        return new ClienteDto(
                cliente.nome(),
                cliente.cpf(),
                cliente.idade(),
                cliente.dataNascimento(),
                cliente.uf(),
                escala(cliente.rendaMensal()),
                cliente.email(),
                cliente.telefoneWhatsapp()
        );
    }

    private CartaoOfertadoDto toCartaoOfertadoDto(CartaoOfertado cartao) {
        return new CartaoOfertadoDto(
                cartao.tipo(),
                escala(cartao.anuidadeMensal()),
                escala(cartao.limiteDisponivel()),
                cartao.status()
        );
    }

    private static BigDecimal escala(BigDecimal valor) {
        return valor.setScale(ESCALA_VALOR_MONETARIO, RoundingMode.HALF_EVEN);
    }
}
