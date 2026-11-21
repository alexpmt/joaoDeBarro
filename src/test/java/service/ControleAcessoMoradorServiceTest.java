
package service;

import model.RegistroAcessoMorador;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ControleAcessoMoradorServiceTest extends ServiceTestSupport {

    private Clock relogio(String dataHora) {
        return Clock.fixed(
                Instant.parse(dataHora),
                ZoneOffset.UTC
        );
    }

    private ControleAcessoMoradorService criarService(Clock clock) {
        return new ControleAcessoMoradorService(
                moradores,
                registrosAcessoMorador,
                clock
        );
    }

    private Long cadastrarMorador(String cpf) {
        ApartamentoService aps = new ApartamentoService(apartamentos);
        aps.cadastrarApartamento("A", cpf);
        Long idApartamento =
                aps.buscarPorNumeroTorre("A", cpf).getId();

        MoradorService ms = new MoradorService(apartamentos, moradores);
        ms.cadastrarMorador("Morador " + cpf, cpf, idApartamento, 0, 1);
        return ms.buscarPorCpf(cpf).getId();
    }

    @Test
    public void registraEntradaDeMorador() {
        cadastrarMorador("111");

        ControleAcessoMoradorService service = criarService(
                relogio("2026-11-21T10:00:00Z")
        );
        RegistroAcessoMorador registro =
                service.registrarEntrada("111");

        assertNotNull(registro);
        assertEquals("111", moradores.buscarCpf("111").getCpf());
        assertNotNull(registro.getEntrada());
        assertNull(registro.getSaida());
        assertTrue(registro.estaPresente());
        assertEquals(1, service.listaPresentes().size());
    }

    @Test
    public void registraSaidaDeMorador() {
        cadastrarMorador("111");

        ControleAcessoMoradorService entradaService = criarService(
                relogio("2026-11-21T10:00:00Z")
        );
        entradaService.registrarEntrada("111");

        ControleAcessoMoradorService saidaService = criarService(
                relogio("2026-11-21T12:00:00Z")
        );
        RegistroAcessoMorador registro =
                saidaService.registrarSaida("111");

        assertNotNull(registro.getSaida());
        assertFalse(registro.estaPresente());
        assertTrue(saidaService.listaPresentes().isEmpty());
    }

    @Test
    public void naoPermiteDuasEntradasConsecutivas() {
        cadastrarMorador("111");

        ControleAcessoMoradorService service = criarService(
                relogio("2026-11-21T10:00:00Z")
        );
        service.registrarEntrada("111");

        assertThrows(IllegalStateException.class, () ->
                service.registrarEntrada("111")
        );
        assertEquals(1, service.listarTodos().size());
    }

    @Test
    public void naoPermiteSaidaSemEntrada() {
        cadastrarMorador("111");

        ControleAcessoMoradorService service = criarService(
                relogio("2026-11-21T10:00:00Z")
        );

        assertThrows(IllegalStateException.class, () ->
                service.registrarSaida("111")
        );
        assertTrue(service.listarTodos().isEmpty());
    }

    @Test
    public void rejeitaMoradorInexistente() {
        ControleAcessoMoradorService service = criarService(
                relogio("2026-11-21T10:00:00Z")
        );

        assertThrows(IllegalArgumentException.class, () ->
                service.registrarEntrada("999")
        );
        assertThrows(IllegalArgumentException.class, () ->
                service.registrarSaida("999")
        );
        assertTrue(service.listarTodos().isEmpty());
    }

    @Test
    public void mantemHistoricoDeEntradasESaidas() {
        cadastrarMorador("111");

        ControleAcessoMoradorService entradaService = criarService(
                relogio("2026-11-21T10:00:00Z")
        );
        entradaService.registrarEntrada("111");

        ControleAcessoMoradorService saidaService = criarService(
                relogio("2026-11-21T12:00:00Z")
        );
        saidaService.registrarSaida("111");

        ControleAcessoMoradorService novaEntradaService = criarService(
                relogio("2026-11-21T14:00:00Z")
        );
        novaEntradaService.registrarEntrada("111");

        List<RegistroAcessoMorador> historico =
                novaEntradaService.historicoMorador("111");

        assertEquals(2, historico.size());
        assertFalse(historico.get(0).estaPresente());
        assertTrue(historico.get(1).estaPresente());
    }

    @Test
    public void listaSomenteMoradoresQueEstaoNoCondominio() {
        cadastrarMorador("111");
        cadastrarMorador("222");

        ControleAcessoMoradorService service = criarService(
                relogio("2026-11-21T10:00:00Z")
        );

        service.registrarEntrada("111");
        service.registrarEntrada("222");
        service.registrarSaida("111");

        assertEquals(1, service.listaPresentes().size());
        assertEquals(2, service.listarTodos().size());
    }

    @Test
    public void naoPermiteSaidaAnteriorAEntrada() {
        RegistroAcessoMorador registro = new RegistroAcessoMorador(
                1L,
                java.time.LocalDateTime.of(2026, 11, 21, 12, 0),
                null
        );

        assertThrows(IllegalArgumentException.class, () ->
                registro.registrarSaida(
                        java.time.LocalDateTime.of(2026, 11, 21, 10, 0)
                )
        );
        assertTrue(registro.estaPresente());
        assertNull(registro.getSaida());
    }

    @Test
    public void naoPermiteRegistrarSaidaDuasVezes() {
        RegistroAcessoMorador registro = new RegistroAcessoMorador(
                1L,
                java.time.LocalDateTime.of(2026, 11, 21, 10, 0),
                null
        );

        registro.registrarSaida(
                java.time.LocalDateTime.of(2026, 11, 21, 12, 0)
        );

        assertThrows(IllegalStateException.class, () ->
                registro.registrarSaida(
                        java.time.LocalDateTime.of(2026, 11, 21, 14, 0)
                )
        );
    }
}
