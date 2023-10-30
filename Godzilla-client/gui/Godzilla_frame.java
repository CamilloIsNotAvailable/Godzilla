package gui;

import network.Connection;
import network.Server;
import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Vector;

public abstract class Godzilla_frame {
    //tutti i pannelli da aggiungere al frame
    private static JPanel server_list = null;
    private static JPanel client_list = null;
    private static JPanel button_topbar = null;
    private static JLayeredPane central_terminal = null;
    private static JPanel temp_panel = null;

    //ricorda quali pannelli erano attivi prima di aprire una temp window in modo da riattivarli una volta chiusa
    protected static final int SERVER_LIST = 0;
    protected static final int CLIENT_LIST = 1;
    protected static final int BUTTON_TOPBAR = 2;

    private static boolean[] active = new boolean[]{true, false, false};

    //permette a tutti di comunicare con il server
    protected static Server connected_server = null;

    private static JFrame godzilla_frame = null;

    public static JFrame init() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException { //inizializza la schermata e ritorna il JFrame
        if (godzilla_frame == null) {
            godzilla_frame = new JFrame("Godzilla - Client");
            godzilla_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            godzilla_frame.setMinimumSize(new Dimension(900, 500));

            //inizializza tutti i pannelli che formeranno la gui principale
            server_list = ServerList_panel.init();
            client_list = ClientList_panel.init();
            button_topbar = ButtonTopBar_panel.init();
            central_terminal = CentralTerminal_panel.init();
            temp_panel = TempPanel.init();

            //inizializza la gui principale (tutti i pannelli tranne Temp Panels)
            JPanel content_panel = new JPanel();
            content_panel.setBackground(new Color(58, 61, 63));
            content_panel.setLayout(new GridBagLayout());

            GridBagConstraints c = new GridBagConstraints();

            c.fill = GridBagConstraints.BOTH;
            c.weightx = 0.22; //i due pannelli sulla sinistra, devono essere più corti rispetto a quelli sulla destra

            c.gridx = 0;
            c.gridy = 2;
            c.gridheight = 1;
            c.weighty = 0.4; //deve compensare per selection panel
            c.insets = new Insets(5, 10, 10, 5);
            content_panel.add(server_list, c);

            c.gridx = 0;
            c.gridy = 0;
            c.gridheight = 2;
            c.weighty = 0.6; //selection panel deve essere un po' più alto rispetto a connection panel
            c.insets = new Insets(10, 10, 5, 5);
            content_panel.add(client_list, c);

            c.weightx = 0.78; //i due pannelli sulla destra, devono essere più lunghi e conpensare i due pannelli sulla sinistra

            c.gridx = 1;
            c.gridy = 0;
            c.gridheight = 1;
            c.weighty = 0; //la top bar non viene ridimensionata per le y
            c.insets = new Insets(10, 5, 5, 10);
            content_panel.add(button_topbar, c);

            c.gridx = 1;
            c.gridy = 1;
            c.weighty = 1; //per compensare la top bar
            c.insets = new Insets(5, 5, 10, 10);
            c.gridheight = 2;
            content_panel.add(central_terminal, c);

            FullscreenLayeredPane lp = new FullscreenLayeredPane();
            content_panel.setBounds(0, 0, 900, 663);
            lp.add_fullscreen(content_panel, JLayeredPane.DEFAULT_LAYER);
            lp.add(temp_panel, JLayeredPane.POPUP_LAYER);

            godzilla_frame.setLayeredPane(lp);

            //mantiene TempPanel sempre al centro del frame
            godzilla_frame.addComponentListener(new ComponentListener() {
                @Override
                public void componentMoved(ComponentEvent e) {}
                @Override
                public void componentShown(ComponentEvent e) {}
                @Override
                public void componentHidden(ComponentEvent e) {}

                @Override
                public void componentResized(ComponentEvent e) {
                    recenter_temp_panel();
                }
            });
        }
        return godzilla_frame;
    }

    protected static void disable_panels() { //disabilita tutti i pannelli quando si apre TempPanel
        ServerList_panel.setEnabled(false);
        ClientList_panel.setEnabled(false);
        ButtonTopBar_panel.setEnabled(false);
    }

    protected static void enable_panels() { //riattiva i pannelli una volta chiusa TempPanel
        ServerList_panel.setEnabled(active[SERVER_LIST]);
        ClientList_panel.setEnabled(active[CLIENT_LIST]);
        ButtonTopBar_panel.setEnabled(active[BUTTON_TOPBAR]);
    }

    protected static void recenter_temp_panel() {
        temp_panel.setLocation(
                godzilla_frame.getWidth() / 2 - temp_panel.getWidth() / 2,
                godzilla_frame.getHeight() / 2 - temp_panel.getHeight() / 2
        );
    }
}

class MyJScrollPane extends JScrollPane {
    public MyJScrollPane(Component c) {
        super(c);

        this.setBorder(BorderFactory.createLineBorder(new Color(72, 74, 75)));
    }

    @Override
    public JScrollBar createVerticalScrollBar() {
        JScrollBar scrollBar = super.createVerticalScrollBar();

        scrollBar.setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(78, 81, 83);
                this.thumbDarkShadowColor = new Color(58, 61, 63);
                this.thumbHighlightColor = new Color(108, 111, 113);
            }

            class null_button extends JButton {
                public null_button() {
                    super();
                    this.setPreferredSize(new Dimension(0, 0));
                }
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return new null_button();
            }
            @Override
            protected JButton createIncreaseButton(int orientation) {
                return new null_button();
            }
        });

        scrollBar.setBackground(new Color(128, 131, 133));
        scrollBar.setBorder(BorderFactory.createLineBorder(new Color(72, 74, 75)));

        return scrollBar;
    }

    @Override
    public JScrollBar createHorizontalScrollBar() {
        JScrollBar scrollBar = super.createHorizontalScrollBar();

        scrollBar.setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(78, 81, 83);
                this.thumbDarkShadowColor = new Color(58, 61, 63);
                this.thumbHighlightColor = new Color(108, 111, 113);
            }

            class null_button extends JButton {
                public null_button() {
                    super();
                    this.setPreferredSize(new Dimension(0, 0));
                }
            }

            @Override
            protected JButton createDecreaseButton(int orientation) { return new null_button(); }
            @Override
            protected JButton createIncreaseButton(int orientation) { return new null_button(); }

        });

        scrollBar.setBackground(new Color(128, 131, 133));
        scrollBar.setBorder(BorderFactory.createLineBorder(new Color(72, 74, 75)));

        return scrollBar;
    }
}

/*
* utilizzando un estensione di JList viene più semplice ma aggiungendo e rimuovendo elementi dalla lista in modo dinamico può provocare problemi grafici
* dove la lista viene mostrata vuota finché non le si dà un nuovo update, di conseguenza ho creato la mia versione di JList utilizzando varie JTextArea
* e partendo da un JPanel.
* Non so bene da che cosa sia dovuto il problema con JList ma sembra essere risolto utilizzando la mia versione
 */
class MyJList extends JPanel {
    private Vector<ListCell> list_elements = new Vector<>();
    private int selected_index = -1;

    private JTextArea filler = new JTextArea();
    private GridBagConstraints c = new GridBagConstraints();
    private Constructor popupMenu = null;

    public MyJList() {
        super();
        this.setLayout(new GridBagLayout());

        this.setForeground(new Color(44, 46, 47));
        this.setBackground(new Color(98, 101, 103));
        this.setFont(new Font("custom_list", Font.BOLD, 11));

        filler.setBackground(this.getBackground());
        filler.setFocusable(false);
        filler.setCursor(null);

        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;
        this.add(filler, c);

        c.weighty = 0;
    }

    public void set_popup(Class PopupMenu) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        this.popupMenu = PopupMenu.getDeclaredConstructor(String.class, MyJList.class);

        for (int i = 0; i < list_elements.size() - 1; i++) { //viene saltato il filler, che si trova sempre all'ultima posizione
            list_elements.elementAt(i).setComponentPopupMenu((JPopupMenu) this.popupMenu.newInstance(list_elements.elementAt(i).getText(), this));
        }
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);

        filler.setPreferredSize(new Dimension(width, 0));
    }

    public void add(String name) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        list_elements.add(new ListCell(name, this, list_elements.size()));
        if (this.popupMenu != null) {
            list_elements.lastElement().setComponentPopupMenu((JPopupMenu) this.popupMenu.newInstance(name, this));
        }

        //rimuove il filler
        this.remove(filler);

        //aggiunge la nuova ListCell
        this.add(list_elements.lastElement(), c);

        //aggiunge il filler
        this.c.gridy += 1;
        this.c.weighty = 1;
        this.add(filler, c);

        //resetta this.c
        this.c.weighty = 0;

        //mostra il pulsante in più
        this.updateUI();
    }

    public void remove(String name) {
        ListCell name_cell = null;
        for (ListCell cell : list_elements) {
            if (cell.getText().equals(name)) {
                name_cell = cell;
                break;
            }
        }

        if (name_cell != null) { //se è stato trovato una cella con quella scritta
            list_elements.remove(name_cell);
            this.remove(name_cell);

            if (name_cell.my_index == selected_index) { //se era selezionata in questo momento
                selected_index = -1;
            }
        } else {
            throw new RuntimeException("impossibile eliminare l'elemento con nome " + name + ", non è stato trovato");
        }
    }

    public void rename_element(String old_name, String new_name) {
        for (ListCell cell : list_elements) {
            if (cell.getText().equals(old_name)) {
                cell.setText(new_name);
                break;
            }
        }
    }

    public String getSelectedValue() {
        try {
            return list_elements.elementAt(selected_index).getText();
        } catch (Exception e) { //non è selezionato nessun elemento
            return "";
        }
    }

    class ListCell extends JTextArea {
        private static final Color STD_BACKGROUND = new Color(98, 101, 103);
        private static final Color SEL_BACKGROUND = new Color(116, 121, 125);
        private static final Color SEL_BORDER = new Color(72, 74, 75);

        private final MyJList PARENT_LIST;
        private int my_index;

        public ListCell(String text, MyJList list, int index) {
            super(text);
            this.PARENT_LIST = list;
            this.my_index = index;

            //imposta tutti i colori
            this.setForeground(new Color(44, 46, 47));
            this.setBackground(STD_BACKGROUND);
            this.setFont(new Font("custom_list", Font.BOLD, 11));
            this.setBorder(null);

            this.setEditable(false);
            this.setCaretColor(STD_BACKGROUND);
            this.setCursor(null);

            this.addKeyListener(key_l);
            this.addMouseListener(mouse_l);
        }

        private KeyListener key_l = new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}
            @Override
            public void keyPressed(KeyEvent e) {}

            @Override
            public void keyReleased(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case 40: //freccia in basso
                        try {
                            PARENT_LIST.list_elements.elementAt(my_index + 1).set_selected();
                            PARENT_LIST.list_elements.elementAt(my_index + 1).requestFocus();
                        } catch (Exception ex) {} //se non esiste un elemento ad index my_index + 1
                        break;

                    case 38: //freccia in alto
                        try {
                            PARENT_LIST.list_elements.elementAt(my_index - 1).set_selected();
                            PARENT_LIST.list_elements.elementAt(my_index - 1).requestFocus();
                        } catch (Exception ex) {} //se non esiste un elemento ad index my_index - 1
                        break;

                    case 27: //esc
                        unselect();
                        break;

                    case 10: //invio, si collega a questo server
                        Connection.start_connection_with(getText());
                        break;
                }
            }
        };

        private MouseListener mouse_l = new MouseListener() {
            @Override
            public void mouseEntered(MouseEvent e) {}
            @Override
            public void mouseExited(MouseEvent e) {}
            @Override
            public void mouseReleased(MouseEvent e) {}
            @Override
            public void mouseClicked(MouseEvent e) {}

            @Override
            public void mousePressed(MouseEvent e) {
                set_selected();
            }
        };

        public void set_selected() {
            //deseleziona la casella selezionata in precedenza, se ne era selezionata una
            if (PARENT_LIST.selected_index != -1) {
                PARENT_LIST.list_elements.elementAt(PARENT_LIST.selected_index).unselect();
            }

            //imposta questa JTextArea come selezionata
            setBackground(SEL_BACKGROUND);
            setBorder(BorderFactory.createLineBorder(SEL_BORDER));
            setCaretColor(SEL_BACKGROUND);
            setSelectionColor(SEL_BACKGROUND);

            PARENT_LIST.selected_index = my_index;
        }

        public void unselect() {
            setBackground(STD_BACKGROUND);
            setBorder(null);
            setCaretColor(STD_BACKGROUND);
            setSelectionColor(STD_BACKGROUND);

            PARENT_LIST.selected_index = -1;
        }

    }
}

class FullscreenLayeredPane extends JLayeredPane { //permette di ridimensionare componenti in modo che abbiamo sempre la sua stessa dimensione
    Vector<Component> resizable = new Vector<>();
    public FullscreenLayeredPane() {
        super();
        this.setBackground(new Color(58, 61, 63));
    }

    @Override
    public void setBounds(int x, int y, int width, int height) { //in questo modo si elimina il delay che si avrebbe utilizzando un component listener
        super.setBounds(x, y, width, height);

        for (Component cmp : resizable) {
            cmp.setBounds(0, 0, width, height);
        }
    }

    public void add_fullscreen(Component comp, int index) {
        super.add(comp, index);
        resizable.add(comp);
    }
}