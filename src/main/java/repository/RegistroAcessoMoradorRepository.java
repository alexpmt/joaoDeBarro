package repository;

import model.RegistroAcessoMorador;

import java.nio.file.Path;
import java.util.List;

public class RegistroAcessoMoradorRepository extends CsvRepository<RegistroAcessoMorador>{

    public RegistroAcessoMoradorRepository() {
        super(Path.of("data", "registro_acesso_morador.csv"),  RegistroAcessoMorador.class);
    }

    public List<RegistroAcessoMorador> buscarPresentes() {
        return dados.stream()
                .filter(RegistroAcessoMorador::estaPresente)
                .toList();
    }

    public RegistroAcessoMorador buscarStatusMorador(Long moradorId) {
        return dados.stream()
                .filter(r -> r.estaPresente()
                        && r.getIdMorador().equals(moradorId))
                .findFirst().orElse(null);
    }

    public List<RegistroAcessoMorador> buscarPorMorador(Long moradorId) {
        return dados.stream()
                .filter(r -> r.getIdMorador().equals(moradorId))
                .toList();
    }
}
