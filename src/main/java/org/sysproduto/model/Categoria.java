package org.sysproduto.model;

public class Categoria {
    private int id;
    private String nome;
    private String descricao;
    private String setor;

    // construtor sem parametros: permitindo criar a categoria sem informar os dados inicialmente
    public Categoria() {
    }

    /* construtor com parametros: permite criar a categoria ja informando o Id, nome,
    e setor */
    public Categoria(int id, String nome, String descricao, String setor) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.setor = setor;
    }

    // métodos get e setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getSetor() {
        return setor;
    }

    public void setSetor(String setor) {
        this.setor = setor;
    }
}
