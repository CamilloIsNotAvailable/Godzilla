package gui;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Vector;

public abstract class Server_frame {
    public static String project_path;

    private static JFrame frame = null;

    private static JPanel terminal_p;
    private static JPanel buttons_p;
    private static JPanel clients_p;

    public static void init() {
        project_path = Server_frame.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        System.out.println(project_path);

        //inizializza la grafica
        frame = new JFrame();

        FullScreen_layered_pane layeredPane = new FullScreen_layered_pane(); //permette di aggiungere oggetti in full screen nel layered pane
        layeredPane.setBackground(new Color(58, 61, 63));
        frame.setLayeredPane(layeredPane);

        //crea il pannello principale e lo aggiunge al frame
        JPanel main_panel = new JPanel();
        main_panel.setBackground(new Color(58, 61, 63));
        main_panel.setLayout(new GridBagLayout());

        layeredPane.add(main_panel, JLayeredPane.FRAME_CONTENT_LAYER, true);

        //inizializza i pannelli che formano la schermata principale
        terminal_p = Terminal_panel.init();
        buttons_p = Buttons_panel.init();
        clients_p = Clients_panel.init();

        //aggiunge i pannelli al main_panel
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;

        c.weighty = 1;
        c.weightx = 0;
        c.gridy = 0;
        c.gridx = 0;
        c.gridheight = 2;
        c.insets = new Insets(10, 10, 10, 10);
        main_panel.add(clients_p, c);

        c.weightx = 1;
        c.gridx = 1;
        c.gridy = 1;
        c.gridheight = 1;
        c.insets = new Insets(10, 0,10,10);
        main_panel.add(terminal_p, c);

        c.weighty = 0;
        c.gridy = 0;
        c.insets = new Insets(10, 0, 0, 10);
        main_panel.add(buttons_p, c);

        //mostra il frame
        frame.setBounds(100, 100, 200, 200);
        frame.setVisible(true);
    }
}

abstract class Terminal_panel {
    private static JPanel panel = null;

    private static JTextArea terminal = new JTextArea();
    public static JPanel init() {
        if (panel == null) {
            panel = new JPanel();
            panel.setLayout(new GridLayout(1, 1));

            MyScrollPane scrollPane = new MyScrollPane(terminal);

            terminal.setBackground(Color.BLACK);
            terminal.setForeground(Color.lightGray);
            terminal.setSelectionColor(new Color(180, 180,180));
            terminal.setSelectedTextColor(new Color(30, 30, 30));

            terminal.setEditable(false);
            terminal.setText(" ======================================== Client Starting " + get_data_time() + " ========================================\n");

            panel.add(scrollPane);
        }

        return panel;
    }

    public static void print_txt(String msg) {
        terminal.setText(terminal.getText() + msg);
    }

    private static String get_data_time() {
        String pattern = "dd.MM.yyyy - HH:mm.ss";
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        Calendar c = Calendar.getInstance();

        return sdf.format(c.getTime());
    }

}
abstract class Buttons_panel {
    private static JPanel buttons_list;
    private static JScrollPane buttons_scroller;

    private static JPanel buttons_panel = null;
    public static JPanel init() {
        if (buttons_list == null) {
            buttons_panel = new JPanel();
            buttons_panel.setLayout(new GridBagLayout());

            //inizializza tutti i componenti della gui
            JButton right_shift = new JButton();
            JButton left_shift = new JButton();
            buttons_list = new JPanel();
            buttons_scroller = new JScrollPane(buttons_list, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

            right_shift.setIcon(new ImageIcon(Server_frame.project_path + "images/right_arrow.png"));
            right_shift.setRolloverIcon(new ImageIcon(Server_frame.project_path + "images/right_arrow_sel.png"));
            right_shift.setPressedIcon(new ImageIcon(Server_frame.project_path + "images/right_arrow_pres.png"));
            left_shift.setIcon(new ImageIcon(Server_frame.project_path + "images/left_arrow.png"));
            left_shift.setRolloverIcon(new ImageIcon(Server_frame.project_path + "images/left_arrow_sel.png"));
            left_shift.setPressedIcon(new ImageIcon(Server_frame.project_path + "images/left_arrow_pres.png"));

            right_shift.setBorder(null);
            left_shift.setBorder(null);
            buttons_scroller.setBorder(null);

            right_shift.addActionListener(right_shift_listener);
            left_shift.addActionListener(left_shift_listener);

            buttons_list.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));
            buttons_list.setBackground(new Color(58, 61, 63));

            //aggiunge tutti i componenti al pannello organizzandoli nella griglia
            GridBagConstraints c = new GridBagConstraints();

            c.fill = GridBagConstraints.BOTH;
            c.gridy = 0;
            c.weightx = 0; //i due pulsanti non vengono ridimensionati

            c.gridx = 0;
            buttons_panel.add(left_shift, c);

            c.gridx = 2;
            buttons_panel.add(right_shift, c);

            c.weightx = 1;

            c.gridx = 1;
            buttons_panel.add(buttons_scroller, c);

            buttons_panel.setPreferredSize(new Dimension(0, 30));
        }
        return buttons_panel;
    }

    public static void setEnabled(boolean enabled) {
        buttons_list.setEnabled(enabled);
        for (Component c : buttons_list.getComponents()) { //disabilita tutti i bottoni registrati
            c.setEnabled(enabled);
        }
    }

    private static ActionListener left_shift_listener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            buttons_scroller.getHorizontalScrollBar().setValue(
                    buttons_scroller.getHorizontalScrollBar().getValue() - 30
            );
        }
    };

    private static ActionListener right_shift_listener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            buttons_scroller.getHorizontalScrollBar().setValue(
                    buttons_scroller.getHorizontalScrollBar().getValue() + 30
            );
        }
    };
}

abstract class Clients_panel {
    private static JPanel panel = null;

    private static MyList list = new MyList();

    public static JPanel init() {
        if (panel == null) {
            panel = new JPanel();
            panel.setLayout(new GridLayout(1, 1));

            MyScrollPane scrollPane = new MyScrollPane(list);
            scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

            panel.add(scrollPane);
            panel.setPreferredSize(new Dimension(200, 0));
        }

        return panel;
    }
}

class MyScrollPane extends JScrollPane { //imposta la grafica
    public MyScrollPane(Component c) {
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
 * utilizzando un estensione di JList viene più semplice ma aggiungere e rimuovere elementi dalla lista in modo dinamico può provocare problemi grafici
 * dove la lista viene mostrata vuota finché non le si dà un nuovo update, di conseguenza ho creato la mia versione di JList utilizzando varie JTextArea
 * e partendo da un JPanel.
 * Non so bene da che cosa sia dovuto il problema con JList ma sembra essere risolto utilizzando la mia versione
 */
class MyList extends JPanel {
    private Vector<ListCell> list_elements = new Vector<>();
    private int selected_index = -1;

    private JTextArea filler = new JTextArea();
    private GridBagConstraints c = new GridBagConstraints();
    private Constructor popupMenu = null;

    public MyList() {
        super();
        this.setLayout(new GridBagLayout());

        this.setForeground(new Color(44, 46, 47));
        this.setBackground(new Color(98, 101, 103));
        this.setFont(new Font("custom_list", Font.BOLD, 11));

        filler.setBackground(this.getBackground());
        filler.setPreferredSize(new Dimension(0, 0));
        filler.setFocusable(false);
        filler.setCursor(null);

        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;
        c.weightx = 1;
        this.add(filler, c);

        c.weighty = 0;
    }

    public void set_popup(Class PopupMenu) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        this.popupMenu = PopupMenu.getDeclaredConstructor(String.class, MyList.class);

        for (int i = 0; i < list_elements.size() - 1; i++) { //viene saltato il filler, che si trova sempre all'ultima posizione
            list_elements.elementAt(i).setComponentPopupMenu((JPopupMenu) this.popupMenu.newInstance(list_elements.elementAt(i).getText(), this));
        }
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

        private final MyList PARENT_LIST;
        private int my_index;

        public ListCell(String text, MyList list, int index) {
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

                    case 27: //deseleziona
                        unselect();
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

class FullScreen_layered_pane extends JLayeredPane {
    private Vector<Component> full_screen_components = new Vector<>();
    public Component add(Component comp, int index, boolean full_screen) {
        if (full_screen) {
            full_screen_components.add(comp);
        }

        return super.add(comp, index);
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);

        for (Component c : full_screen_components) {
            c.setBounds(0, 0, width, height);
        }
    }
}