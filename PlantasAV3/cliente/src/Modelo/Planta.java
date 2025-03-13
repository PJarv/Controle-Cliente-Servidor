package Modelo;

import java.io.Serializable;

public class Planta implements Serializable {
    private static final long serialVersionUID = 1L;
    private String identificacao;
    private String nome;

    public Planta(String identificacao, String nome) {
        this.identificacao = identificacao;
        this.nome = nome;
    }

    public String getIdentificacao() {
        return identificacao;
    }

    public String getNome() {
        return nome;
    }

    @Override
    public String toString() {
        return nome + " (" + identificacao + ")";
    }
}