package gui;

import javax.swing.*;

public class ButtonInfo {
    public String name;
    public ImageIcon default_icon;
    public ImageIcon rollover_icon;
    public ImageIcon pressed_icon;
    public ImageIcon disabled_icon;

    public ButtonInfo(String name, ImageIcon default_icon, ImageIcon rollover_icon, ImageIcon pressed_icon, ImageIcon disabled_icon) {
        this.name = name;
        this.default_icon = default_icon;
        this.rollover_icon = rollover_icon;
        this.pressed_icon = pressed_icon;
        this.disabled_icon = disabled_icon;
    }
}
