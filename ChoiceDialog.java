package silentconvent;

import java.awt.*;
import javax.swing.*;

public class ChoiceDialog {
    public static int showChoices(Component parent, String prompt, String[] options) {
        // If there is a GameFrame available and parent is null, create an in-window
        // model dialog
        if (parent == null && silentconvent.GameFrame.class != null) {
            // Dim the screen using the fade overlay and show a model JDialog owned by the
            // game frame
            silentconvent.GameFrame.setFadeAlpha(0.45f);
            Frame owner = null;
            if (silentconvent.GameFrame.getInstance() != null)
                owner = silentconvent.GameFrame.getInstance();
            JDialog dialog = new JDialog(owner, "Choose", true);
            dialog.setUndecorated(true);
            dialog.setLayout(new BorderLayout());
            dialog.add(new JLabel(prompt), BorderLayout.NORTH);
            JList<String> list = new JList<>(options);
            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            list.setSelectedIndex(0);
            dialog.add(new JScrollPane(list), BorderLayout.CENTER);
            JPanel btns = new JPanel();
            JButton ok = new JButton("OK");
            JButton cancel = new JButton("Cancel");
            btns.add(ok);
            btns.add(cancel);
            dialog.add(btns, BorderLayout.SOUTH);
            dialog.setSize(360, 220);
            dialog.setLocationRelativeTo(null);

            final int[] result = { -1 };
            ok.addActionListener(e -> {
                result[0] = list.getSelectedIndex();
                dialog.dispose();
            });
            cancel.addActionListener(e -> {
                result[0] = -1;
                dialog.dispose();
            });

            dialog.setVisible(true);
            silentconvent.GameFrame.setFadeAlpha(0f);
            return result[0];
        }

        JPanel p = new JPanel(new BorderLayout());
        p.add(new JLabel(prompt), BorderLayout.NORTH);
        JList<String> list = new JList<>(options);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setSelectedIndex(0);
        p.add(new JScrollPane(list), BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(parent, p, "Choose", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            return list.getSelectedIndex();
        }
        return -1;
    }
}
