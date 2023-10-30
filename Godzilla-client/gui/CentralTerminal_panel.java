package gui;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public abstract class CentralTerminal_panel {
    private static TerminalJTextArea terminal = new TerminalJTextArea();
    private static String spacing = " ";

    private static JPanel terminal_panel = new JPanel();
    private static JPanel programmable_panel = new JPanel();
    private static FullscreenLayeredPane layeredPane = null;

    protected static JLayeredPane init() {
        if (layeredPane == null) {
            layeredPane = new FullscreenLayeredPane();
            MyJScrollPane terminal_scroller = new MyJScrollPane(terminal);

            terminal_scroller.setBackground(new Color(128, 131, 133));

            terminal_panel.setLayout(new GridLayout(1, 1));
            terminal_panel.add(terminal_scroller);

            layeredPane.add_fullscreen(terminal_panel, JLayeredPane.DEFAULT_LAYER);
            layeredPane.add_fullscreen(programmable_panel, JLayeredPane.FRAME_CONTENT_LAYER);
        }
        return layeredPane;
    }

    public static void terminal_write(String txt) {
        terminal.setText(terminal.getText() + txt);
    }

    public static void terminal_new_line() {
        terminal.setText(terminal.getText() + "\n" + spacing);
    }

    public static void terminal_goto_layer(int layer) {
        spacing = " ";
        for (int i = 0; i < layer; i++) {
            spacing += " ";
        }
    }

    public static void terminal_next_layer() {
        spacing += " ";
    }

    public static void terminal_prev_layer() {
        spacing = spacing.substring(0, spacing.length() - 1);

        if (spacing.equals("")) {
            spacing = " ";
        }
    }

    public static String get_terminal_log() {
        return terminal.getText();
    }

    private static class TerminalJTextArea extends JTextArea {
        public TerminalJTextArea() {
            super();

            this.setBackground(Color.BLACK);
            this.setForeground(Color.lightGray);
            this.setCaretColor(Color.WHITE);
            this.setSelectionColor(new Color(180, 180, 180));
            this.setSelectedTextColor(new Color(30, 30, 30));

            this.setEditable(false);

            this.setText(" ======================================== Client Starting " + get_data_time() + " ========================================\n ");
        }

        private static String get_data_time() {
            String pattern = "dd.MM.yyyy - HH:mm.ss";
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            Calendar c = Calendar.getInstance();

            return sdf.format(c.getTime());
        }
    }

}