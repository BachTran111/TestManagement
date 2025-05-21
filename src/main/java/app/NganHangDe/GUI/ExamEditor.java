package app.NganHangDe.GUI;

import app.NganHangDe.DAO.DeThiDAO;
import app.NganHangDe.DAO.DeThiChiTietDAO;
import app.NganHangDe.Model.DeThi;
import app.NganHangDe.Model.DeThiChiTiet;
import app.NganHangDe.Service.ExportService;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.List;

public class ExamEditor extends JFrame {
    private JTextField nameField, descriptionField, dateField;
    private JTextField questionIdField, questionNumberField;
    private JTable examTable;
    private DefaultTableModel tableModel;

    private DeThiDAO deThiDAO = new DeThiDAO();
    private DeThiChiTietDAO chiTietDAO = new DeThiChiTietDAO();

    public ExamEditor() {
        setTitle("Quản lý Đề Thi");
        setSize(1000, 615);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        initComponents();
        loadExamList();
    }

    private void initComponents() {
        // Form inputs
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Thông tin đề thi"));

        nameField = new JTextField();
        descriptionField = new JTextField();
        dateField = new JTextField();

        formPanel.add(new JLabel("Tên đề thi:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Mô tả:"));
        formPanel.add(descriptionField);
        formPanel.add(new JLabel("Ngày tạo (yyyy-MM-dd):"));
        formPanel.add(dateField);

        // Question Add
        JPanel questionPanel = new JPanel(new GridLayout(2, 2, 5, 5));
//        questionPanel.setBorder(BorderFactory.createTitledBorder("Thêm câu hỏi vào đề thi"));
//
//        questionIdField = new JTextField();
//        questionNumberField = new JTextField();
//        questionPanel.add(new JLabel("ID câu hỏi:"));
//        questionPanel.add(questionIdField);
//        questionPanel.add(new JLabel("Số thứ tự:"));
//        questionPanel.add(questionNumberField);

        // Buttons
        JPanel buttonPanel = new JPanel();
        JButton newBtn = new JButton("Tạo mới");
        JButton saveBtn = new JButton("Lưu");
        JButton updateBtn = new JButton("Cập nhật");
        JButton deleteBtn = new JButton("Xóa");
        JButton addQBtn = new JButton("Thêm câu hỏi");
        JButton btnExportDocx = new JButton("Xuất DOCX");

        buttonPanel.add(newBtn);
        buttonPanel.add(saveBtn);
        buttonPanel.add(updateBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(addQBtn);
        buttonPanel.add(btnExportDocx);

        // Table
        String[] columns = {"ID", "Tên đề thi", "Mô tả", "Ngày tạo"};
        tableModel = new DefaultTableModel(columns, 0);
        examTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(examTable);

        // Layout
        setLayout(new BorderLayout());
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(formPanel, BorderLayout.CENTER);
        topPanel.add(questionPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
        add(scrollPane, BorderLayout.SOUTH);

        // Event handlers
        saveBtn.addActionListener(e -> saveExam());
        updateBtn.addActionListener(e -> updateExam());
        deleteBtn.addActionListener(e -> deleteExam());
        newBtn.addActionListener(e -> clearForm());
        btnExportDocx.addActionListener(e -> {
            deThiDAO = new DeThiDAO();
            DeThi selectedDeThi = null;
            int row = examTable.getSelectedRow();
            if (row >= 0) {
                int deThiId = (int) tableModel.getValueAt(row, 0);
                try {
                    selectedDeThi = deThiDAO.findById(deThiId);
                } catch (Exception e1) {}
            }
            if (selectedDeThi == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 đề thi");
                return;
            }

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("Word Documents", "docx"));

            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                String filePath = fileChooser.getSelectedFile().getAbsolutePath();
                if (!filePath.endsWith(".docx")) filePath += ".docx";

                try {
                    new ExportService().exportToDocx(selectedDeThi, filePath);
                    JOptionPane.showMessageDialog(this, "Xuất file thành công!");
                    Desktop.getDesktop().open(new File(filePath));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Lỗi xuất file: " + ex.getMessage());
                }
            }
        });
        addQBtn.addActionListener(e -> {
            int row = examTable.getSelectedRow();
            if (row >= 0) {
                int deThiId = (int) tableModel.getValueAt(row, 0);
                new QuestionSelectorDialog(this, deThiId).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Chọn đề thi trước khi thêm câu hỏi.");
            }
        });


        examTable.getSelectionModel().addListSelectionListener(e -> {
            int row = examTable.getSelectedRow();
            if (row >= 0) {
                nameField.setText(tableModel.getValueAt(row, 1).toString());
                descriptionField.setText(tableModel.getValueAt(row, 2).toString());
                dateField.setText(tableModel.getValueAt(row, 3).toString());
            }
        });
    }

    private void loadExamList() {
        try {
            List<DeThi> exams = deThiDAO.findAll();
            tableModel.setRowCount(0);
            for (DeThi d : exams) {
                tableModel.addRow(new Object[]{
                        d.getId(), d.getName(), d.getDescription(), d.getDate()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải danh sách đề thi: " + ex.getMessage());
        }
    }

    private void clearForm() {
        nameField.setText("");
        descriptionField.setText("");
        dateField.setText("");
        examTable.clearSelection();
    }

    private void saveExam() {
        try {
            DeThi deThi = new DeThi();
            deThi.setName(nameField.getText());
            deThi.setDescription(descriptionField.getText());
            deThi.setDate(java.sql.Date.valueOf(dateField.getText()));
            deThiDAO.create(deThi);
            loadExamList();
            clearForm();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi lưu: " + ex.getMessage());
        }
    }

    private void updateExam() {
        int row = examTable.getSelectedRow();
        if (row >= 0) {
            try {
                int id = (int) tableModel.getValueAt(row, 0);
                DeThi deThi = new DeThi();
                deThi.setId(id);
                deThi.setName(nameField.getText());
                deThi.setDescription(descriptionField.getText());
                deThi.setDate(java.sql.Date.valueOf(dateField.getText()));
                deThiDAO.update(deThi);
                loadExamList();
                clearForm();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật: " + ex.getMessage());
            }
        }
    }

    private void deleteExam() {
        int row = examTable.getSelectedRow();
        if (row >= 0) {
            int confirm = JOptionPane.showConfirmDialog(this, "Xóa đề thi này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    int id = (int) tableModel.getValueAt(row, 0);
                    deThiDAO.delete(id);
                    loadExamList();
                    clearForm();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Lỗi khi xóa: " + ex.getMessage());
                }
            }
        }
    }

    private void addQuestionToExam() {
        int row = examTable.getSelectedRow();
        if (row >= 0) {
            try {
                int deThiId = (int) tableModel.getValueAt(row, 0);
                int cauHoiId = Integer.parseInt(questionIdField.getText());
                int soThuTu = Integer.parseInt(questionNumberField.getText());

                DeThiChiTiet ct = new DeThiChiTiet();
                ct.setDeThiId(deThiId);
                ct.setCauHoiId(cauHoiId);
                ct.setQuestionNumber(soThuTu);

                chiTietDAO.add(ct);
                JOptionPane.showMessageDialog(this, "Đã thêm câu hỏi vào đề thi.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi thêm câu hỏi: " + ex.getMessage());
            }
        }
    }

    private void handleExportDocx() {
        deThiDAO = new DeThiDAO();
        DeThi selectedDeThi = null;
        int row = examTable.getSelectedRow();
        if (row >= 0) {
            int deThiId = (int) tableModel.getValueAt(row, 0);
            try {
                selectedDeThi = deThiDAO.findById(deThiId);
            } catch (Exception e) {
            }
            if (selectedDeThi == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một đề thi trước khi xuất");
                return;
            }

            // Hiển thị hộp thoại chọn file
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Lưu file DOCX");
            fileChooser.setFileFilter(new FileNameExtensionFilter("Word Documents", "docx"));

            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();

                // Đảm bảo có phần mở rộng .docx
                if (!file.getName().toLowerCase().endsWith(".docx")) {
                    file = new File(file.getAbsolutePath() + ".docx");
                }

                try {
                    // Gọi service export
                    ExportService exportService = new ExportService();
                    exportService.exportToDocx(selectedDeThi, file.getAbsolutePath());

                    // Thông báo thành công
                    JOptionPane.showMessageDialog(this,
                            "Xuất file thành công!\nĐường dẫn: " + file.getAbsolutePath());

                    // Tự động mở file (tuỳ chọn)
                    Desktop.getDesktop().open(file);

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this,
                            "Lỗi khi xuất file: " + ex.getMessage(),
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}
