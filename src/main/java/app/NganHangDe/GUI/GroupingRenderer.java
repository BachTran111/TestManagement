package app.NganHangDe.GUI;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

class GroupingRenderer extends DefaultTableCellRenderer {
    private Color vocabColor = new Color(240, 248, 255); // Màu cho Vocabulary
    private Color readingColor = new Color(255, 250, 240); // Màu cho Reading
    private Color listeningColor = new Color(240, 255, 240); // Màu cho Listening

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        String type = (String) table.getValueAt(row, 2);
        switch (type) {
            case "VOCABULARY":
                c.setBackground(vocabColor);
                break;
            case "READING":
                c.setBackground(readingColor);
                break;
            case "LISTENING":
                c.setBackground(listeningColor);
                break;
            default:
                c.setBackground(Color.WHITE);
        }

        if (isSelected) {
            c.setBackground(c.getBackground().darker());
        }

        return c;
    }
}

