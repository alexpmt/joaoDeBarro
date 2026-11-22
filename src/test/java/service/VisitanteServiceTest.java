package service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class VisitanteServiceTest extends ServiceTestSupport {
    @Test
    public void cadastraBuscaEListaVisitante() {
        VisitanteService service = new VisitanteService(visitantes);
        service.cadastrarVisitante("Carlos", "333");
        assertNotNull(service.buscarPorCpf("333"));
        assertEquals("Carlos", service.buscarPorCpf("333").getNome());
        assertEquals(1, service.listarTodos().size());
    }

    @Test
    public void naoPermiteCadastrarCpfDuplicado() {
        VisitanteService service = new VisitanteService(visitantes);
        service.cadastrarVisitante("Carlos", "333");

        assertThrows(
                IllegalArgumentException.class,
                () -> service.cadastrarVisitante("Outro", "333")
        );
        assertEquals(1, service.listarTodos().size());
    }

    @Test
    public void rejeitaBuscaPorVisitanteInexistente() {
        VisitanteService service = new VisitanteService(visitantes);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.buscarPorCpf("999")
        );
    }

    @Test
    public void removeVisitanteCadastrado() {
        VisitanteService service = new VisitanteService(visitantes);
        service.cadastrarVisitante("Carlos", "333");
        service.removerVisitante("333");

        assertTrue(service.listarTodos().isEmpty());
        assertThrows(
                IllegalArgumentException.class,
                () -> service.buscarPorCpf("333")
        );
    }

    @Test
    public void rejeitaRemocaoDeVisitanteInexistente() {
        VisitanteService service = new VisitanteService(visitantes);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.removerVisitante("999")
        );
        assertTrue(service.listarTodos().isEmpty());
    }

    @Test
    public void listaVisitantesSemRegistros() {
        VisitanteService service = new VisitanteService(visitantes);

        assertTrue(service.listarTodos().isEmpty());
    }
}
