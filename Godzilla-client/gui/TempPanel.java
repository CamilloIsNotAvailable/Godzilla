package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

abstract public class TempPanel {
    private static JButton ok_button = new JButton();
    private static JButton annulla_button = new JButton();

    public static Vector<JTextArea> input_array = new Vector<>();
    private static StringVectorOperator action = null;

    /*
    * Vector<String> = input txt
    * StrignVectorOperator = action
    * Integer = 0/1,
    *   0, messaggio
    *   1, richiesta di input
     */
    private static Vector<Triple<Vector<String>, StringVectorOperator, Integer>> queue = new Vector<>();
    private static final int MESSAGE = 0;
    private static final int TA_MESSAGE = 1;
    private static final int INPUT = 2;

    private static JPanel temp_panel = null;
    private static boolean visible = false;
    private static int type; // 0 se sta mostrando un messaggio, 1 se sta richiedendo un input
    public static JPanel init() {
        if (temp_panel == null) {
            temp_panel = new JPanel();
            temp_panel.setLayout(new GridBagLayout());
            temp_panel.setBackground(new Color(58, 61, 63));
            temp_panel.setBorder(BorderFactory.createLineBorder(new Color(38, 41, 43)));

            ok_button.setIcon(new ImageIcon("./images/ok.png"));
            ok_button.setPressedIcon(new ImageIcon("./images/ok_pres.png"));
            ok_button.setSelectedIcon(new ImageIcon("./images/ok_sel.png"));
            annulla_button.setIcon(new ImageIcon("./images/cancel.png"));
            annulla_button.setPressedIcon(new ImageIcon("./images/cancel_pres.png"));
            annulla_button.setSelectedIcon(new ImageIcon("./images/cancel_sel.png"));

            ok_button.addActionListener(ok_listener);
            annulla_button.addActionListener(annulla_listener);

            ok_button.setBorder(null);
            annulla_button.setBorder(null);
        }
        return temp_panel;
    }

    private static ActionListener ok_listener = e -> {
        Vector<String> input_txt = new Vector<>(); //copia tutti i testi contenuti nelle input area in questo array
        while (input_array.size() != 0) {
            input_txt.add(input_array.elementAt(0).getText());
            input_array.removeElementAt(0);
        }

        if (action != null) { //se è stata specificata un azione
            if (type == MESSAGE || (type == INPUT && valid_input())) {
                action.input.removeAllElements();
                action.input.addAll(input_txt); //fa partire il codice con tutti gli input ricavati
                new Thread(() -> action.success()).start();
            } else {
                new Thread(() -> action.fail()).start();
            }
        }

        //resetta tutto come nuovo
        reset();
    };

    private static ActionListener annulla_listener = e ->  {
        input_array.removeAllElements(); //rimuove tutti gli input precedenti

        if (action != null) {
            new Thread(() -> action.fail()).start();
        }

        reset(); //resetta il pannello
    };

    public static void show_msg(String msg) {
        show_msg(msg, null, false);
    }

    public static void show_msg(String msg, StringVectorOperator action, boolean two_answer_question) {
        if (!visible) {
            TempPanel.action = action;
            type = MESSAGE;
            NofocusTextArea t = new NofocusTextArea(msg);

            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.NONE;

            //ordina tutti i componenti nel pannello e lo ridimensiona
            c.gridx = 0;
            c.gridy = 0;
            c.gridwidth = 2;
            c.insets = new Insets(10, 10, 5, 10);
            temp_panel.add(t, c);

            if (two_answer_question) { //aggiunge anche il pulsante annulla solo se è una domanda
                c.gridy = 1;
                c.gridwidth = 1;
                c.insets = new Insets(5, 10, 10, 5);
                c.anchor = GridBagConstraints.LAST_LINE_START;
                temp_panel.add(annulla_button, c);
            }

            c.anchor = GridBagConstraints.LAST_LINE_END;
            c.gridx = 1;
            c.gridy = 1;
            c.gridwidth = 1;
            c.insets = new Insets(5, 5, 10, 10);
            temp_panel.add(ok_button, c);

            int min_width = (int) (ok_button.getPreferredSize().getWidth() + annulla_button.getPreferredSize().getWidth() + 30);
            temp_panel.setSize(
                    (t.getPreferredSize().width + 20 > min_width) ? t.getPreferredSize().width + 20 : min_width,
                    (int) (t.getPreferredSize().height + ok_button.getPreferredSize().getHeight() + 30)
            );

            Godzilla_frame.disable_panels();
            Godzilla_frame.recenter_temp_panel();

            temp_panel.setVisible(true);
            temp_panel.updateUI();
            visible = true;
        } else {
            Vector<String> vec = new Vector<>(); //aggiunge alla coda questo messaggio
            vec.add(msg);

            if (two_answer_question) {
                queue.add(new Triple<>(vec, action, TA_MESSAGE));
            } else {
                queue.add(new Triple<>(vec, action, MESSAGE));
            }
        }
    }

    public static void request_string(String txt, StringVectorOperator action) {
        Vector<String> txt_v = new Vector<>();
        txt_v.add(txt);

        if (!visible) {
            type = INPUT;
            request_string(txt_v, action);
        } else {
            queue.add(new Triple<>(txt_v, action, INPUT)); //aggiunge alla coda questa richiesta
            System.out.println(queue.size());
        }
    }

    public static void request_string(Vector<String> txt_vec, StringVectorOperator action) {
        if (!visible) {
            type = INPUT;
            TempPanel.action = action;

            JPanel txt_panel = new JPanel();
            txt_panel.setBackground(new Color(58, 61, 63));
            txt_panel.setLayout(new GridBagLayout());

            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.NONE;
            c.anchor = GridBagConstraints.LAST_LINE_END;

            c.insets = new Insets(0, 0, 0, 10); //le JTextArea della prima riga hanno insets diversi dalle altre

            int max_txt_width = 0;
            for (int i = 0; i < txt_vec.size(); i++) { //genera ed aggiunge a txt_panel tutte le JTextArea
                input_array.add(new FocusTextArea(i));

                JTextArea txt_area = new NofocusTextArea(txt_vec.elementAt(i));
                if (txt_area.getPreferredSize().width > max_txt_width) { //aggiorna la lunghezza massima delle JTextArea contenenti il testo
                    max_txt_width = txt_area.getPreferredSize().width;
                }

                //aggiunge le due JTextArea appena generate a txt_panel
                c.gridx = 0;
                c.gridy = i;
                c.weightx = 1;
                txt_panel.add(txt_area, c);

                c.gridx = 1;
                c.weightx = 0;
                c.insets.right = 0;
                txt_panel.add(input_array.lastElement(), c);

                c.insets = new Insets(10, 0, 0, 10);

            }

            //definisce le misure di txt_panel
            int min_width = ok_button.getPreferredSize().width + annulla_button.getPreferredSize().width + 10;
            txt_panel.setSize(
                    (max_txt_width + FocusTextArea.WIDTH + 10 > min_width) ? max_txt_width + FocusTextArea.WIDTH + 10 : min_width,
                    (FocusTextArea.HEIGHT + 10) * txt_vec.size() - 10
            );

            //aggiunge a temp_panel i due bottoni ok_button, annulla_button ed il pannello txt_panel
            c = new GridBagConstraints();
            c.fill = GridBagConstraints.NONE;

            c.insets = new Insets(10, 10, 5, 10);
            c.gridx = 0;
            c.gridy = 0;
            c.gridwidth = 2;
            temp_panel.add(txt_panel, c);

            c.insets = new Insets(5, 10, 10, 5);
            c.gridy = 1;
            c.gridwidth = 1;
            temp_panel.add(annulla_button, c);

            c.insets = new Insets(5, 5, 10, 10);
            c.gridx = 1;
            c.anchor = GridBagConstraints.LAST_LINE_END;
            temp_panel.add(ok_button, c);

            //imposta le dimensioni di temp_panel e lo rende visibile
            temp_panel.setSize(
                    txt_panel.getWidth() + 20,
                    txt_panel.getHeight() + ok_button.getPreferredSize().height + 30
            );

            temp_panel.updateUI();
            Godzilla_frame.recenter_temp_panel();
            Godzilla_frame.disable_panels();

            temp_panel.setVisible(true);
            visible = true;

            //richiede il focus nella prima input area
            input_array.elementAt(0).requestFocusInWindow();
        } else {
            //aggiunge alla coda questa richiesta
            queue.add(new Triple<>(txt_vec, action, INPUT));
            System.out.println(queue.size());
        }
    }

    private static boolean valid_input() { //controlla che nessun campo di input sia stato lasciato vuoto o con solo uno spazio/a capo
        for (JTextArea txt_area : input_array) {
            String txt = txt_area.getText();

            txt = txt.replaceAll("[ \n]", ""); //rimuove tutti gli spazi e \n

            if (txt.equals("")) {
                return false;
            }
        }
        return true;
    }

    private static void reset() {
        //resetta il pannello e lo rende invisibile
        temp_panel.setVisible(false);
        temp_panel.removeAll();

        visible = false;
        if (queue.size() != 0) {
            Vector<String> txt = queue.elementAt(0).el1;
            StringVectorOperator action = queue.elementAt(0).el2;
            int id = queue.elementAt(0).el3;

            queue.removeElementAt(0);

            if (id == MESSAGE) { //è un messaggio
                show_msg(txt.elementAt(0), action, false);
            } else if (id == TA_MESSAGE) {
                show_msg(txt.elementAt(0), action, true);
            } else { //richiede un input
                    request_string(txt, action);
            }
        } else {
            Godzilla_frame.enable_panels();
        }
    }

    private static class NofocusTextArea extends JTextArea {
        public NofocusTextArea(String txt) {
            super(txt);

            this.setBackground(new Color(58, 61, 63));
            this.setBorder(null);
            this.setFont(new Font("Charter Bd BT", Font.PLAIN, 12));
            this.setForeground(new Color(188, 191,  193));

            this.setFocusable(false);
        }
    }

    private static class FocusTextArea extends JTextArea {
        protected static final int WIDTH  = 150;
        protected static final int HEIGHT = 20;

        private final int INDEX;
        public FocusTextArea(int index) {
            super();

            this.INDEX = index;

            this.setBackground(new Color(108, 111, 113));
            this.setBorder(BorderFactory.createLineBorder(new Color(68, 71, 73)));
            this.setFont(new Font("Arial", Font.BOLD, 14));
            this.setForeground(new Color(218, 221, 223));

            this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
            this.setMinimumSize(this.getPreferredSize());

            this.addKeyListener(enter_list);
        }

        private KeyListener enter_list = new KeyListener() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == 10) { //10 -> enter
                    setText(getText().replaceAll("\n", "")); //rimuove la nuova linea

                    try {
                        input_array.elementAt(INDEX + 1).grabFocus(); //passa il focus all'input successivo
                    } catch (Exception ex) { //se non esiste un input con index > di questo
                        ok_button.doClick(); //simula il tasto "ok"
                    }
                }
            }
            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e) {}

        };

    }

}

class Triple <E1, E2, E3> {
    public E1 el1 = null;
    public E2 el2 = null;
    public E3 el3 = null;

    public Triple(E1 el1, E2 el2, E3 el3) {
        this.el1 = el1;
        this.el2 = el2;
        this.el3 = el3;
    }

    public Triple() {}

}