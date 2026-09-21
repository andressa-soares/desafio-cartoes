package br.com.desafio.cartoes.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;

import static br.com.desafio.cartoes.domain.fixture.ClienteFixture.umCliente;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

@DisplayName("Cliente")
class ClienteTest {

    @Nested
    @DisplayName("construtor")
    class Construtor {

        @Test
        @DisplayName("mantém os dados informados quando todos são válidos")
        void criaClienteComOsDadosInformados() {
            var cliente = umCliente()
                    .comNome("Maria Souza")
                    .comCpf("98765432100")
                    .comIdade(31)
                    .comDataNascimento(LocalDate.of(1995, 7, 20))
                    .comUf("SP")
                    .comRendaMensal("4200.50")
                    .comEmail("maria@exemplo.com")
                    .comTelefoneWhatsapp("11988887777")
                    .build();

            assertThat(cliente.nome()).isEqualTo("Maria Souza");
            assertThat(cliente.cpf()).isEqualTo("98765432100");
            assertThat(cliente.idade()).isEqualTo(31);
            assertThat(cliente.dataNascimento()).isEqualTo(LocalDate.of(1995, 7, 20));
            assertThat(cliente.uf()).isEqualTo("SP");
            assertThat(cliente.rendaMensal()).isEqualByComparingTo("4200.50");
            assertThat(cliente.email()).isEqualTo("maria@exemplo.com");
            assertThat(cliente.telefoneWhatsapp()).isEqualTo("11988887777");
        }

        @Test
        @DisplayName("rejeita nome nulo")
        void rejeitaNomeNulo() {
            var fixture = umCliente().comNome(null);

            assertThatNullPointerException()
                    .isThrownBy(fixture::build)
                    .withMessage("Nome é obrigatório.");
        }

        @Test
        @DisplayName("rejeita CPF nulo")
        void rejeitaCpfNulo() {
            var fixture = umCliente().comCpf(null);

            assertThatNullPointerException()
                    .isThrownBy(fixture::build)
                    .withMessage("CPF é obrigatório.");
        }

        @Test
        @DisplayName("rejeita data de nascimento nula")
        void rejeitaDataNascimentoNula() {
            var fixture = umCliente().comDataNascimento(null);

            assertThatNullPointerException()
                    .isThrownBy(fixture::build)
                    .withMessage("Data de nascimento é obrigatória.");
        }

        @Test
        @DisplayName("rejeita UF nula")
        void rejeitaUfNula() {
            var fixture = umCliente().comUf(null);

            assertThatNullPointerException()
                    .isThrownBy(fixture::build)
                    .withMessage("UF é obrigatória.");
        }

        @Test
        @DisplayName("rejeita renda mensal nula")
        void rejeitaRendaMensalNula() {
            var fixture = umCliente().comRendaMensal(null);

            assertThatNullPointerException()
                    .isThrownBy(fixture::build)
                    .withMessage("Renda mensal é obrigatória.");
        }

        @Test
        @DisplayName("rejeita e-mail nulo")
        void rejeitaEmailNulo() {
            var fixture = umCliente().comEmail(null);

            assertThatNullPointerException()
                    .isThrownBy(fixture::build)
                    .withMessage("E-mail é obrigatório.");
        }

        @Test
        @DisplayName("rejeita telefone de WhatsApp nulo")
        void rejeitaTelefoneWhatsappNulo() {
            var fixture = umCliente().comTelefoneWhatsapp(null);

            assertThatNullPointerException()
                    .isThrownBy(fixture::build)
                    .withMessage("Telefone WhatsApp é obrigatório.");
        }

        @Test
        @DisplayName("rejeita idade negativa")
        void rejeitaIdadeNegativa() {
            var fixture = umCliente().comIdade(-1);

            assertThatIllegalArgumentException()
                    .isThrownBy(fixture::build)
                    .withMessage("Idade não pode ser negativa.");
        }

        @Test
        @DisplayName("aceita idade zero")
        void aceitaIdadeZero() {
            var fixture = umCliente().comIdade(0);

            assertThatCode(fixture::build).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("rejeita renda mensal negativa")
        void rejeitaRendaMensalNegativa() {
            var fixture = umCliente().comRendaMensal("-0.01");

            assertThatIllegalArgumentException()
                    .isThrownBy(fixture::build)
                    .withMessage("Renda mensal não pode ser negativa.");
        }

        @Test
        @DisplayName("aceita renda mensal zero")
        void aceitaRendaMensalZero() {
            var cliente = umCliente().comRendaMensal("0.00").build();

            assertThat(cliente.rendaMensal()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("aceita renda mensal zero escrita com escala diferente")
        void aceitaRendaMensalZeroComEscalaDiferente() {
            var fixture = umCliente().comRendaMensal("0.0000");

            assertThatCode(fixture::build).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("resideEm")
    class ResideEm {

        @Test
        @DisplayName("confirma residência quando a UF é a mesma")
        void confirmaResidenciaQuandoUfEhAMesma() {
            var cliente = umCliente().comUf("SP").build();

            var resultado = cliente.resideEm("SP");

            assertThat(resultado).isTrue();
        }

        @Test
        @DisplayName("nega residência quando a UF é outra")
        void negaResidenciaQuandoUfEhOutra() {
            var cliente = umCliente().comUf("BA").build();

            var resultado = cliente.resideEm("SP");

            assertThat(resultado).isFalse();
        }

        @ParameterizedTest(name = "cliente em {0} reside em {1}")
        @CsvSource({"sp,SP", "SP,sp", "Sp,sP"})
        @DisplayName("ignora maiúsculas e minúsculas na comparação da UF")
        void ignoraCaixaDaUf(String ufDoCliente, String ufConsultada) {
            var cliente = umCliente().comUf(ufDoCliente).build();

            var resultado = cliente.resideEm(ufConsultada);

            assertThat(resultado).isTrue();
        }
    }

    @Nested
    @DisplayName("temIdadeEntre")
    class TemIdadeEntre {

        @Test
        @DisplayName("inclui a idade exatamente no início da faixa")
        void incluiInicioDaFaixa() {
            var cliente = umCliente().comIdade(18).build();

            var resultado = cliente.temIdadeEntre(18, 24);

            assertThat(resultado).isTrue();
        }

        @Test
        @DisplayName("inclui a idade exatamente no fim da faixa")
        void incluiFimDaFaixa() {
            var cliente = umCliente().comIdade(24).build();

            var resultado = cliente.temIdadeEntre(18, 24);

            assertThat(resultado).isTrue();
        }

        @Test
        @DisplayName("exclui a idade um ano abaixo do início da faixa")
        void excluiIdadeAbaixoDaFaixa() {
            var cliente = umCliente().comIdade(17).build();

            var resultado = cliente.temIdadeEntre(18, 24);

            assertThat(resultado).isFalse();
        }

        @Test
        @DisplayName("exclui a idade um ano acima do fim da faixa")
        void excluiIdadeAcimaDaFaixa() {
            var cliente = umCliente().comIdade(25).build();

            var resultado = cliente.temIdadeEntre(18, 24);

            assertThat(resultado).isFalse();
        }

        @ParameterizedTest(name = "idade {0} está dentro da faixa")
        @ValueSource(ints = {19, 20, 21, 22, 23})
        @DisplayName("inclui as idades no interior da faixa")
        void incluiIdadesNoInteriorDaFaixa(int idade) {
            var cliente = umCliente().comIdade(idade).build();

            var resultado = cliente.temIdadeEntre(18, 24);

            assertThat(resultado).isTrue();
        }

        @Test
        @DisplayName("aceita faixa de um único ano quando a idade coincide")
        void aceitaFaixaDeUmUnicoAno() {
            var cliente = umCliente().comIdade(30).build();

            var resultado = cliente.temIdadeEntre(30, 30);

            assertThat(resultado).isTrue();
        }

        @Test
        @DisplayName("não inclui idade alguma quando a faixa está invertida")
        void naoIncluiIdadeQuandoFaixaEstaInvertida() {
            var cliente = umCliente().comIdade(27).build();

            var resultado = cliente.temIdadeEntre(30, 25);

            assertThat(resultado).isFalse();
        }
    }
}
