package model;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;

import java.time.LocalDateTime;

public class RegistroAcessoMorador implements Identificavel{
    @CsvBindByName private Long id;
    @CsvBindByName private Long idMorador;
    @CsvBindByName @CsvDate("yyyy-MM-dd HH:mm") private LocalDateTime entrada;
    @CsvBindByName @CsvDate("yyyy-MM-dd HH:mm") private LocalDateTime saida;

    public RegistroAcessoMorador() {}

    public RegistroAcessoMorador(Long idMorador, LocalDateTime entrada, LocalDateTime saida) {
        this.idMorador = idMorador;
        this.entrada = entrada;
        this.saida = saida;
    }

    public boolean estaPresente() {
        return saida == null;
    }

    public void registrarSaida(LocalDateTime momento) {
        if (saida != null) {
            throw new IllegalStateException("A saída deste morador já foi registrada.");
        }
        if (momento.isBefore(entrada)) {
            throw new IllegalArgumentException("A saída não pode ser anterior à entrada.");
        }

        this.saida = momento;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdMorador() {
        return idMorador;
    }

    public void setIdMorador(Long idMorador) {
        this.idMorador = idMorador;
    }

    public LocalDateTime getEntrada() {
        return entrada;
    }

    public void setEntrada(LocalDateTime entrada) {
        this.entrada = entrada;
    }

    public LocalDateTime getSaida() {
        return saida;
    }

    public void setSaida(LocalDateTime saida) {
        this.saida = saida;
    }

}
