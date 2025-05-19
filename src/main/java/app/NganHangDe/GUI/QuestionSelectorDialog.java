package app.NganHangDe.GUI;

import app.NganHangDe.DAO.CauHoiDAO;
import app.NganHangDe.DAO.DeThiChiTietDAO;
import app.NganHangDe.Model.CauHoi;
import app.NganHangDe.Model.DeThiChiTiet;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Dialog trực quan để chọn câu hỏi cho đề thi, hiển thị bảng và chi tiết câu hỏi đáp án
 */
public class QuestionSelectorDialog extends JDialog {
    private JTable tblAll, tblSelected;
    private DefaultTableModel modelAll, modelSelected;
    private JTextArea txtContent;
    private JPanel pnlAnswers;
    private DeThiChiTietDAO chiTietDAO;
    private CauHoiDAO cauHoiDAO;
    private int deThiId;

    public QuestionSelectorDialog(Frame owner, int deThiId) {
        super(owner, "Chọn câu hỏi cho đề " + deThiId, true);
        this.deThiId = deThiId;
        chiTietDAO = new DeThiChiTietDAO();
        cauHoiDAO = new CauHoiDAO();
        initComponents();
        loadData();
        setSize(900, 600);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        // Models và Tables
        String[] cols = {"ID", "Content", "Type", "Audio ID"};
        modelAll = new DefaultTableModel(cols, 0);
        modelSelected = new DefaultTableModel(cols, 0);
        tblAll = new JTable(modelAll);
        tblSelected = new JTable(modelSelected);
        tblAll.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblSelected.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Panel điều khiển giữa
        JButton btnAdd = new JButton("→");
        JButton btnRemove = new JButton("←");
        JPanel pnlButtons = new JPanel(new GridLayout(2,1,5,5));
        pnlButtons.add(btnAdd);
        pnlButtons.add(btnRemove);

        // Chi tiết câu hỏi và đáp án
        JPanel pnlDetail = new JPanel(new BorderLayout(5,5));
        pnlDetail.setBorder(BorderFactory.createTitledBorder("Chi tiết câu hỏi"));
        txtContent = new JTextArea(3, 50);
        txtContent.setEditable(false);
        pnlDetail.add(new JScrollPane(txtContent), BorderLayout.NORTH);
        pnlAnswers = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlDetail.add(pnlAnswers, BorderLayout.CENTER);

        // Bottom OK/Cancel
        JButton btnOk = new JButton("OK");
        JButton btnCancel = new JButton("Hủy");
        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlBottom.add(btnOk);
        pnlBottom.add(btnCancel);

        // Layout chính
        JSplitPane splitTables = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(tblAll), new JScrollPane(tblSelected));
        splitTables.setResizeWeight(0.5);

        JPanel center = new JPanel(new BorderLayout(5,5));
        center.add(splitTables, BorderLayout.CENTER);
        center.add(pnlButtons, BorderLayout.WEST);
        center.add(pnlDetail, BorderLayout.SOUTH);

        getContentPane().setLayout(new BorderLayout(5,5));
        getContentPane().add(center, BorderLayout.CENTER);
        getContentPane().add(pnlBottom, BorderLayout.SOUTH);

        // Event handlers
        btnAdd.addActionListener(e -> onAdd());
        btnRemove.addActionListener(e -> onRemove());
        btnOk.addActionListener(e -> onOk());
        btnCancel.addActionListener(e -> dispose());

        tblAll.getSelectionModel().addListSelectionListener(e -> showDetail(tblAll));
        tblSelected.getSelectionModel().addListSelectionListener(e -> showDetail(tblSelected));
    }

    private void loadData() {
        try {
            List<CauHoi> all = cauHoiDAO.findAll();
            List<CauHoi> sel = cauHoiDAO.findByDeThi(deThiId);
            for (CauHoi ch: all) modelAll.addRow(new Object[]{ch.getId(), ch.getContent(), ch.getType(), ch.getAmThanhId()});
            for (CauHoi ch: sel) {
                modelSelected.addRow(new Object[]{ch.getId(), ch.getContent(), ch.getType(), ch.getAmThanhId()});
                // xóa khỏi all nếu đã chọn
                for (int i = 0; i < modelAll.getRowCount(); i++) {
                    if ((Integer)modelAll.getValueAt(i,0) == ch.getId()) {
                        modelAll.removeRow(i);
                        break;
                    }
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu: " + ex.getMessage());
        }
    }

    private void showDetail(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) return;
        String content = table.getValueAt(row,1).toString();
        String type = table.getValueAt(row,2).toString();
        txtContent.setText(content);
        pnlAnswers.removeAll();
        if ("Multiple Choice".equals(type)) {
            for (char opt = 'A'; opt <= 'D'; opt++) {
                JTextField tf = new JTextField(20);
                tf.setBorder(BorderFactory.createTitledBorder("Đáp án " + opt));
                pnlAnswers.add(tf);
            }
        } else {
            JTextField tf = new JTextField(60);
            tf.setBorder(BorderFactory.createTitledBorder("Đáp án"));
            pnlAnswers.add(tf);
        }
        pnlAnswers.revalidate();
        pnlAnswers.repaint();
    }

    private void onAdd() {
        int row = tblAll.getSelectedRow();
        if (row >= 0) {
            Object[] vals = new Object[4];
            for (int i=0;i<4;i++) vals[i] = modelAll.getValueAt(row,i);
            modelSelected.addRow(vals);
            modelAll.removeRow(row);
        }
    }

    private void onRemove() {
        int row = tblSelected.getSelectedRow();
        if (row >= 0) {
            Object[] vals = new Object[4];
            for (int i=0;i<4;i++) vals[i] = modelSelected.getValueAt(row,i);
            modelAll.addRow(vals);
            modelSelected.removeRow(row);
        }
    }

    private void onOk() {
        try {
            chiTietDAO.deleteByDeThiId(deThiId);
            for (int i=0;i<modelSelected.getRowCount();i++) {
                DeThiChiTiet ct = new DeThiChiTiet();
                ct.setDeThiId(deThiId);
                ct.setCauHoiId((Integer)modelSelected.getValueAt(i,0));
                ct.setQuestionNumber(i+1);
                chiTietDAO.add(ct);
            }
            JOptionPane.showMessageDialog(this, "Cập nhật thành công.");
            dispose();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi lưu dữ liệu: " + ex.getMessage());
        }
    }
}
