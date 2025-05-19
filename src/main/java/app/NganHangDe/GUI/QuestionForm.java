package app.NganHangDe.GUI;

import app.NganHangDe.DAO.CauHoiDAO;
import app.NganHangDe.DAO.DapAnDAO;
import app.NganHangDe.Model.CauHoi;
import app.NganHangDe.Model.DapAn;
//import app.NganHangDe.Service.AIService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class QuestionForm extends JFrame {
    private JTable tblCauHoi;
    private DefaultTableModel tableModel;
    private JTextArea txtContent;
    private JComboBox<String> cmbType;
    private JTextField txtAmThanhId;
    private JButton btnNew, btnSave, btnUpdate, btnDelete, btnSuggest;
    private CauHoiDAO cauHoiDAO;
    private DapAnDAO dapAnDAO;

    //    private AIService aiService;
    private Integer selectedId = null;

    // Answer components
    private JPanel pnlAnswers;
    private JRadioButton[] radOptions;
    private JTextField[] txtOptions;
    private ButtonGroup answerGroup;
    private JTextField txtWritingAnswer;

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

        cmbType.addActionListener(e -> updateAnswerPanel());

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

        pnlAnswers = new JPanel();
        pnlAnswers.setLayout(new BoxLayout(pnlAnswers, BoxLayout.Y_AXIS));
        updateAnswerPanel();

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth=4; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(pnlAnswers, gbc);

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

    private void updateAnswerPanel() {
        dapAnDAO = new DapAnDAO();
        pnlAnswers.removeAll();
        int rows = tblCauHoi.getSelectedRow();
        if (rows == -1) {
            selectedId = null;
        } else {
            selectedId = (Integer) tableModel.getValueAt(rows, 0);
        }
        try {
//            List<DapAn> answers = dapAnDAO.findByCauHoiId(selectedId);
            List<DapAn> answers = (selectedId != null) ? dapAnDAO.findByCauHoiId(selectedId) : new ArrayList<>();
            if (cmbType.getSelectedItem().equals("Multiple Choice")) {
                radOptions = new JRadioButton[4];
                txtOptions = new JTextField[4];
                answerGroup = new ButtonGroup();
                String[] labels = {"A:", "B:", "C:", "D:"};

                for (int i = 0; i < 4; i++) {
                    JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
                    radOptions[i] = new JRadioButton();
                    txtOptions[i] = new JTextField(30);
                    answerGroup.add(radOptions[i]);

                    if (i < answers.size()) {
                        txtOptions[i].setText(answers.get(i).getContent());
                        radOptions[i].setSelected(answers.get(i).getCorrect());
                    }

                    row.add(new JLabel(labels[i]));
                    row.add(txtOptions[i]);
                    row.add(radOptions[i]);
                    pnlAnswers.add(row);
                }

            } else { //1
                txtWritingAnswer = new JTextField(40);
                JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
                row.add(new JLabel("Đáp án:"));

                if (!answers.isEmpty()) {
                    txtWritingAnswer.setText(answers.get(0).getContent());
                }

                row.add(txtWritingAnswer);
                pnlAnswers.add(row);
            }

            pnlAnswers.revalidate();
            pnlAnswers.repaint();

        } catch (SQLException e) {
            showError(e);
        }
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
            updateAnswerPanel();
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void clearForm() {
        selectedId = null;
        txtContent.setText("");
        cmbType.setSelectedIndex(0);
        txtAmThanhId.setText("");
        updateAnswerPanel();
    }

    private void saveQuestion() {
        try {
            CauHoi ch = new CauHoi();
            dapAnDAO = new DapAnDAO();
            ch.setContent(txtContent.getText());
            ch.setType((String)cmbType.getSelectedItem());
            ch.setAmThanhId(txtAmThanhId.getText().isBlank() ? null : Integer.valueOf(txtAmThanhId.getText()));

            // 1. Lưu câu hỏi
            cauHoiDAO.create(ch);
            int questionId = ch.getId();
            // 2. Nếu là Multiple Choice → lưu 4 đáp án
            if ("Multiple Choice".equals(ch.getType())) {
                for (int i = 0; i < 4; i++) {
                    DapAn da = new DapAn();
                    da.setCauHoiId(questionId);
                    da.setContent(txtOptions[i].getText());
                    da.setCorrect(radOptions[i].isSelected());
                    dapAnDAO.create(da);
                }
            }

            // 3. Nếu là Writing → lưu 1 đáp án
            if ("Writing".equals(ch.getType())) {
                DapAn da = new DapAn();
                da.setCauHoiId(questionId);
                da.setContent(txtWritingAnswer.getText());
                da.setCorrect(true);
                dapAnDAO.update(da);
            }

            loadData();
            clearForm();
        } catch (SQLException ex) {
            showError(ex);
        }
    }


    private void updateQuestion() {
        if (selectedId == null) return;
        try {
            // Cập nhật câu hỏi
            CauHoi ch = new CauHoi();
            ch.setId(selectedId);
            ch.setContent(txtContent.getText());
            ch.setType((String) cmbType.getSelectedItem());
            ch.setAmThanhId(txtAmThanhId.getText().isBlank() ? null : Integer.valueOf(txtAmThanhId.getText()));
            cauHoiDAO.update(ch);

            // Xóa các đáp án cũ
            dapAnDAO.deleteByCauHoiId(selectedId);

            // Lưu lại đáp án mới
            if (cmbType.getSelectedItem().equals("Multiple Choice")) {
                for (int i = 0; i < 4; i++) {
                    String noiDung = txtOptions[i].getText().trim();
                    if (!noiDung.isEmpty()) {
                        DapAn da = new DapAn();
                        da.setCauHoiId(selectedId);
                        da.setContent(noiDung);
                        da.setCorrect(radOptions[i].isSelected());
                        dapAnDAO.create(da);
                    }
                }
            } else {
                String noiDung = txtWritingAnswer.getText().trim();
                if (!noiDung.isEmpty()) {
                    DapAn da = new DapAn();
                    da.setCauHoiId(selectedId);
                    da.setContent(noiDung);
                    da.setCorrect(true); // đáp án viết luôn đúng
                    dapAnDAO.create(da);
                }
            }

            loadData();
            clearForm();
        } catch (SQLException ex) {
            showError(ex);
        }
    }


    private void deleteQuestion() {
        if (selectedId == null) return;
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn chắc chắn muốn xóa?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // Xóa đáp án trước
                dapAnDAO.deleteByCauHoiId(selectedId);

                // Xóa câu hỏi
                cauHoiDAO.delete(selectedId);

                loadData();
                clearForm();
            } catch (SQLException ex) {
                showError(ex);
            }
        }
    }


    private void showError(Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}
