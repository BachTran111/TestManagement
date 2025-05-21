package app.NganHangDe.GUI;

import app.NganHangDe.DAO.CauHoiDAO;
import app.NganHangDe.DAO.DapAnDAO;
import app.NganHangDe.DAO.DeThiChiTietDAO;
import app.NganHangDe.Model.CauHoi;
import app.NganHangDe.Model.DapAn;
import app.NganHangDe.Model.DeThiChiTiet;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class QuestionSelectorDialog extends JDialog {
    private JTable tblAll, tblSelected;
    private DefaultTableModel modelAll, modelSelected;
    private JTextArea txtContent;
    private JPanel pnlAnswers;
    private DeThiChiTietDAO chiTietDAO;
    private CauHoiDAO cauHoiDAO;
    private DapAnDAO dapAnDAO;
    private int deThiId;

    private JPanel pnlRandomConfig;
    private JSpinner spnVocabulary, spnReading, spnListening;
    private JButton btnGenerateRandom;


    public QuestionSelectorDialog(Frame owner, int deThiId) {
        super(owner, "Chọn câu hỏi cho đề " + deThiId, true);
        this.deThiId = deThiId;
        chiTietDAO = new DeThiChiTietDAO();
        cauHoiDAO = new CauHoiDAO();
        dapAnDAO = new DapAnDAO();
        initComponents();
        loadData();
        setSize(900, 600);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        String[] cols = {"ID", "Content", "Type", "Audio ID"};
        modelAll = new DefaultTableModel(cols, 0);
        modelSelected = new DefaultTableModel(cols, 0);
        tblAll = new JTable(modelAll);
        tblSelected = new JTable(modelSelected);
        tblAll.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblSelected.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblSelected.setDefaultRenderer(Object.class, new GroupingRenderer());

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

        pnlRandomConfig = new JPanel(new GridLayout(4, 2));
        pnlRandomConfig.setBorder(BorderFactory.createTitledBorder("Tạo ngẫu nhiên"));

        pnlRandomConfig.add(new JLabel("Từ vựng:"));
        spnVocabulary = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
        pnlRandomConfig.add(spnVocabulary);

        pnlRandomConfig.add(new JLabel("Đọc hiểu:"));
        spnReading = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
        pnlRandomConfig.add(spnReading);

        pnlRandomConfig.add(new JLabel("Nghe:"));
        spnListening = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
        pnlRandomConfig.add(spnListening);

        btnGenerateRandom = new JButton("Tạo ngẫu nhiên");
        pnlRandomConfig.add(btnGenerateRandom);

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

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(new JScrollPane(tblAll), BorderLayout.CENTER);
        leftPanel.add(pnlRandomConfig, BorderLayout.SOUTH);

        // Cập nhật JSplitPane
        splitTables.setLeftComponent(leftPanel);

        // Event handlers
        btnAdd.addActionListener(e -> onAdd());
        btnRemove.addActionListener(e -> onRemove());
        btnOk.addActionListener(e -> onOk());
        btnCancel.addActionListener(e -> dispose());

        tblAll.getSelectionModel().addListSelectionListener(e -> showDetail(tblAll));
        tblSelected.getSelectionModel().addListSelectionListener(e -> showDetail(tblSelected));
        btnGenerateRandom.addActionListener(e -> generateRandomQuestions());

    }

    private void loadData() {
        try {
            modelAll.setRowCount(0);
            modelSelected.setRowCount(0);

            List<CauHoi> all = cauHoiDAO.findAll();
            List<CauHoi> sel = cauHoiDAO.findByDeThi(deThiId);

            // Thêm logic lọc các câu đã chọn
            Set<Integer> selectedIds = sel.stream()
                    .map(CauHoi::getId)
                    .collect(Collectors.toSet());

            for (CauHoi ch : all) {
                if (!selectedIds.contains(ch.getId())) {
                    modelAll.addRow(new Object[]{
                            ch.getId(),
                            ch.getContent(),
                            ch.getType(),
                            ch.getAmThanhId()
                    });
                }
            }

            for (CauHoi ch : sel) {
                modelSelected.addRow(new Object[]{
                        ch.getId(),
                        ch.getContent(),
                        ch.getType(),
                        ch.getAmThanhId()
                });
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu: " + ex.getMessage());
        }
    }

    private void showDetail(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) return;

        int cauHoiId = (Integer) table.getValueAt(row, 0);
        String content = table.getValueAt(row, 1).toString();
        String type = table.getValueAt(row, 2).toString();

        txtContent.setText(content);
        pnlAnswers.removeAll();

        try {
            List<DapAn> dapAns = dapAnDAO.findByCauHoiId(cauHoiId);

                char opt = 'A';
                for (DapAn da : dapAns) {
                    JTextField tf = new JTextField(da.getContent(), 20);
                    tf.setEditable(false);
                    tf.setBorder(BorderFactory.createTitledBorder("Đáp án " + opt + (da.getCorrect() ? " ✔" : "")));
                    pnlAnswers.add(tf);
                    opt++;
                }
        } catch (SQLException ex) {
            pnlAnswers.add(new JLabel("Lỗi tải đáp án: " + ex.getMessage()));
        }

        pnlAnswers.revalidate();
        pnlAnswers.repaint();
    }


    private void onAdd() {
        int row = tblAll.getSelectedRow();
        if (row >= 0) {
            Integer id = (Integer) modelAll.getValueAt(row, 0);

            boolean exists = false;
            for (int i = 0; i < modelSelected.getRowCount(); i++) {
                if (modelSelected.getValueAt(i, 0).equals(id)) {
                    exists = true;
                    break;
                }
            }

            if (!exists) {
                Object[] vals = new Object[4];
                for (int i = 0; i < 4; i++) {
                    vals[i] = modelAll.getValueAt(row, i);
                }
                modelSelected.addRow(vals);
                modelAll.removeRow(row);
            }
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
            // Lấy danh sách ID câu hỏi đã chọn hiện tại
            Set<Integer> selectedIds = new HashSet<>();
            for (int i = 0; i < modelSelected.getRowCount(); i++) {
                selectedIds.add((Integer) modelSelected.getValueAt(i, 0));
            }

            // Lấy danh sách câu hỏi đã có trong đề thi từ CSDL
            List<DeThiChiTiet> existingDetails = chiTietDAO.findByDeThiId(deThiId);
            Set<Integer> existingIds = existingDetails.stream()
                    .map(DeThiChiTiet::getCauHoiId)
                    .collect(Collectors.toSet());

            // Xóa những câu hỏi không còn được chọn
            for (DeThiChiTiet detail : existingDetails) {
                if (!selectedIds.contains(detail.getCauHoiId())) {
                    chiTietDAO.delete(detail.getId());
                }
            }

            // Thêm những câu hỏi mới được chọn
            int order = 1;
            for (int i = 0; i < modelSelected.getRowCount(); i++) {
                Integer cauHoiId = (Integer) modelSelected.getValueAt(i, 0);

                if (!existingIds.contains(cauHoiId)) {
                    DeThiChiTiet ct = new DeThiChiTiet();
                    ct.setDeThiId(deThiId);
                    ct.setCauHoiId(cauHoiId);
                    ct.setQuestionNumber(order++);
                    chiTietDAO.add(ct);
                }
            }

            JOptionPane.showMessageDialog(this, "Cập nhật thành công.");
            dispose();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi lưu dữ liệu: " + ex.getMessage());
        }
    }

    private void generateRandomQuestions() {
        try {
            int vocab = (Integer) spnVocabulary.getValue();
            int reading = (Integer) spnReading.getValue();
            int listening = (Integer) spnListening.getValue();

            // Khởi tạo danh sách theo thứ tự ưu tiên
            List<CauHoi> randomQuestions = new ArrayList<>();

            // Xử lý từng loại theo thứ tự Vocabulary -> Reading -> Listening
            processQuestionType(randomQuestions, "VOCABULARY", vocab);
            processQuestionType(randomQuestions, "READING", reading);
            processQuestionType(randomQuestions, "LISTENING", listening);

            // Thêm vào bảng selected
            addToSelectedTable(randomQuestions);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi CSDL: " + ex.getMessage());
        }
    }

    private void processQuestionType(List<CauHoi> resultList, String type, int count) throws SQLException {
        if (count > 0) {
            List<CauHoi> questions = cauHoiDAO.findRandomByType(type, count);
            if (questions.size() < count) {
                JOptionPane.showMessageDialog(this,
                        "Không đủ câu " + type.toLowerCase() + " trong ngân hàng\n" +
                                "Yêu cầu: " + count + ", Tìm thấy: " + questions.size());
                return;
            }

            // Xáo trộn trong loại
            Collections.shuffle(questions);
            resultList.addAll(questions);
        }
    }

    private void addToSelectedTable(List<CauHoi> questions) {
        for (CauHoi ch : questions) {
            boolean alreadyExists = false;
            for (int i = 0; i < modelSelected.getRowCount(); i++) {
                if ((Integer) modelSelected.getValueAt(i, 0) == ch.getId()) {
                    alreadyExists = true;
                    break;
                }
            }

            if (!alreadyExists) {
                // Thêm vào cuối bảng selected
                modelSelected.addRow(new Object[]{
                        ch.getId(),
                        ch.getContent(),
                        ch.getType(),
                        ch.getAmThanhId()
                });
                // Xóa khỏi bảng all
                for (int i = 0; i < modelAll.getRowCount(); i++) {
                    if ((Integer) modelAll.getValueAt(i, 0) == ch.getId()) {
                        modelAll.removeRow(i);
                        break;
                    }
                }
            }
        }
    }

}
