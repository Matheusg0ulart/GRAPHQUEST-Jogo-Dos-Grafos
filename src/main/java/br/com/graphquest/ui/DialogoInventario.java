package br.com.graphquest.ui;

import br.com.graphquest.audio.GerenciadorAudio;
import br.com.graphquest.jogo.Jogo;
import br.com.graphquest.modelo.Item;
import br.com.graphquest.modelo.TipoItem;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.List;

/**
 * Diálogo de Inventário e Gerenciamento de Equipamentos do GraphQuest.
 * 
 * Permite visualizar consumíveis, armas, armaduras e acessórios,
 * equipar/desequipar itens com atualização imediata de atributos
 * e utilizar consumíveis fora de combate.
 */
public class DialogoInventario extends JDialog {

    private final Jogo jogo;
    private final JPanel painelListaItens;
    private final JLabel lblAtaque;
    private final JLabel lblDefesa;
    private final JLabel lblVida;
    private final JLabel lblMoedas;
    private final JLabel lblArmaEquipada;
    private final JLabel lblArmaduraEquipada;
    private final JLabel lblAcessorioEquipado;

    public DialogoInventario(Window proprietario, Jogo jogo) {
        super(proprietario, "Mochila e Equipamentos do Aventureiro", ModalityType.APPLICATION_MODAL);
        this.jogo = jogo;

        setSize(780, 560);
        setLocationRelativeTo(proprietario);
        setResizable(false);

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(TemaVisual.COR_FUNDO_ESCURECIDO);

        // Cabeçalho
        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.setBackground(TemaVisual.COR_SUPERFICIE);
        cabecalho.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, TemaVisual.COR_BORDA),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)
        ));

        JLabel titulo = new JLabel("🎒 Mochila & Arsenal");
        titulo.setFont(TemaVisual.FONTE_TITULO_GRANDE);
        titulo.setForeground(TemaVisual.COR_DOURADO);

        lblMoedas = new JLabel("Moedas: " + jogo.getMoedas() + " ✦");
        lblMoedas.setFont(TemaVisual.FONTE_SUBTITULO);
        lblMoedas.setForeground(TemaVisual.COR_DOURADO_CLARO);

        cabecalho.add(titulo, BorderLayout.WEST);
        cabecalho.add(lblMoedas, BorderLayout.EAST);
        raiz.add(cabecalho, BorderLayout.NORTH);

        // Centro: Divisão em 2 colunas (Equipamentos/Atributos à esquerda, Lista de Itens à direita)
        JPanel painelCentral = new JPanel(new GridBagLayout());
        painelCentral.setOpaque(false);
        painelCentral.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        GridBagConstraints gbc = new GridBagConstraints();

        // Coluna Esquerda: Equipamentos & Atributos
        JPanel painelEsquerdo = new JPanel();
        painelEsquerdo.setLayout(new BoxLayout(painelEsquerdo, BoxLayout.Y_AXIS));
        painelEsquerdo.setOpaque(false);
        painelEsquerdo.setPreferredSize(new Dimension(280, 420));

        // Card de Atributos
        JPanel cardAtributos = new JPanel(new GridLayout(4, 1, 4, 4));
        cardAtributos.setBackground(TemaVisual.COR_SUPERFICIE_CARD);
        cardAtributos.setBorder(TemaVisual.criarBordaCard());

        JLabel titAtributos = new JLabel("ATRIBUTOS ATUAIS");
        titAtributos.setFont(TemaVisual.FONTE_PEQUENA);
        titAtributos.setForeground(TemaVisual.COR_TEXTO_MUTED);

        lblAtaque = new JLabel("⚔ Ataque: " + jogo.getAtaque());
        lblAtaque.setFont(TemaVisual.FONTE_NEGRITO);
        lblAtaque.setForeground(new Color(248, 113, 113));

        lblDefesa = new JLabel("🛡 Defesa: " + jogo.getDefesa());
        lblDefesa.setFont(TemaVisual.FONTE_NEGRITO);
        lblDefesa.setForeground(new Color(96, 165, 250));

        lblVida = new JLabel("♥ Vida: " + jogo.getVida() + "/" + jogo.getVidaMaxima());
        lblVida.setFont(TemaVisual.FONTE_NEGRITO);
        lblVida.setForeground(TemaVisual.COR_ESMERALDA);

        cardAtributos.add(titAtributos);
        cardAtributos.add(lblAtaque);
        cardAtributos.add(lblDefesa);
        cardAtributos.add(lblVida);

        // Card de Equipamentos
        JPanel cardEquip = new JPanel();
        cardEquip.setLayout(new BoxLayout(cardEquip, BoxLayout.Y_AXIS));
        cardEquip.setBackground(TemaVisual.COR_SUPERFICIE_CARD);
        cardEquip.setBorder(TemaVisual.criarBordaCard());

        JLabel titEquip = new JLabel("EQUIPAMENTOS EM USO");
        titEquip.setFont(TemaVisual.FONTE_PEQUENA);
        titEquip.setForeground(TemaVisual.COR_TEXTO_MUTED);
        titEquip.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblArmaEquipada = new JLabel("Arma: Nenhuma");
        lblArmaEquipada.setFont(TemaVisual.FONTE_NORMAL);
        lblArmaEquipada.setForeground(TemaVisual.COR_TEXTO_BRANCO);
        lblArmaEquipada.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblArmaduraEquipada = new JLabel("Armadura: Nenhuma");
        lblArmaduraEquipada.setFont(TemaVisual.FONTE_NORMAL);
        lblArmaduraEquipada.setForeground(TemaVisual.COR_TEXTO_BRANCO);
        lblArmaduraEquipada.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblAcessorioEquipado = new JLabel("Acessório: Nenhum");
        lblAcessorioEquipado.setFont(TemaVisual.FONTE_NORMAL);
        lblAcessorioEquipado.setForeground(TemaVisual.COR_TEXTO_BRANCO);
        lblAcessorioEquipado.setAlignmentX(Component.LEFT_ALIGNMENT);

        cardEquip.add(titEquip);
        cardEquip.add(Box.createVerticalStrut(8));
        cardEquip.add(lblArmaEquipada);
        cardEquip.add(Box.createVerticalStrut(6));
        cardEquip.add(lblArmaduraEquipada);
        cardEquip.add(Box.createVerticalStrut(6));
        cardEquip.add(lblAcessorioEquipado);

        painelEsquerdo.add(cardAtributos);
        painelEsquerdo.add(Box.createVerticalStrut(10));
        painelEsquerdo.add(cardEquip);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.38;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        painelCentral.add(painelEsquerdo, gbc);

        // Coluna Direita: Itens da Mochila
        painelListaItens = new JPanel();
        painelListaItens.setLayout(new BoxLayout(painelListaItens, BoxLayout.Y_AXIS));
        painelListaItens.setBackground(TemaVisual.COR_SUPERFICIE);
        painelListaItens.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollItens = new JScrollPane(painelListaItens);
        scrollItens.setBorder(BorderFactory.createLineBorder(TemaVisual.COR_BORDA));
        scrollItens.getVerticalScrollBar().setUnitIncrement(12);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.62;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 10, 0, 0);
        painelCentral.add(scrollItens, gbc);

        raiz.add(painelCentral, BorderLayout.CENTER);

        // Rodapé
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 10));
        rodape.setBackground(TemaVisual.COR_SUPERFICIE);
        rodape.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, TemaVisual.COR_BORDA));

        JButton btnFechar = TemaVisual.criarBotao("Fechar Mochila", new Color(45, 55, 72), new Color(60, 75, 95));
        btnFechar.addActionListener(e -> dispose());
        rodape.add(btnFechar);
        raiz.add(rodape, BorderLayout.SOUTH);

        setContentPane(raiz);
        atualizarPainel();
    }

    private void atualizarPainel() {
        // Atualiza atributos
        lblAtaque.setText("⚔ Ataque: " + jogo.getAtaque());
        lblDefesa.setText("🛡 Defesa: " + jogo.getDefesa());
        lblVida.setText("♥ Vida: " + jogo.getVida() + "/" + jogo.getVidaMaxima());
        lblMoedas.setText("Moedas: " + jogo.getMoedas() + " ✦");

        // Atualiza slots de equipamentos
        Item arma = jogo.getArmaEquipada();
        lblArmaEquipada.setText("Arma: " + (arma != null ? arma.getNome() + " (+" + arma.getBonusAtaque() + " ATK)" : "Nenhuma"));
        lblArmaEquipada.setForeground(arma != null ? TemaVisual.COR_DOURADO_CLARO : TemaVisual.COR_TEXTO_MUTED);

        Item armadura = jogo.getArmaduraEquipada();
        lblArmaduraEquipada.setText("Armadura: " + (armadura != null ? armadura.getNome() + " (+" + armadura.getBonusDefesa() + " DEF)" : "Nenhuma"));
        lblArmaduraEquipada.setForeground(armadura != null ? new Color(96, 165, 250) : TemaVisual.COR_TEXTO_MUTED);

        Item acessorio = jogo.getAcessorioEquipado();
        lblAcessorioEquipado.setText("Acessório: " + (acessorio != null ? acessorio.getNome() + " (+" + acessorio.getBonusVida() + " PV)" : "Nenhum"));
        lblAcessorioEquipado.setForeground(acessorio != null ? TemaVisual.COR_ROXO_ARCANO : TemaVisual.COR_TEXTO_MUTED);

        // Reconstrói lista de itens
        painelListaItens.removeAll();
        List<Item> itens = jogo.getItensInventario();

        if (itens.isEmpty()) {
            JLabel vazio = new JLabel("Sua mochila está vazia no momento.");
            vazio.setFont(TemaVisual.FONTE_NORMAL);
            vazio.setForeground(TemaVisual.COR_TEXTO_CINZA);
            vazio.setAlignmentX(Component.CENTER_ALIGNMENT);
            painelListaItens.add(Box.createVerticalStrut(40));
            painelListaItens.add(vazio);
        } else {
            for (Item item : itens) {
                painelListaItens.add(criarCardItem(item));
                painelListaItens.add(Box.createVerticalStrut(6));
            }
        }

        painelListaItens.revalidate();
        painelListaItens.repaint();
    }

    private JPanel criarCardItem(Item item) {
        JPanel card = new JPanel(new BorderLayout(8, 0));
        card.setBackground(TemaVisual.COR_SUPERFICIE_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(TemaVisual.COR_BORDA, 1, true),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));

        // Detalhes do item
        JPanel painelInfo = new JPanel();
        painelInfo.setLayout(new BoxLayout(painelInfo, BoxLayout.Y_AXIS));
        painelInfo.setOpaque(false);

        String tagTipo = switch (item.getTipo()) {
            case ARMA -> "[ARMA] ";
            case ARMADURA -> "[ARMADURA] ";
            case ACESSORIO -> "[ACESSÓRIO] ";
            case CONSUMIVEL -> "[CONSUMÍVEL] ";
            case CHAVE -> "[CHAVE] ";
            case ARTEFATO -> "[ARTEFATO] ";
            default -> "[ITEM] ";
        };

        JLabel lblNome = new JLabel(tagTipo + item.getNome());
        lblNome.setFont(TemaVisual.FONTE_NEGRITO);
        lblNome.setForeground(item.isEquipado() ? TemaVisual.COR_ESMERALDA : TemaVisual.COR_TEXTO_BRANCO);

        JLabel lblDesc = new JLabel(item.getDescricao() + " • " + item.getEfeitoDescricao());
        lblDesc.setFont(TemaVisual.FONTE_PEQUENA);
        lblDesc.setForeground(TemaVisual.COR_TEXTO_CINZA);

        painelInfo.add(lblNome);
        painelInfo.add(Box.createVerticalStrut(2));
        painelInfo.add(lblDesc);
        card.add(painelInfo, BorderLayout.CENTER);

        // Botões de ação
        JPanel painelAcao = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        painelAcao.setOpaque(false);

        if (item.getTipo().isEquipavel()) {
            if (item.isEquipado()) {
                JButton btnDesequipar = TemaVisual.criarBotao("Desequipar", new Color(80, 50, 50), new Color(110, 60, 60));
                btnDesequipar.setFont(TemaVisual.FONTE_PEQUENA);
                btnDesequipar.addActionListener(e -> {
                    jogo.desequiparItem(item);
                    atualizarPainel();
                });
                painelAcao.add(btnDesequipar);
            } else {
                JButton btnEquipar = TemaVisual.criarBotao("Equipar", new Color(30, 80, 60), new Color(40, 110, 80));
                btnEquipar.setFont(TemaVisual.FONTE_PEQUENA);
                btnEquipar.addActionListener(e -> {
                    jogo.equiparItem(item);
                    atualizarPainel();
                });
                painelAcao.add(btnEquipar);
            }
        } else if (item.getTipo() == TipoItem.CONSUMIVEL) {
            JButton btnUsar = TemaVisual.criarBotao("Usar", new Color(30, 64, 110), new Color(40, 85, 145));
            btnUsar.setFont(TemaVisual.FONTE_PEQUENA);
            btnUsar.addActionListener(e -> {
                boolean usou = jogo.usarItem(item);
                if (usou) {
                    GerenciadorAudio.tocarPocao();
                    atualizarPainel();
                } else {
                    JOptionPane.showMessageDialog(this, "Não foi possível utilizar este item agora.", "Aviso", JOptionPane.WARNING_MESSAGE);
                }
            });
            painelAcao.add(btnUsar);
        }

        card.add(painelAcao, BorderLayout.EAST);
        return card;
    }
}
