package app.NganHangDe.GUI;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    public MainFrame() {
        super("Hệ thống Ngân hàng Đề thi");
        initComponents();
        setupFrame();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new GridLayout(2, 1, 20, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        JButton btnQuestionManager = createStyledButton("QUẢN LÝ CÂU HỎI");
        btnQuestionManager.addActionListener(e -> openQuestionForm());

        JButton btnExamManager = createStyledButton("QUẢN LÝ ĐỀ THI");
        btnExamManager.addActionListener(e -> openExamEditor());

        mainPanel.add(btnQuestionManager);
        mainPanel.add(btnExamManager);

        add(mainPanel, BorderLayout.CENTER);
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 18));
        button.setPreferredSize(new Dimension(300, 80));
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        return button;
    }

    private void setupFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);
    }

    private void openQuestionForm() {
        new QuestionForm().setVisible(true);
    }

    private void openExamEditor() {
        new ExamEditor().setVisible(true);
    }

}