package br.com.desafio.cartoes.controller.mapper;

import br.com.desafio.cartoes.config.CartoesProperties;
import br.com.desafio.cartoes.controller.dto.CartaoOfertadoDto;
import br.com.desafio.cartoes.controller.dto.ClienteDto;
import br.com.desafio.cartoes.controller.dto.SolicitacaoResponse;
import br.com.desafio.cartoes.controller.exception.RequisicaoInvalidaException;
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
// lido no DTO, mas só usado para conferir coerência com a data — nunca repassado
// direto ao domínio.
@Component
public class SolicitacaoMapper {

    private static final int ESCALA_VALOR_MONETARIO = 2;

    private final Clock clock;
    private final CartoesProperties propriedades;

    public SolicitacaoMapper(Clock clock, CartoesProperties propriedades) {
        this.clock = clock;
        this.propriedades = propriedades;
    }

    /*
     * Idade mínima e coerência idade/data_nascimento não cabem em anotação Bean
     * Validation: a primeira vem da configuração (não é uma constante de
     * compilação) e a segunda cruza dois campos do DTO. Por isso ficam aqui,
     * junto do cálculo da idade que as duas checagens precisam de qualquer forma.
     */
    public Cliente toCliente(ClienteDto dto) {
        int idadeCalculada = Period.between(dto.dataNascimento(), LocalDate.now(clock)).getYears();

        if (idadeCalculada < propriedades.idadeMinima()) {
            throw new RequisicaoInvalidaException(
                    "O campo data_nascimento indica idade menor que o mínimo de "
                            + propriedades.idadeMinima() + " anos exigido.");
        }
        if (dto.idade() != null && !dto.idade().equals(idadeCalculada)) {
            throw new RequisicaoInvalidaException("O campo idade não é coerente com data_nascimento.");
        }

        return new Cliente(
                dto.nome(),
                dto.cpf(),
                idadeCalculada,
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
