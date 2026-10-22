package br.com.graphquest.modelo;

import java.util.Objects;

/**
 * Representa um item ou equipamento no inventário do aventureiro.
 */
public class Item {
    private final String id;
    private final String nome;
    private final String descricao;
    private final TipoItem tipo;
    private final int preco;
    private final int bonusAtaque;
    private final int bonusDefesa;
    private final int bonusVidaMaxima;
    private final int cura;
    private final int quantidade;
    private boolean equipado;

    public Item(String id, String nome, String descricao, TipoItem tipo, int preco,
                int bonusAtaque, int bonusDefesa, int bonusVidaMaxima, int cura, int quantidade) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.tipo = tipo;
        this.preco = preco;
        this.bonusAtaque = bonusAtaque;
        this.bonusDefesa = bonusDefesa;
        this.bonusVidaMaxima = bonusVidaMaxima;
        this.cura = cura;
        this.quantidade = Math.max(1, quantidade);
    }

    public Item(String id, String nome, String descricao, TipoItem tipo, int preco) {
        this(id, nome, descricao, tipo, preco, 0, 0, 0, 0, 1);
    }

    public Item(String id, String nome, String descricao, TipoItem tipo,
                int bonusAtaque, int bonusDefesa, int bonusVidaMaxima, int cura, int preco) {
        this(id, nome, descricao, tipo, preco, bonusAtaque, bonusDefesa, bonusVidaMaxima, cura, 1);
    }

    public String getId() { return id; }
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public TipoItem getTipo() { return tipo; }
    public int getPreco() { return preco; }
    public int getBonusAtaque() { return bonusAtaque; }
    public int getBonusDefesa() { return bonusDefesa; }
    public int getBonusVidaMaxima() { return bonusVidaMaxima; }
    public int getCura() { return cura; }
    public int getQuantidade() { return quantidade; }

    public Item comQuantidade(int novaQuantidade) {
        return new Item(id, nome, descricao, tipo, preco, bonusAtaque, bonusDefesa,
                bonusVidaMaxima, cura, novaQuantidade);
    }

    public boolean isConsumivel() { return tipo == TipoItem.CONSUMIVEL; }
    public boolean isEquipamento() {
        return tipo == TipoItem.ARMA || tipo == TipoItem.ARMADURA || tipo == TipoItem.ACESSORIO;
    }
    public boolean isEquipado() { return equipado; }
    public void setEquipado(boolean equipado) { this.equipado = equipado; }
    public int getBonusVida() { return bonusVidaMaxima; }

    public String getResumoEfeito() {
        StringBuilder sb = new StringBuilder();
        if (cura > 0) sb.append("Recupera ").append(cura).append(" PV. ");
        if (bonusAtaque > 0) sb.append("+").append(bonusAtaque).append(" ATK. ");
        if (bonusDefesa > 0) sb.append("+").append(bonusDefesa).append(" DEF. ");
        if (bonusVidaMaxima > 0) sb.append("+").append(bonusVidaMaxima).append(" PV Máx. ");
        if (sb.isEmpty()) sb.append(descricao);
        return sb.toString().trim();
    }

    public String getEfeitoDescricao() {
        return getResumoEfeito();
    }

    // --- Catálogo Estático de Itens Úteis ---
    public static Item pocaoCura() {
        return new Item("pocao_cura", "Poção de Cura", "Restaura 40 pontos de vida do aventureiro.",
                TipoItem.CONSUMIVEL, 25, 0, 0, 0, 40, 1);
    }

    public static Item pocaoGrande() {
        return new Item("pocao_grande", "Poção Maior de Vida", "Restaura 80 pontos de vida.",
                TipoItem.CONSUMIVEL, 50, 0, 0, 0, 80, 1);
    }

    public static Item bombaArcana() {
        return new Item("bomba_arcana", "Bomba Arcana", "Causa 50 de dano instantâneo ao inimigo em combate.",
                TipoItem.CONSUMIVEL, 35, 0, 0, 0, 0, 1);
    }

    public static Item chaveMistica() {
        return new Item("chave_mistica", "Chave Mística", "Chave mágica capaz de abrir portões trancados.",
                TipoItem.CHAVE, 0, 0, 0, 0, 0, 1);
    }

    public static Item laminaRunica() {
        return new Item("lamina_runica", "Lâmina Rúnica", "Espada afiada imbuída de runas antigas (+8 ATK).",
                TipoItem.ARMA, 140, 8, 0, 0, 0, 1);
    }

    public static Item machadoMasmorra() {
        return new Item("machado_masmorra", "Machado dos Guardiões", "Machado pesado de combate (+14 ATK).",
                TipoItem.ARMA, 220, 14, 0, 0, 0, 1);
    }

    public static Item cotaGuardiao() {
        return new Item("cota_guardiao", "Cota do Guardião", "Armadura de ferro forjada que reduz 5 de dano recebido.",
                TipoItem.ARMADURA, 150, 0, 5, 0, 0, 1);
    }

    public static Item couracaDragao() {
        return new Item("couraca_dragao", "Couraça de Dragão", "Armadura feita de escamas resistentes (+10 DEF, +20 PV).",
                TipoItem.ARMADURA, 260, 0, 10, 20, 0, 1);
    }

    public static Item amuletoAmbar() {
        return new Item("amuleto_ambar", "Amuleto de Âmbar", "Aumenta o ganho de moedas e tesouros em 15%.",
                TipoItem.ACESSORIO, 120, 0, 0, 15, 0, 1);
    }

    public static Item anelVigor() {
        return new Item("anel_vigor", "Anel do Vigor", "Aumenta a vida máxima em 25 pontos (+25 PV).",
                TipoItem.ACESSORIO, 130, 0, 2, 25, 0, 1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return Objects.equals(id, item.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return nome + (quantidade > 1 ? " (x" + quantidade + ")" : "");
    }
}
