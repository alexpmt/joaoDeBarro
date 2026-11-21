package service;

import model.Morador;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MoradorServiceTest extends ServiceTestSupport {
    @Test
    public void cadastraMoradorVinculadoAApartamento() {
        ApartamentoService aps = new ApartamentoService(apartamentos);
        aps.cadastrarApartamento("12", "C");
        Long idApartamento = aps.buscarPorNumeroTorre("12", "C").getId();
        MoradorService service = new MoradorService(apartamentos, moradores);
        service.cadastrarMorador("Ana", "111", idApartamento, 1, 2);
        Morador m = service.buscarPorCpf("111");
        assertNotNull(m);
        assertEquals("Ana", m.getNome());
        assertEquals(idApartamento, m.getApartamentoId());
        assertEquals(1, m.getQtdPets());
        assertEquals(2, m.getVagasGaragem());
    }

    @Test
    public void rejeitaCpfDuplicado() {
        ApartamentoService aps = new ApartamentoService(apartamentos);
        aps.cadastrarApartamento("12", "C");
        Long id = aps.buscarPorNumeroTorre("12", "C").getId();
        MoradorService service = new MoradorService(apartamentos, moradores);
        service.cadastrarMorador("Ana", "111", id, 0, 1);
        assertThrows(IllegalArgumentException.class, () -> {
            service.cadastrarMorador("Outra Ana", "111", id, 0, 1);
        });
        assertEquals(1, service.listarTodos().size());
    }

    @Test
    public void rejeitaApartamentoInexistente() {
        MoradorService service = new MoradorService(apartamentos, moradores);
        assertThrows(IllegalArgumentException.class, () -> {
            service.cadastrarMorador("Ana", "111", 99L, 0, 1);
        });
        assertTrue(service.listarTodos().isEmpty());
    }

    @Test
    public void buscarMoradorInexistente() {
        MoradorService service = new MoradorService(apartamentos, moradores);
        assertThrows(IllegalArgumentException.class, () -> {
            service.buscarPorCpf("11234");
        });
    }

    @Test
    public void removeMoradorCadastrado() {
        new ApartamentoService(apartamentos).cadastrarApartamento("12", "C");
        Long id = apartamentos.buscarPorTorreNumero("12", "C").getId();
        MoradorService service = new MoradorService(apartamentos, moradores);
        service.cadastrarMorador("Ana", "111", id, 0, 1);
        service.removerMorador("111");
        assertTrue(service.listarTodos().isEmpty());
        assertThrows(IllegalArgumentException.class, () -> {
            service.buscarPorCpf("111");
        });
    }

    @Test
    public void removeMoradorInexistente() {
        MoradorService service = new MoradorService(apartamentos, moradores);
        assertThrows(IllegalArgumentException.class, () -> {
            service.removerMorador("111");
        });
        assertTrue(service.listarTodos().isEmpty());
    }


    @Test
    public void editaMoradorCadastrado() {
        ApartamentoService aps = new ApartamentoService(apartamentos);
        aps.cadastrarApartamento("12", "C");
        aps.cadastrarApartamento("12", "D");
        Long idApartamentoOriginal =
                aps.buscarPorNumeroTorre("12", "C").getId();
        Long novoIdApartamento =
                aps.buscarPorNumeroTorre("12", "D").getId();

        MoradorService service = new MoradorService(apartamentos, moradores);
        service.cadastrarMorador("Ana", "111", idApartamentoOriginal, 1, 1);
        Long idOriginal = service.buscarPorCpf("111").getId();
        service.editarMorador("Ana Silva", "111", novoIdApartamento, 2, 2);
        Morador editado = service.buscarPorCpf("111");

        assertEquals(idOriginal, editado.getId());
        assertEquals("Ana Silva", editado.getNome());
        assertEquals(novoIdApartamento, editado.getApartamentoId());
        assertEquals(2, editado.getQtdPets());
        assertEquals(2, editado.getVagasGaragem());
        assertEquals(1, service.listarTodos().size());
    }

    @Test
    public void naoEditaMoradorInexistente() {
        MoradorService service = new MoradorService(apartamentos, moradores);
        assertThrows(IllegalArgumentException.class, () ->
                service.editarMorador("Ana", "999", 1L, 0, 1)
        );
        assertTrue(service.listarTodos().isEmpty());
    }

    @Test
    public void naoEditaMoradorComApartamentoInexistente() {
        ApartamentoService aps = new ApartamentoService(apartamentos);
        aps.cadastrarApartamento("12", "C");
        Long idApartamento =
                aps.buscarPorNumeroTorre("12", "C").getId();

        MoradorService service = new MoradorService(apartamentos, moradores);
        service.cadastrarMorador("Ana", "111", idApartamento, 1, 1);

        Long idOriginal = service.buscarPorCpf("111").getId();
        assertThrows(IllegalArgumentException.class, () ->
                service.editarMorador("Ana Silva", "111", 999L, 2, 2)
        );

        Morador morador = service.buscarPorCpf("111");
        assertEquals(idOriginal, morador.getId());
        assertEquals("Ana", morador.getNome());
        assertEquals(idApartamento, morador.getApartamentoId());
        assertEquals(1, morador.getQtdPets());
        assertEquals(1, morador.getVagasGaragem());
    }

    @Test
    public void edicaoNaoCriaOutroMorador() {
        ApartamentoService aps = new ApartamentoService(apartamentos);
        aps.cadastrarApartamento("12", "C");
        Long idApartamento =
                aps.buscarPorNumeroTorre("12", "C").getId();

        MoradorService service = new MoradorService(apartamentos, moradores);
        service.cadastrarMorador("Ana", "111", idApartamento, 1, 1);
        service.editarMorador("Ana Silva", "111", idApartamento, 2, 2);

        assertEquals(1, service.listarTodos().size());
    }

}
