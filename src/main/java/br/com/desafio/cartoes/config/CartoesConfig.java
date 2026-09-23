package br.com.desafio.cartoes.config;

import br.com.desafio.cartoes.domain.regra.FiltroRenda;
import br.com.desafio.cartoes.domain.regra.ParametrosRegras;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

// Não há bean List<Produto>: o Spring trataria a injeção desse tipo como "todos
// os beans do tipo Produto" em vez do parâmetro que ele é.
@Configuration
@EnableConfigurationProperties(CartoesProperties.class)
public class CartoesConfig {

    @Bean
    public ParametrosRegras parametrosRegras(CartoesProperties propriedades) {
        return propriedades.toParametrosRegras();
    }

    @Bean
    public FiltroRenda filtroRenda(CartoesProperties propriedades) {
        return new FiltroRenda(propriedades.toProdutos());
    }

    // Injetado (em vez de Clock.systemDefaultZone() direto no service/mapper) para
    // poder ser substituído por um relógio fixo nos testes.
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
