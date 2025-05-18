package app.NganHangDe.GUI;

import app.NganHangDe.DAO.CauHoiDAO;
import app.NganHangDe.Model.CauHoi;
//import app.NganHangDe.Service.AIService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.List;

public class QuestionForm extends JFrame {
    private JTable tblCauHoi;
    private DefaultTableModel tableModel;
    private JTextArea txtContent;
    private JComboBox<String> cmbType;
    private JTextField txtAmThanhId;
    private JButton btnNew, btnSave, btnUpdate, btnDelete, btnSuggest;
    private CauHoiDAO cauHoiDAO;
//    private AIService aiService;
    private Integer selectedId = null;

    public QuestionForm() {
        super("Quản lý Câu Hỏi");
        cauHoiDAO = new CauHoiDAO();
//        aiService = new AIService();  // xử lý IOException bên trong
        initComponents();
        loadData();
    }

    private void initComponents() {
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Table
        tableModel = new DefaultTableModel(new String[]{"ID", "Content", "Type", "Audio ID"}, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblCauHoi = new JTable(tableModel);
        tblCauHoi.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblCauHoi.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount()==2) loadSelected();
            }
        });

        // Form inputs
        txtContent = new JTextArea(3, 40);
        cmbType = new JComboBox<>(new String[]{"Multiple Choice", "Listening", "Reading", "Writing"});
        txtAmThanhId = new JTextField(5);

        btnNew = new JButton("Tạo mới");
        btnSave = new JButton("Lưu");
        btnUpdate = new JButton("Cập nhật");
        btnDelete = new JButton("Xóa");
        btnSuggest = new JButton("Gợi ý AI");

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5); gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Nội dung:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.gridwidth=3; formPanel.add(new JScrollPane(txtContent), gbc);
        gbc.gridwidth=1;
        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("Loại câu hỏi:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; formPanel.add(cmbType, gbc);
        gbc.gridx = 2; gbc.gridy = 1; formPanel.add(new JLabel("ID Audio:"), gbc);
        gbc.gridx = 3; gbc.gridy = 1; formPanel.add(txtAmThanhId, gbc);
        gbc.gridx = 1; gbc.gridy = 2; formPanel.add(btnSuggest, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(btnNew); btnPanel.add(btnSave); btnPanel.add(btnUpdate); btnPanel.add(btnDelete);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(formPanel, BorderLayout.CENTER);
        topPanel.add(btnPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(tblCauHoi), BorderLayout.CENTER);

        // Actions
        btnNew.addActionListener(e -> clearForm());
        btnSave.addActionListener(e -> saveQuestion());
        btnUpdate.addActionListener(e -> updateQuestion());
        btnDelete.addActionListener(e -> deleteQuestion());
//        btnSuggest.addActionListener(e -> suggestAI());
    }

    private void loadData() {
        try {
            List<CauHoi> list = cauHoiDAO.findAll();
            tableModel.setRowCount(0);
            for (CauHoi ch : list) {
                tableModel.addRow(new Object[]{ch.getId(), ch.getContent(), ch.getType(), ch.getAmThanhId()});
            }
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void loadSelected() {
        int row = tblCauHoi.getSelectedRow();
        if (row==-1) return;
        selectedId = (Integer) tableModel.getValueAt(row,0);
        try {
            CauHoi ch = cauHoiDAO.findById(selectedId);
            txtContent.setText(ch.getContent());
            cmbType.setSelectedItem(ch.getType());
            txtAmThanhId.setText(ch.getAmThanhId()!=null?ch.getAmThanhId().toString():"");
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void clearForm() {
        selectedId = null;
        txtContent.setText("");
        cmbType.setSelectedIndex(0);
        txtAmThanhId.setText("");
    }

    private void saveQuestion() {
        try {
            CauHoi ch = new CauHoi();
            ch.setContent(txtContent.getText());
            ch.setType((String)cmbType.getSelectedItem());
            ch.setAmThanhId(txtAmThanhId.getText().isBlank()?null:Integer.valueOf(txtAmThanhId.getText()));
            cauHoiDAO.create(ch);
            loadData(); clearForm();
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void updateQuestion() {
        if (selectedId==null) return;
        try {
            CauHoi ch = new CauHoi();
            ch.setId(selectedId);
            ch.setContent(txtContent.getText());
            ch.setType((String)cmbType.getSelectedItem());
            ch.setAmThanhId(txtAmThanhId.getText().isBlank()?null:Integer.valueOf(txtAmThanhId.getText()));
            cauHoiDAO.update(ch);
            loadData(); clearForm();
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void deleteQuestion() {
        if (selectedId==null) return;
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn chắc chắn muốn xóa?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm==JOptionPane.YES_OPTION) {
            try {
                cauHoiDAO.delete(selectedId);
                loadData(); clearForm();
            } catch (SQLException ex) {
                showError(ex);
            }
        }
    }

//    private void suggestAI() {
//        String prompt = txtContent.getText();
//        if (prompt.isBlank()) return;
//        try {
//            String suggestion = aiService.suggestAnswer(prompt);
//            JOptionPane.showMessageDialog(this, suggestion, "Gợi ý AI", JOptionPane.INFORMATION_MESSAGE);
//        } catch (Exception ex) {
//            showError(ex);
//        }
//    }

    private void showError(Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

}
