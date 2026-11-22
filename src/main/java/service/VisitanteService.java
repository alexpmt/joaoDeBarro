package service;

import model.Visitante;
import repository.VisitanteRepository;

import java.util.List;

public class VisitanteService {
    private final VisitanteRepository visitanteRepository;

    public VisitanteService(VisitanteRepository visitanteRepository) {
        this.visitanteRepository = visitanteRepository;
    }

    public List<Visitante> listarTodos() {
        return visitanteRepository.listarTudo();
    }

    public Visitante buscarPorCpf(String cpf) {
        Visitante visitante = visitanteRepository.buscarCpf(cpf);
        if (visitante == null) throw new IllegalArgumentException("Visitante não encontrado.");
        return visitante;
    }

    public void cadastrarVisitante(String nome, String cpf) {
        if (visitanteRepository.buscarCpf(cpf) != null) {
            throw new IllegalArgumentException("Já existe um visitante com esse CPF.");
        }

        Visitante visitante = new Visitante(nome,  cpf);
        visitanteRepository.salvar(visitante);
    }

    public void removerVisitante(String cpf) {
        Visitante visitante = visitanteRepository.buscarCpf(cpf);
        if (visitante == null) {
            throw new IllegalArgumentException("Visitante não encontrado.");
        }

        visitanteRepository.remover(visitante.getId());
    }
}
