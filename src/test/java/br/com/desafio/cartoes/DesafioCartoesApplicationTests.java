package br.com.desafio.cartoes;

import br.com.desafio.cartoes.domain.regra.RegraElegibilidade;
import br.com.desafio.cartoes.domain.regra.RegraJovem;
import br.com.desafio.cartoes.domain.regra.RegraResidenteSp;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class DesafioCartoesApplicationTests {

	@Autowired
	private List<RegraElegibilidade> regras;

	@Test
	void contextLoads() {
	}

	@Test
	void regrasDePerfilSaoDescobertasPeloSpring() {
		assertThat(regras)
				.hasAtLeastOneElementOfType(RegraJovem.class)
				.hasAtLeastOneElementOfType(RegraResidenteSp.class);
	}

}
