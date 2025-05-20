package app.NganHangDe.GUI;

import app.NganHangDe.DAO.CauHoiDAO;
import app.NganHangDe.DAO.DapAnDAO;
import app.NganHangDe.Model.CauHoi;
import app.NganHangDe.Model.DapAn;
import app.NganHangDe.Model.ParsedQuestion;
import app.NganHangDe.Model.ParsedOption;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class QuestionReviewDialog extends JDialog {
    private JTable tblQuestions;
    private DefaultTableModel model;
    private List<ParsedQuestion> questions;
    private JButton btnConfirm;
    private CauHoiDAO cauHoiDAO;
    private DapAnDAO dapAnDAO;

    public QuestionReviewDialog(Frame owner, List<ParsedQuestion> questions) {
        super(owner, "Xem và chọn câu hỏi", true);
        cauHoiDAO = new CauHoiDAO();
        dapAnDAO = new DapAnDAO();
        this.questions = questions;
        initComponents();
        loadData();
        setSize(800, 600);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        // Model và Table
        String[] cols = {"Chọn", "Nội dung câu hỏi", "Đáp án 1", "Đáp án 2", "Đáp án 3", "Đáp án 4"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Boolean.class : String.class;
            }
        };

        tblQuestions = new JTable(model);
        tblQuestions.setRowHeight(30);

        // Panel nút
        btnConfirm = new JButton("Xác nhận lưu");
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlButtons.add(btnConfirm);

        // Layout
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(new JScrollPane(tblQuestions), BorderLayout.CENTER);
        getContentPane().add(pnlButtons, BorderLayout.SOUTH);

        // Xử lý sự kiện
        btnConfirm.addActionListener(e -> onConfirm());
    }

    private void loadData() {
        for (ParsedQuestion pq : questions) {
            Object[] row = new Object[6];
            row[0] = true; // Mặc định chọn tất cả
            row[1] = pq.getQuestionText();
            for (int i = 0; i < 4; i++) {
                row[i+2] = pq.getOptions().size() > i ? pq.getOptions().get(i).getContent() : "";
            }
            model.addRow(row);
        }
    }

    private void onConfirm() {
        List<ParsedQuestion> selectedQuestions = new ArrayList<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            if ((Boolean) model.getValueAt(i, 0)) {
                ParsedQuestion pq = questions.get(i);
                selectedQuestions.add(pq);
            }
        }

        try {
            saveQuestions(selectedQuestions);
            JOptionPane.showMessageDialog(this, "Đã lưu " + selectedQuestions.size() + " câu hỏi");
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi lưu: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveQuestions(List<ParsedQuestion> questions) throws Exception {
        try {
            // Gọi DAO để lưu từng câu hỏi
            for (ParsedQuestion pq : questions) {
                CauHoi ch = new CauHoi();
                ch.setContent(pq.getQuestionText());
                ch.setType("TRAC_NGHIEM");
                cauHoiDAO.create(ch);

                for (ParsedOption po : pq.getOptions()) {
                    DapAn da = new DapAn();
                    da.setCauHoiId(ch.getId());
                    da.setContent(po.getContent());
                    da.setCorrect(po.isCorrect());
                    dapAnDAO.create(da);
                }
            }
        } catch (SQLException ex) {
            throw new Exception("Lỗi CSDL: " + ex.getMessage());
        }
    }
}
