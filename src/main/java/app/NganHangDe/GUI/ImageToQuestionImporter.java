package app.NganHangDe.GUI;

import app.NganHangDe.DAO.CauHoiDAO;
import app.NganHangDe.DAO.DapAnDAO;
import app.NganHangDe.Model.CauHoi;
import app.NganHangDe.Model.DapAn;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

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

                        saveQuestionsToDatabase(questions);
                        publish("Hoàn thành! Đã lưu " + questions.size() + " câu hỏi");
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
        logArea.append("Toàn bộ text trích xuất:\n" + text + "\n"); // Debug: Xem toàn bộ text OCR

        // Pattern cải tiến - linh hoạt hơn với nhiều định dạng
        Pattern pattern = Pattern.compile(
                "(?mi)^\\s*(\\d+)[.)]\\s*(.+?)(?=\\s*(?:\\d+[.)]|$))" + // Câu hỏi
                        "((?:\\s*[a-dA-D1-4][.)]\\s*.+?\\s*(?:\\n|$)){2,})" // Các đáp án
        );

        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            ParsedQuestion pq = new ParsedQuestion();
            pq.setQuestionText(matcher.group(2).trim());

            // Debug từng phần trích xuất
            logArea.append("Phát hiện câu hỏi: " + matcher.group(1) + "\n");
            logArea.append("Nội dung: " + pq.getQuestionText() + "\n");

            // Xử lý các đáp án
            String optionsText = matcher.group(3);
            Pattern optionPattern = Pattern.compile(
                    "(?mi)^\\s*([a-dA-D1-4])[.)]\\s*(.+?)(?:\\s*[✓*✔]\\s*)?$"
            );
            Matcher optionMatcher = optionPattern.matcher(optionsText);

            while (optionMatcher.find()) {
                ParsedOption opt = new ParsedOption();
                opt.setContent(optionMatcher.group(2).trim());
                opt.setCorrect(optionMatcher.group(0).matches(".*[✓*✔].*"));
                pq.getOptions().add(opt);

                // Debug đáp án
                logArea.append(" - Đáp án " + optionMatcher.group(1) + ": " + opt.getContent() +
                        (opt.isCorrect() ? " (ĐÚNG)" : "") + "\n");
            }

            questions.add(pq);
        }

        if (questions.isEmpty()) {
            logArea.append("CẢNH BÁO: Không tìm thấy câu hỏi nào phù hợp!\n");
            logArea.append("Gợi ý kiểm tra:\n");
            logArea.append("1. Định dạng câu hỏi có số thứ tự (1., 2.,...)\n");
            logArea.append("2. Đáp án có ký hiệu (a., b.,... hoặc 1., 2.,...)\n");
            logArea.append("3. Văn bản rõ ràng, không bị nhiễu\n");
        }

        return questions;
    }

    private void saveQuestionsToDatabase(List<ParsedQuestion> questions) throws Exception {
        try {
            for (ParsedQuestion pq : questions) {
                CauHoi ch = new CauHoi();
                ch.setContent(pq.getQuestionText());
                ch.setType("TRAC_NGHIEM");
                cauHoiDAO.create(ch);

                boolean hasCorrectAnswer = false;
                for (ParsedOption po : pq.getOptions()) {
                    DapAn da = new DapAn();
                    da.setCauHoiId(ch.getId());
                    da.setContent(po.getContent());
                    da.setCorrect(po.isCorrect());
                    if (po.isCorrect()) hasCorrectAnswer = true;
                    dapAnDAO.create(da);
                }

                if (!hasCorrectAnswer && !pq.getOptions().isEmpty()) {
                    // Mặc định chọn option đầu tiên nếu không xác định được đáp án đúng
                    DapAn da = new DapAn();
                    da.setCauHoiId(ch.getId());
                    da.setContent(pq.getOptions().get(0).getContent());
                    da.setCorrect(true);
                    dapAnDAO.update(da);
                }
            }
        } catch (Exception e) {
            throw new Exception("Lỗi khi lưu vào CSDL: " + e.getMessage());
        }
    }

    // Helper classes
    private static class ParsedQuestion {
        private String questionText;
        private List<ParsedOption> options = new ArrayList<>();

        public String getQuestionText() {
            return questionText;
        }

        public void setQuestionText(String questionText) {
            this.questionText = questionText;
        }

        public List<ParsedOption> getOptions() {
            return options;
        }

        public void setOptions(List<ParsedOption> options) {
            this.options = options;
        }
    }

    private static class ParsedOption {
        private String content;
        private boolean isCorrect;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public boolean isCorrect() {
            return isCorrect;
        }

        public void setCorrect(boolean correct) {
            isCorrect = correct;
        }
    }
}