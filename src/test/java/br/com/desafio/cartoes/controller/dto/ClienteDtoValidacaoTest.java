package br.com.desafio.cartoes.controller.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static br.com.desafio.cartoes.controller.fixture.SolicitacaoRequestFixture.umaSolicitacao;
import static org.assertj.core.api.Assertions.assertThat;

/*
 * Só cobre as validações exigidas pelo enunciado: campo obrigatório, renda não
 * negativa e formato de e-mail. CPF, telefone e UF não têm formato validado.
 */
@DisplayName("ClienteDto: validação de campo")
class ClienteDtoValidacaoTest {

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("cliente válido não produz violações")
    void clienteValidoNaoProduzViolacoes() {
        var violacoes = VALIDATOR.validate(umaSolicitacao().clienteDto());

        assertThat(violacoes).isEmpty();
    }

    @Nested
    @DisplayName("campos obrigatórios")
    class CamposObrigatorios {

        static Stream<Arguments> clientesComCampoAusente() {
            return Stream.of(
                    Arguments.of("nome", umaSolicitacao().comNome(null).clienteDto()),
                    Arguments.of("cpf", umaSolicitacao().comCpf(null).clienteDto()),
                    Arguments.of("idade", umaSolicitacao().comIdade(null).clienteDto()),
                    Arguments.of("dataNascimento", umaSolicitacao().comDataNascimento(null).clienteDto()),
                    Arguments.of("uf", umaSolicitacao().comUf(null).clienteDto()),
                    Arguments.of("rendaMensal", umaSolicitacao().comRendaMensal(null).clienteDto()),
                    Arguments.of("email", umaSolicitacao().comEmail(null).clienteDto()),
                    Arguments.of("telefoneWhatsapp", umaSolicitacao().comTelefoneWhatsapp(null).clienteDto())
            );
        }

        @ParameterizedTest(name = "campo {0} ausente")
        @MethodSource("clientesComCampoAusente")
        @DisplayName("cada campo obrigatório ausente produz violação apontando o próprio campo")
        void campoAusenteProduzViolacaoNoProprioCampo(String campoEsperado, ClienteDto cliente) {
            var violacoes = VALIDATOR.validate(cliente);

            assertThat(violacoes)
                    .extracting(violacao -> violacao.getPropertyPath().toString())
                    .contains(campoEsperado);
        }

        @Test
        @DisplayName("nome em branco produz violação")
        void nomeEmBrancoProduzViolacao() {
            var violacoes = VALIDATOR.validate(umaSolicitacao().comNome("").clienteDto());

            assertThat(violacoes).isNotEmpty();
        }

        @Test
        @DisplayName("nome composto só de espaços produz violação")
        void nomeComEspacosProduzViolacao() {
            var violacoes = VALIDATOR.validate(umaSolicitacao().comNome("   ").clienteDto());

            assertThat(violacoes).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("renda_mensal")
    class RendaMensal {

        @Test
        @DisplayName("renda negativa produz violação")
        void rendaNegativaProduzViolacao() {
            var violacoes = VALIDATOR.validate(umaSolicitacao().comRendaMensal("-0.01").clienteDto());

            assertThat(violacoes).isNotEmpty();
        }

        @Test
        @DisplayName("renda zero não produz violação")
        void rendaZeroNaoProduzViolacao() {
            var violacoes = VALIDATOR.validate(umaSolicitacao().comRendaMensal("0.00").clienteDto());

            assertThat(violacoes).isEmpty();
        }
    }

    @Nested
    @DisplayName("email")
    class Email {

        @Test
        @DisplayName("e-mail sem @ produz violação")
        void emailSemArrobaProduzViolacao() {
            var violacoes = VALIDATOR.validate(umaSolicitacao().comEmail("clienteteste.com").clienteDto());

            assertThat(violacoes).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("cpf (sem validação de formato)")
    class Cpf {

        @ParameterizedTest(name = "cpf \"{0}\" não produz violação")
        @ValueSource(strings = {"12345678910", "123.456.789-10"})
        @DisplayName("CPF com ou sem pontuação não produz violação")
        void cpfComOuSemPontuacaoNaoProduzViolacao(String cpf) {
            var violacoes = VALIDATOR.validate(umaSolicitacao().comCpf(cpf).clienteDto());

            assertThat(violacoes).isEmpty();
        }
    }

    @Nested
    @DisplayName("telefone_whatsapp (sem validação de formato)")
    class TelefoneWhatsapp {

        @ParameterizedTest(name = "telefone \"{0}\" não produz violação")
        @ValueSource(strings = {"9999999999", "99999999999"})
        @DisplayName("telefone corrido com 10 ou 11 dígitos não produz violação")
        void telefoneCorridoNaoProduzViolacao(String telefone) {
            var violacoes = VALIDATOR.validate(umaSolicitacao().comTelefoneWhatsapp(telefone).clienteDto());

            assertThat(violacoes).isEmpty();
        }
    }

    @Nested
    @DisplayName("uf (sem validação de formato)")
    class Uf {

        @Test
        @DisplayName("UF minúscula não produz violação")
        void ufMinusculaNaoProduzViolacao() {
            var violacoes = VALIDATOR.validate(umaSolicitacao().comUf("sp").clienteDto());

            assertThat(violacoes).isEmpty();
        }
    }
}
