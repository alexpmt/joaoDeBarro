package service;

import model.Autorizacao;
import model.Morador;
import model.Visitante;
import repository.AutorizacaoRepository;
import repository.MoradorRepository;
import repository.VisitanteRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AutorizacaoService {

    private final MoradorRepository moradorRepository;
    private final VisitanteRepository visitanteRepository;
    private final AutorizacaoRepository autorizacaoRepository;

    public AutorizacaoService(MoradorRepository moradorRepository, VisitanteRepository visitanteRepository, AutorizacaoRepository autorizacaoRepository) {
        this.moradorRepository = moradorRepository;
        this.visitanteRepository = visitanteRepository;
        this.autorizacaoRepository = autorizacaoRepository;
    }

    public List<Autorizacao> listarTodos() {
        return autorizacaoRepository.listarTudo();
    }

    public List<Autorizacao> buscarPorVisitanteDia(String cpfVisitante, LocalDate dia) {
        Visitante visitante = visitanteRepository.buscarCpf(cpfVisitante);
        if (visitante == null) {
            throw new IllegalArgumentException("Visitante não encontrado.");
        }

        return autorizacaoRepository.buscarVisitanteData(visitante.getId(), dia);
    }

    public Autorizacao buscarPorMoradorVisitanteDia(String cpfMorador, String cpfVisitante, LocalDate dia) {
        Morador morador = moradorRepository.buscarCpf(cpfMorador);
        if (morador == null) {
            throw new IllegalArgumentException("Morador não encontrado.");
        }

        Visitante visitante = visitanteRepository.buscarCpf(cpfVisitante);
        if (visitante == null) {
            throw new IllegalArgumentException("Visitante não encontrado.");
        }

        Autorizacao autorizacao = autorizacaoRepository.buscarMoradorVisitanteData(morador.getId(), visitante.getId(), dia);
        if (autorizacao == null) {
            throw new IllegalArgumentException("Autorização não encontrada.");
        }
        return autorizacao;
    }

    public void cadastrarAutorizacao(String cpfMorador, LocalDate dia, String cpfVisitante, String nomeVisitante) {
        Morador morador = moradorRepository.buscarCpf(cpfMorador);
        if (morador == null) {
            throw new IllegalArgumentException("Morador não encontrado.");
        }

        Visitante visitante = visitanteRepository.buscarCpf(cpfVisitante);
        if (visitante == null) {
            visitante =  new Visitante(nomeVisitante, cpfVisitante);
            visitanteRepository.salvar(visitante);
        }

        Autorizacao autorizacao = autorizacaoRepository.buscarMoradorVisitanteData(morador.getId(), visitante.getId(), dia);
        if (autorizacao != null) {
            throw new IllegalStateException("O visitante já está autorizado no dia fornecido.");
        }
        autorizacao = new Autorizacao(morador.getId(), visitante.getId(), dia);
        autorizacaoRepository.salvar(autorizacao);
    }

    public void removerAutorizacao(String cpfMorador, LocalDate dia, String cpfVisitante) {
        Autorizacao autorizacao = buscarPorMoradorVisitanteDia(cpfMorador, cpfVisitante, dia);
        autorizacaoRepository.remover(autorizacao.getId());
    }
}
