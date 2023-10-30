package gui;

import file_database.Database;

import javax.swing.*;
import java.awt.*;

public abstract class ClientList_panel implements Database {
    private static JButton connect = null;
    private static JButton disconnect = null;
    private static MyJList clients_list = null;

    private static JPanel client_panel = null;
    protected static JPanel init() {
        if (client_panel == null) {
            client_panel = new JPanel();
            client_panel.setBackground(new Color(58, 61, 63));
            client_panel.setLayout(new GridBagLayout());

            //inizializza tutti i componenti della gui
            connect = new JButton();
            disconnect = new JButton();
            clients_list = new MyJList();
            MyJScrollPane clients_scroller = new MyJScrollPane(clients_list);
            JTextArea spacer = new JTextArea();

            spacer.setBackground(new Color(58, 61, 63));
            spacer.setFocusable(false);

            connect.setIcon(new ImageIcon(project_path + "images/power_on.png"));
            connect.setRolloverIcon(new ImageIcon(project_path + "images/power_on_sel.png"));
            connect.setPressedIcon(new ImageIcon(project_path + "images/power_on_pres.png"));
            connect.setDisabledIcon(new ImageIcon(project_path + "images/power_on_dis.png"));
            disconnect.setIcon(new ImageIcon(project_path + "images/power_off.png"));
            disconnect.setRolloverIcon(new ImageIcon(project_path + "images/power_off_sel.png"));
            disconnect.setPressedIcon(new ImageIcon(project_path + "images/power_off_pres.png"));
            disconnect.setDisabledIcon(new ImageIcon(project_path + "images/power_off_dis.png"));

            connect.setBorder(null);
            disconnect.setBorder(null);
            spacer.setBorder(null);

            GridBagConstraints c = new GridBagConstraints();

            c.fill = GridBagConstraints.BOTH;
            c.weighty = 0; //i due pulsanti non si ridimensionano
            c.weightx = 0;

            c.gridx = 0;
            c.gridy = 0;
            c.insets = new Insets(0, 0, 5, 5);
            client_panel.add(disconnect, c);

            c.gridx = 2;
            c.gridy = 0;
            c.insets = new Insets(0, 5, 5, 0);
            client_panel.add(connect, c);

            c.weightx = 1; //lo spacer dovrà allungarsi sulle x per permettere ai pulsanti di rimanere delle stesse dimensioni

            c.gridx = 1;
            c.gridy = 0;
            c.insets = new Insets(0, 5, 5, 5);
            client_panel.add(spacer, c);

            c.weighty = 1; //la lista di client dovrà allungarsi sulle y per compensare i pulsanti

            c.gridx = 0;
            c.gridy = 1;
            c.gridwidth = 3;
            c.insets = new Insets(5, 0, 0, 0);
            client_panel.add(clients_scroller, c);

            connect.setEnabled(false);
            disconnect.setEnabled(false);
            clients_scroller.setPreferredSize(new Dimension(0, 0));
        }
        return client_panel;
    }

    public static void setEnabled(boolean enabled) {
        connect.setEnabled(enabled);
        disconnect.setEnabled(enabled);
        clients_list.setEnabled(enabled);
    }

}