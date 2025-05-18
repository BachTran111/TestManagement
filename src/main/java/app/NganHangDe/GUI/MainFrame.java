package app.NganHangDe.GUI;

import app.NganHangDe.DAO.DeThiDAO;
import app.NganHangDe.GUI.QuestionForm;
import app.NganHangDe.GUI.ExamEditor;
import app.NganHangDe.GUI.ExportForm;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.List;
import app.NganHangDe.Model.DeThi;

public class MainFrame extends JFrame {
    private JTable examTable;
    private JList<String> menuList;
    private DefaultTableModel examTableModel;
    private DeThiDAO deThiDAO;

    public MainFrame() {
        super("Quản lý Ngân hàng Đề thi");
        deThiDAO = new DeThiDAO();
        setSize(1000, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initComponents();
//        loadExams();
    }

    private void initComponents() {
        // Sidebar menu
        String[] menuItems = {"Đề thi", "Câu hỏi", "Âm thanh", "Export", "Thống kê"};
        menuList = new JList<>(menuItems);
        menuList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane menuScroll = new JScrollPane(menuList);
        menuScroll.setPreferredSize(new Dimension(200, 0));

        // Main content panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        // Toolbar
        JToolBar toolBar = new JToolBar();
        JButton newExamButton = new JButton("Tạo đề mới");
        JButton editExamButton = new JButton("Sửa đề");
        JButton deleteExamButton = new JButton("Xóa đề");
        toolBar.add(newExamButton);
        toolBar.add(editExamButton);
        toolBar.add(deleteExamButton);

        // Exam table
        String[] columnNames = {"ID", "Tên đề thi", "Ngày tạo", "Số câu hỏi"};
        examTableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        examTable = new JTable(examTableModel);
        JScrollPane tableScroll = new JScrollPane(examTable);
        mainPanel.add(toolBar, BorderLayout.NORTH);
        mainPanel.add(tableScroll, BorderLayout.CENTER);

        // Layout
        setLayout(new BorderLayout());
        add(menuScroll, BorderLayout.WEST);
        add(mainPanel, BorderLayout.CENTER);

        // Actions
        newExamButton.addActionListener(e -> showExamEditor(null));
        editExamButton.addActionListener(e -> openSelectedExam());
//        deleteExamButton.addActionListener(e -> deleteSelectedExam());

        menuList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    String selected = menuList.getSelectedValue();
                    switch (selected) {
                        case "Đề thi":
//                            loadExams();
                            break;
                        case "Câu hỏi":
                            new QuestionForm().setVisible(true);
                            break;
                        case "Âm thanh":
                            // mở form âm thanh nếu có
                            break;
                        case "Export":
                            new ExportForm().setVisible(true);
                            break;
                        case "Thống kê":
                            // mở form thống kê nếu có
                            break;
                    }
                }
            }
        });

        menuList.setSelectedIndex(0);
    }

//    private void loadExams() {
//        examTableModel.setRowCount(0);
//        try {
//            List<DeThi> list = deThiDAO.findAll();
//            for (DeThi dt : list) {
//                examTableModel.addRow(new Object[]{
//                        dt.getId(), dt.getName(), dt.getDate(), deThiDAO.countQuestions(dt.getId())
//                });
//            }
//        } catch (SQLException ex) {
//            JOptionPane.showMessageDialog(this, "Lỗi tải đề thi: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
//        }
//    }

    private void openSelectedExam() {
        int row = examTable.getSelectedRow();
        if (row >= 0) {
            Integer id = (Integer) examTableModel.getValueAt(row, 0);
            showExamEditor(id);
        }
    }

//    private void deleteSelectedExam() {
//        int row = examTable.getSelectedRow();
//        if (row >= 0 && JOptionPane.showConfirmDialog(this, "Xóa đề thi?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
//            Integer id = (Integer) examTableModel.getValueAt(row, 0);
//            try {
//                deThiDAO.delete(id);
//                loadExams();
//            } catch (SQLException ex) {
//                JOptionPane.showMessageDialog(this, "Xóa thất bại: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
//            }
//        }
//    }

    private void showExamEditor(Integer examId) {
        ExamEditor editor = new ExamEditor(examId);
        editor.setVisible(true);
    }

}
