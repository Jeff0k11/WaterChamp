package com.example.waterchamp.model;

/**
 * Modelo para representar um grupo de usuários
 */
public class Group {
    private int id;
    private String nome;
    private String descricao;
    private int criadorId;
    private String dataCriacao;
    private int totalMembros;

    public Group(int id, String nome, String descricao, int criadorId, String dataCriacao, int totalMembros) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.criadorId = criadorId;
        this.dataCriacao = dataCriacao;
        this.totalMembros = totalMembros;
    }

    public Group(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public int getCriadorId() {
        return criadorId;
    }

    public String getDataCriacao() {
        return dataCriacao;
    }

    public int getTotalMembros() {
        return totalMembros;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public void setCriadorId(int criadorId) {
        this.criadorId = criadorId;
    }

    public void setDataCriacao(String dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public void setTotalMembros(int totalMembros) {
        this.totalMembros = totalMembros;
    }
}
