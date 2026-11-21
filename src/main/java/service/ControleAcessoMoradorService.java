package service;

import model.Morador;
import model.RegistroAcessoMorador;
import repository.MoradorRepository;
import repository.RegistroAcessoMoradorRepository;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class ControleAcessoMoradorService {

    private final MoradorRepository moradorRepository;
    private final RegistroAcessoMoradorRepository registroAcessoMoradorRepository;
    private final Clock relogio;

    public ControleAcessoMoradorService(MoradorRepository moradorRepository, RegistroAcessoMoradorRepository registroAcessoMoradorRepository, Clock relogio) {
        this.moradorRepository = moradorRepository;
        this.registroAcessoMoradorRepository = registroAcessoMoradorRepository;
        this.relogio = relogio == null ? Clock.systemDefaultZone() : relogio;
    }

    public List<RegistroAcessoMorador> listaPresentes() {
        return registroAcessoMoradorRepository.buscarPresentes();
    }

    public RegistroAcessoMorador registrarEntrada(String cpf) {
        Morador morador = moradorRepository.buscarCpf(cpf);
        if (morador == null) {
            throw new IllegalArgumentException("Morador não encontrado.");
        }

        RegistroAcessoMorador anterior = registroAcessoMoradorRepository.buscarStatusMorador(morador.getId());
        if (anterior != null) {
            throw new IllegalStateException(
                    "O morador já está no prédio.");
        }

        RegistroAcessoMorador registro = new RegistroAcessoMorador(morador.getId(),
                LocalDateTime.now(relogio).truncatedTo(ChronoUnit.MINUTES), null);
        registroAcessoMoradorRepository.salvar(registro);
        return registro;
    }

    public RegistroAcessoMorador registrarSaida(String cpf) {
        Morador morador = moradorRepository.buscarCpf(cpf);
        if (morador == null) {
            throw new IllegalArgumentException("Morador não encontrado.");
        }

        RegistroAcessoMorador registro = registroAcessoMoradorRepository.buscarStatusMorador(morador.getId());
        if (registro == null) {
            throw new IllegalStateException("Não foi encontrado um registro de entrada para esse morador.");
        }

        registro.registrarSaida(LocalDateTime.now(relogio).truncatedTo(ChronoUnit.MINUTES));
        registroAcessoMoradorRepository.atualizar(registro);
        return registro;
    }

    public List<RegistroAcessoMorador> listarTodos() {
        return registroAcessoMoradorRepository.listarTudo();
    }

    public List<RegistroAcessoMorador> historicoMorador(String cpf) {
        Morador morador = moradorRepository.buscarCpf(cpf);
        if (morador == null) {
            throw new IllegalArgumentException("Morador não encontrado.");
        }
        return registroAcessoMoradorRepository.buscarPorMorador(morador.getId());
    }
}
