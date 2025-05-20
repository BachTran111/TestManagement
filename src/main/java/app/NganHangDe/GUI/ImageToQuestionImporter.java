package app.NganHangDe.GUI;

import app.NganHangDe.DAO.CauHoiDAO;
import app.NganHangDe.DAO.DapAnDAO;
import app.NganHangDe.Model.CauHoi;
import app.NganHangDe.Model.DapAn;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import app.NganHangDe.Model.ParsedQuestion;
import app.NganHangDe.Model.ParsedOption;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.*;
import java.util.concurrent.ExecutionException;

public class ImageToQuestionImporter extends JDialog {
    private final CauHoiDAO cauHoiDAO;
    private final DapAnDAO dapAnDAO;
    private JProgressBar progressBar;
    private JTextArea logArea;

    public ImageToQuestionImporter(JFrame parent){
        super(parent, "Import Câu hỏi từ Ảnh", true);
        this.cauHoiDAO = new CauHoiDAO();
        this.dapAnDAO = new DapAnDAO();
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setSize(500, 400);

        // Main panel
        JButton btnSelectImage = new JButton("Chọn ảnh đề thi");
        btnSelectImage.addActionListener(this::handleImageSelection);

        progressBar = new JProgressBar();
        progressBar.setVisible(false);

        logArea = new JTextArea();
        logArea.setEditable(false);

        JPanel topPanel = new JPanel();
        topPanel.add(btnSelectImage);
        topPanel.add(progressBar);

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(logArea), BorderLayout.CENTER);

        JButton btnClose = new JButton("Đóng");
        btnClose.addActionListener(e -> dispose());
        add(btnClose, BorderLayout.SOUTH);
    }

    private void handleImageSelection(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Image files", "jpg", "jpeg", "png", "bmp"));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();
            new SwingWorker<Void, String>() {
                @Override
                protected Void doInBackground() throws Exception {
                    try {
                        publish("Đang xử lý ảnh: " + selectedFile.getName());
                        progressBar.setVisible(true);
                        progressBar.setIndeterminate(true);

                        String text = extractTextFromImage(selectedFile);
                        publish("Đã trích xuất text từ ảnh\nĐang phân tích câu hỏi...");

                        List<ParsedQuestion> questions = parseQuestions(text);
                        publish("Tìm thấy " + questions.size() + " câu hỏi\nĐang lưu vào CSDL...");

                        SwingUtilities.invokeLater(() -> {
                            new QuestionReviewDialog(
                                    (Frame) SwingUtilities.getWindowAncestor(ImageToQuestionImporter.this),
                                    questions
                            ).setVisible(true);
                        });

                    } catch (Exception ex) {
                        publish("Lỗi: " + ex.getMessage());
                        throw ex;
                    }
                    return null;
                }

                @Override
                protected void process(List<String> chunks) {
                    chunks.forEach(log -> logArea.append(log + "\n"));
                }

                @Override
                protected void done() {
                    progressBar.setVisible(false);
                    try {
                        get(); // Check for exceptions
                    } catch (InterruptedException | ExecutionException ex) {
                        JOptionPane.showMessageDialog(ImageToQuestionImporter.this,
                                "Lỗi khi xử lý: " + ex.getCause().getMessage(),
                                "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();
        }
    }

    private String extractTextFromImage(File imageFile) throws TesseractException, IOException {
        File tempDir = Files.createTempDirectory("tessdata").toFile();
        File jpnFile = new File(tempDir, "jpn.traineddata");
        File engFile = new File(tempDir, "eng.traineddata");

        // Copy từ resources vào thư mục tạm
        try (InputStream in = getClass().getResourceAsStream("/tessdata/jpn.traineddata");
             OutputStream out = new FileOutputStream(jpnFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }

        try (InputStream in2 = getClass().getResourceAsStream("/tessdata/eng.traineddata");
             OutputStream out = new FileOutputStream(engFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in2.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }

        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath(tempDir.getAbsolutePath());
        tesseract.setLanguage("jpn+eng");

        tesseract.setTessVariable("preserve_interword_spaces", "1");
        tesseract.setTessVariable("user_defined_dpi", "400");

        String result = tesseract.doOCR(imageFile);
        jpnFile.delete();
        engFile.delete();
        tempDir.delete();
        logArea.append("Kết quả OCR thô:\n" + result + "\n");
        return result;
    }

    private List<ParsedQuestion> parseQuestions(String text) {
        List<ParsedQuestion> questions = new ArrayList<>();

        // 1) Split toàn bộ OCR text thành từng block, mỗi block bắt đầu bằng '['
        //    (?=\\[) là lookahead, giữ lại '[' ở đầu mỗi phần.
        String[] rawBlocks = text.split("(?=\\[)");

        // Regex để tìm từng cặp (circled‑number + phần text theo sau)
        Pattern optPat = Pattern.compile("([\\u2460-\\u2473])([^\\u2460-\\u2473\\[\\]]+)");

        for (String raw : rawBlocks) {
            raw = raw.trim();
            if (!raw.startsWith("[")) continue;

            // 2) Lấy tất cả các option
            Matcher mOpt = optPat.matcher(raw);
            List<ParsedOption> opts = new ArrayList<>();
            while (mOpt.find()) {
                String content = mOpt.group(2).trim();
                if (!content.isEmpty()) {
                    ParsedOption o = new ParsedOption();
                    o.setContent(content);
                    // TODO: nếu OCR có dấu ✓/* thì setCorrect(true) ở đây
                    o.setCorrect(false);
                    opts.add(o);
                }
            }
            if (opts.isEmpty()) {
                // nếu block này không có option nào thì bỏ qua
                continue;
            }

            // 3) Lấy questionText bằng cách xóa hết phần “[” và các circled‑number+option
            String questionText = raw
                    // xóa các circled-number và text theo sau
                    .replaceAll("[\\u2460-\\u2473][^\\u2460-\\u2473\\[\\]]+", "")
                    // xóa dấu '[' đầu
                    .replaceFirst("^\\[", "")
                    .trim();

            // 4) Tạo ParsedQuestion và thêm vào list
            ParsedQuestion pq = new ParsedQuestion();
            pq.setQuestionText(questionText);
            pq.setOptions(opts);
            questions.add(pq);

            // Debug log
            logArea.append("– Câu hỏi: " + questionText + "\n");
            for (int i = 0; i < opts.size(); i++) {
                logArea.append("   • Opt " + (i+1) + ": " + opts.get(i).getContent() + "\n");
            }
        }

        if (questions.isEmpty()) {
            logArea.append("⚠️ Không tìm thấy câu hỏi nào!\n");
        }
        return questions;
    }


//    private void saveQuestionsToDatabase(List<ParsedQuestion> questions) throws Exception {
//        try {
//            for (ParsedQuestion pq : questions) {
//                CauHoi ch = new CauHoi();
//                ch.setContent(pq.getQuestionText());
//                ch.setType("TRAC_NGHIEM");
//                cauHoiDAO.create(ch);
//
//                boolean hasCorrectAnswer = false;
//                for (ParsedOption po : pq.getOptions()) {
//                    DapAn da = new DapAn();
//                    da.setCauHoiId(ch.getId());
//                    da.setContent(po.getContent());
//                    da.setCorrect(po.isCorrect());
//                    if (po.isCorrect()) hasCorrectAnswer = true;
//                    dapAnDAO.create(da);
//                }
//
//                if (!hasCorrectAnswer && !pq.getOptions().isEmpty()) {
//                    // Mặc định chọn option đầu tiên nếu không xác định được đáp án đúng
//                    DapAn da = new DapAn();
//                    da.setCauHoiId(ch.getId());
//                    da.setContent(pq.getOptions().get(0).getContent());
//                    da.setCorrect(true);
//                    dapAnDAO.update(da);
//                }
//            }
//        } catch (Exception e) {
//            throw new Exception("Lỗi khi lưu vào CSDL: " + e.getMessage());
//        }
//    }

    private void saveQuestionsToDatabase(List<ParsedQuestion> questions) throws Exception {
        try {
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
        } catch (Exception e) {
            throw new Exception("Lỗi khi lưu vào CSDL: " + e.getMessage());
        }
    }

    // Helper classes
}