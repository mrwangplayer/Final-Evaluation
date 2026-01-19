package silentconvent;

import java.awt.*;
import javax.swing.*;

public class SaveMenu {

    public static void open() {
        open(null);
    }

    public static void open(JDialog parent) {
        JDialog dialog = new JDialog((Frame) null, "Memory", true);
        dialog.setSize(500, 360);
        dialog.setLayout(new BorderLayout());
        dialog.setLocationRelativeTo(null);

        DefaultListModel<String> model = new DefaultListModel<>();
        for (String s : SaveManager.listSaves())
            model.addElement(s);
        JList<String> list = new JList<>(model);
        JScrollPane sc = new JScrollPane(list);
        dialog.add(sc, BorderLayout.CENTER);

        JPanel buttons = new JPanel();
        JButton saveBtn = new JButton("Save");
        JButton loadBtn = new JButton("Load");
        JButton deleteBtn = new JButton("Delete");
        JButton cancelBtn = new JButton("Cancel");
        buttons.add(saveBtn);
        buttons.add(loadBtn);
        buttons.add(deleteBtn);
        buttons.add(cancelBtn);
        dialog.add(buttons, BorderLayout.SOUTH);

        saveBtn.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(dialog, "Save name:");
            if (name != null && !name.trim().isEmpty()) {
                String saved = SaveManager.saveGame(name.trim());
                if (saved != null) {
                    model.addElement(saved);
                    JOptionPane.showMessageDialog(dialog, "Saved as: " + saved);
                } else {
                    JOptionPane.showMessageDialog(dialog, "Save failed.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        loadBtn.addActionListener(e -> {
            String sel = list.getSelectedValue();
            if (sel == null) {
                JOptionPane.showMessageDialog(dialog, "Select a save to load.");
                return;
            }
            if (SaveManager.isBroken()) {
                // Narrative: loading is blocked and appears corrupted
                GameFrame.triggerRememberGlitch();
                return;
            }
            GameFrame.ensureCreated();
            boolean ok = SaveManager.loadGame(sel);
            if (ok) {
                JOptionPane.showMessageDialog(dialog, "Loaded.");
                dialog.dispose();
                if (parent != null) {
                    parent.dispose();
                }
                SceneManager.ensureButtonsInitialized();
                GameFrame.getInstance().setVisible(true);
                GameFrame.getInstance().setState(java.awt.Frame.NORMAL);
                GameFrame.getInstance().toFront();
            } else {
                // SaveManager handles glitching messages
            }
        });

        deleteBtn.addActionListener(e -> {
            String sel = list.getSelectedValue();
            if (sel == null) {
                JOptionPane.showMessageDialog(dialog, "Select a save to delete.");
                return;
            }
            int conf = JOptionPane.showConfirmDialog(dialog, "Delete save '" + sel + "'?", "Confirm",
                    JOptionPane.YES_NO_OPTION);
            if (conf == JOptionPane.YES_OPTION) {
                java.io.File f = new java.io.File("saves", sel + ".sav");
                if (f.exists() && f.delete()) {
                    model.removeElement(sel);
                    JOptionPane.showMessageDialog(dialog, "Deleted.");
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to delete.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }
}
