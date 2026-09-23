package br.com.desafio.cartoes.fixture;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;

/*
 * Relógio fixo usado nos testes de service, mapper e controller deste commit,
 * para que idade e data da solicitação sejam determinísticas e não dependam da
 * data real de execução. Vive fora de domain/config/controller porque é
 * infraestrutura de teste compartilhada entre as três camadas.
 */
public final class ClockFixture {

    public static final LocalDateTime DATA_HORA_FIXA = LocalDateTime.of(2026, 9, 22, 10, 15, 30, 123_000_000);

    private ClockFixture() {
    }

    public static Clock fixo() {
        return Clock.fixed(DATA_HORA_FIXA.atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
    }
}
