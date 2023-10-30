package gui;

import file_database.Database;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Vector;

public abstract class ButtonTopBar_panel {
    private static JPanel buttons_list;
    private static JScrollPane buttons_scroller;

    private static JPanel buttons_panel = null;
    protected static JPanel init() {
        if (buttons_list == null) {
            buttons_panel = new JPanel();
            buttons_panel.setLayout(new GridBagLayout());

            //inizializza tutti i componenti della gui
            JButton right_shift = new JButton();
            JButton left_shift = new JButton();
            buttons_list = new JPanel();
            buttons_scroller = new JScrollPane(buttons_list, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

            right_shift.setIcon(new ImageIcon(Database.project_path + "images/right_arrow.png"));
            right_shift.setRolloverIcon(new ImageIcon(Database.project_path + "images/right_arrow_sel.png"));
            right_shift.setPressedIcon(new ImageIcon(Database.project_path + "images/right_arrow_pres.png"));
            left_shift.setIcon(new ImageIcon(Database.project_path + "images/left_arrow.png"));
            left_shift.setRolloverIcon(new ImageIcon(Database.project_path + "images/left_arrow_sel.png"));
            left_shift.setPressedIcon(new ImageIcon(Database.project_path + "images/left_arrow_pres.png"));

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

    public static void init_buttons() throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        File buttons_folder = new File(Database.project_path + "buttons");
        String[] button_class_files = buttons_folder.list();

        class Button_class extends ClassLoader {
            public Class find_class(String class_name) throws IOException {
                byte[] class_data = new FileInputStream(Database.project_path + "buttons/" + class_name + ".class").readAllBytes(); //read file
                return defineClass(class_name, class_data, 0, class_data.length); //define class
            }
        }

        for (String class_name : button_class_files) {
            if (class_name.substring(class_name.length() - 6, class_name.length()).equals(".class")) { //se è un file .class
                String file_name = class_name.substring(0, class_name.length() - 6);
                Class button_class = new Button_class().find_class(file_name);

                //imposta la funzione "public static void on_press(String name)" all'interno della classe per essere invocata una volta premuto un pulsante
                ButtonTopBar_panel.on_press = button_class.getDeclaredMethod("on_press", String.class);

                //all'interno della classe dovrà essere definita una funzione pubblica statica "define_buttons" che viene invocata ora per far registrare tutti i bottoni
                button_class.getDeclaredMethod("register_buttons").invoke(null);
            }
        }
    }

    private static Method on_press;
    public static void register_button(ButtonInfo info) {
        JButton b = new JButton();
        b.setToolTipText(info.name);

        b.setIcon(info.default_icon);
        b.setRolloverIcon(info.rollover_icon);
        b.setPressedIcon(info.pressed_icon);
        b.setDisabledIcon(info.disabled_icon);

        b.addActionListener(new ActionListener() {
            private Method on_press = ButtonTopBar_panel.on_press;
            String name = b.getToolTipText();
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    on_press.invoke(null, name);

                } catch (IllegalAccessException ex) {
                    throw new RuntimeException(ex);
                } catch (InvocationTargetException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        b.setPreferredSize(new Dimension(info.default_icon.getIconWidth(), info.default_icon.getIconHeight()));
        b.setBorder(null);
        b.setEnabled(false);

        buttons_list.add(b);
        buttons_scroller.updateUI();
        buttons_scroller.setBorder(null); //altrimenti con updateUI() si mostra il bordo
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
