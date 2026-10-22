package br.com.graphquest.modelo;

/**
 * Categorias de itens e equipamentos no GraphQuest.
 */
public enum TipoItem {
    CONSUMIVEL("Consumível", "Item de uso único"),
    ARMA("Arma", "Equipamento que aumenta o poder de ataque"),
    ARMADURA("Armadura", "Equipamento que reduz o dano recebido"),
    ACESSORIO("Acessório", "Concede bônus especiais de atributos"),
    CHAVE("Chave", "Desbloqueia portões e passagens trancadas"),
    ARTEFATO("Artefato", "Relíquia lendária da masmorra");

    private final String rotulo;
    private final String descricao;

    TipoItem(String rotulo, String descricao) {
        this.rotulo = rotulo;
        this.descricao = descricao;
    }

    public String getRotulo() {
        return rotulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean isEquipavel() {
        return this == ARMA || this == ARMADURA || this == ACESSORIO;
    }
}
