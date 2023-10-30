package gui;

import file_database.Database;
import network.Connection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.util.Vector;

public abstract class ServerList_panel implements Database {

    private static JButton connect;
    private static JButton disconnect;
    private static JButton add_server;
    private static MyJList server_list; //rispetto a JList viene modificata la grafica ed inserito un popup per rinominare ed eliminare server dalla lista

    private static JPanel serverL_panel = null;
    protected static JPanel init() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        if (serverL_panel == null) {
            serverL_panel = new JPanel();
            serverL_panel.setBackground(new Color(58, 61, 63));
            serverL_panel.setLayout(new GridBagLayout());

            connect = new JButton();
            disconnect = new JButton();
            add_server = new JButton();
            server_list = new MyJList();
            MyJScrollPane server_scroller = new MyJScrollPane(server_list); //rispetto a JScrollPane viene modificata la grafica
            JTextArea filler = new JTextArea();

            server_scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

            //aggiunge i popup menu ai componenti della lista
            server_list.set_popup(CellPopupMenu.class);

            //inizializza tutti i componenti della gui
            filler.setBackground(new Color(58, 61, 63));
            filler.setFocusable(false);

            connect.setBorder(null);
            disconnect.setBorder(null);
            add_server.setBorder(null);
            filler.setBorder(null);

            connect.setIcon(new ImageIcon(project_path + "images/power_on.png"));
            connect.setRolloverIcon(new ImageIcon(project_path + "images/power_on_sel.png"));
            connect.setPressedIcon(new ImageIcon(project_path + "images/power_on_pres.png"));
            connect.setDisabledIcon(new ImageIcon(project_path + "images/power_on_dis.png"));
            disconnect.setIcon(new ImageIcon(project_path + "images/power_off.png"));
            disconnect.setRolloverIcon(new ImageIcon(project_path + "images/power_off_sel.png"));
            disconnect.setPressedIcon(new ImageIcon(project_path + "images/power_off_pres.png"));
            disconnect.setDisabledIcon(new ImageIcon(project_path + "images/power_off_dis.png"));
            add_server.setIcon(new ImageIcon(project_path + "images/add_server.png"));
            add_server.setRolloverIcon(new ImageIcon(project_path + "images/add_server_sel.png"));
            add_server.setPressedIcon(new ImageIcon(project_path + "images/add_server_pres.png"));
            add_server.setDisabledIcon(new ImageIcon(project_path + "images/add_server_dis.png"));

            connect.setPreferredSize(new Dimension(30, 30));
            disconnect.setPreferredSize(new Dimension(30, 30));
            add_server.setPreferredSize(new Dimension(95, 30));

            connect.addActionListener(connect_listener);
            disconnect.addActionListener(disconnect_listener);
            add_server.addActionListener(add_server_listener);

            //aggiunge tutti i componenti al pannello organizzando la gui
            GridBagConstraints c = new GridBagConstraints();

            c.fill = GridBagConstraints.BOTH;

            c.weightx = 0; //i tre pulsanti per connettersi, disconnettersi, aggiungere un server non si allungano ne sulle x ne sulle y
            c.weighty = 0;

            c.gridx = 0;
            c.gridy = 0;
            c.insets = new Insets(0, 0, 5, 5);
            serverL_panel.add(disconnect, c);

            c.gridx = 2;
            c.gridy = 0;
            c.insets = new Insets(0, 5, 5, 5);
            serverL_panel.add(add_server, c);

            c.gridx = 3;
            c.gridy = 0;
            c.insets = new Insets(0, 5, 5, 0);
            serverL_panel.add(connect, c);

            c.weightx = 1; //spacing fra i bottoni per disconnettersi ed aggiungere un pulsante

            c.gridx = 1;
            c.gridy = 0;
            c.insets = new Insets(0, 5, 5, 5);
            serverL_panel.add(filler, c);

            c.weighty = 1;

            c.gridx = 0;
            c.gridy = 1;
            c.gridwidth = 4;
            c.insets = new Insets(5, 0, 0, 0);
            serverL_panel.add(server_scroller, c);

            serverL_panel.setPreferredSize(new Dimension(0, 0));

        }
        return serverL_panel;
    }

    public static void setEnabled(boolean enabled) {
        serverL_panel.setEnabled(enabled);
        connect.setEnabled(enabled);
        disconnect.setEnabled(enabled);
        add_server.setEnabled(enabled);
        server_list.setEnabled(enabled);
    }

    public static void update_gui() throws InvocationTargetException, InstantiationException, IllegalAccessException {
        for (String name : Database.serverList.keySet()) { //aggiunge alla lista tutti i server caricati sul database
            server_list.add(name);
        }
    }

    private static ActionListener add_server_listener = new ActionListener() {
        Vector<String> requests = new Vector<>();
        @Override
        public void actionPerformed(ActionEvent e) {
            if (requests.size() == 0) {
                requests.add("inserisci il link al server: ");
                requests.add("inserisci il nome del server: ");
            }

            TempPanel.request_string(requests, name_and_ip);
        }

        private StringVectorOperator name_and_ip = new StringVectorOperator() {
            @Override
            public void success()  {
                try {
                    if (ServerList_panel.is_valid(input.elementAt(0), false) && ServerList_panel.is_valid(input.elementAt(1), true)) {
                        Database.serverList.put(input.elementAt(1), input.elementAt(0)); //aggiunge indirizzo e nome al database
                        server_list.add(input.elementAt(1)); //aggiunge il nome del server alla JList rendendolo visibile
                    } else {
                        TempPanel.show_msg("il nome o indirizzo inseriti non sono validi, inserire nome ed indirizzo validi", error_name_ip, false);
                    }
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e);
                } catch (InstantiationException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void fail() {} //non si vuole più aggiungere un server
        };

        private StringVectorOperator error_name_ip = new StringVectorOperator() {
            @Override
            public void success() {
                TempPanel.request_string(requests, name_and_ip);
            }

            @Override
            public void fail() {} //essendo un messaggio non può essere premuto il tasto "annulla"
        };

    };

    private static boolean is_valid(String name, boolean is_a_name) { //controlla che il nome o indirizzo inserito sia valido
        return true;
    }

    private static ActionListener connect_listener = e -> {
        Connection.start_connection_with(Database.serverList.get(server_list.getSelectedValue()));
    };

    private static ActionListener disconnect_listener = e -> {
        Connection.disconnect();
    };

    public static class CellPopupMenu extends JPopupMenu {
        private String cell_name;
        private final MyJList PARENT_LIST;

        public CellPopupMenu(String name, MyJList list) {
            super();
            this.cell_name = name;
            this.PARENT_LIST = list;

            UIManager.put("MenuItem.selectionBackground", new Color(108, 111, 113));
            UIManager.put("MenuItem.selectionForeground", new Color(158, 161, 163));

            JMenuItem rename = new JMenuItem("rename");
            JMenuItem remove = new JMenuItem("remove");

            this.setBorder(BorderFactory.createLineBorder(new Color(28, 31, 33)));
            rename.setBorder(BorderFactory.createLineBorder(new Color(78, 81, 83)));
            remove.setBorder(BorderFactory.createLineBorder(new Color(78, 81, 83)));

            rename.setBackground(new Color(88, 91, 93));
            remove.setBackground(new Color(88, 91, 93));
            rename.setForeground(new Color(158, 161, 163));
            remove.setForeground(new Color(158, 161, 163));

            rename.addActionListener(rename_listener);
            remove.addActionListener(remove_listener);

            this.add(rename);
            this.add(remove);
        }

        private StringVectorOperator rename_action = new StringVectorOperator() {
            @Override
            public void success() {
                if (is_valid(input.elementAt(0), true) && !input.elementAt(0).equals(cell_name)) {
                    PARENT_LIST.rename_element(cell_name, input.elementAt(0)); //modifica il nome nella lista visibile

                    Database.serverList.put( //modifica il nome nel database
                            input.elementAt(0),
                            Database.serverList.get(cell_name)
                    );
                    Database.serverList.remove(cell_name);

                    cell_name = input.elementAt(0); //modifica il nome per questo popup
                } else {
                    TempPanel.show_msg("inserisci un nome valido ed unico fra tutti i server", rename_fail, false);
                }
            }

            @Override
            public void fail() {} //non si vuole più rinominare il server
        };

        private StringVectorOperator rename_fail = new StringVectorOperator() {
            @Override
            public void success() {
                TempPanel.request_string("inserisci il nuovo nome per il server: " + cell_name, rename_action);
            }

            @Override
            public void fail() {} //essendo un messaggio non può "fallire"
        };

        private ActionListener rename_listener = (e) -> {
            TempPanel.request_string("inserisci il nuovo nome per il server: " + cell_name, rename_action);
        };

        private StringVectorOperator remove_confirm = new StringVectorOperator() {
            @Override
            public void success() {
                Database.serverList.remove(cell_name); //rimuove il server dal database
                PARENT_LIST.remove(cell_name); //rimuove il server dalla lista visibile
            }

            @Override
            public void fail() {} //non vuole più rimuovere il server
        };

        private ActionListener remove_listener = (e) -> {
            TempPanel.show_msg("il server \"" + cell_name + "\" verrà rimosso", remove_confirm, true);
        };

    }
}