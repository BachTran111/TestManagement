package app.NganHangDe.GUI;

import javax.swing.*;
import java.awt.*;

public class ExamEditor extends JDialog {
    private JTextField nameField;
    private JTextArea descArea;
    private JTable questionTable;

    public ExamEditor(Integer examId) {
        setTitle(examId == null ? "Tạo đề mới" : "Chỉnh sửa đề thi");
        setSize(800, 600);
        initComponents(examId);
    }

    private void initComponents(Integer examId) {
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Form fields
        JPanel formPanel = new JPanel(new GridLayout(3, 2));
        nameField = new JTextField();
        descArea = new JTextArea(3, 20);
        JScrollPane descScroll = new JScrollPane(descArea);
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());

        formPanel.add(new JLabel("Tên đề thi:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Mô tả:"));
        formPanel.add(descScroll);

        // Question table
        String[] columns = {"STT", "Câu hỏi", "Loại"};
        Object[][] data = {}; // Lấy từ DAO
        questionTable = new JTable(data, columns);
        JScrollPane tableScroll = new JScrollPane(questionTable);

        // Control buttons
        JButton addQuestionBtn = new JButton("Thêm câu hỏi");
        JButton removeQuestionBtn = new JButton("Xóa câu hỏi");
        JButton saveBtn = new JButton("Lưu đề");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addQuestionBtn);
        buttonPanel.add(removeQuestionBtn);
        buttonPanel.add(saveBtn);

        mainPanel.add(formPanel, BorderLayout.NORTH);
        mainPanel.add(tableScroll, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Event handlers
        addQuestionBtn.addActionListener(e -> showQuestionBrowser());
        saveBtn.addActionListener(e -> saveExam());

        add(mainPanel);
    }

    private void showQuestionBrowser() {
        // Hiển thị dialog chọn câu hỏi từ ngân hàng
        QuestionBrowser browser = new QuestionBrowser();
        browser.setVisible(true);
    }

    private void saveExam() {
        // Gọi DAO để lưu đề thi
    }
}
