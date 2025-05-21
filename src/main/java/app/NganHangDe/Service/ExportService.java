package app.NganHangDe.Service;

import app.NganHangDe.DAO.CauHoiDAO;
import app.NganHangDe.DAO.DapAnDAO;
import app.NganHangDe.DAO.DeThiChiTietDAO;
import app.NganHangDe.Model.*;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

import java.io.FileOutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExportService {
    private final CauHoiDAO cauHoiDAO = new CauHoiDAO();
    private final DapAnDAO dapAnDAO = new DapAnDAO();
    private final DeThiChiTietDAO deThiChiTietDAO = new DeThiChiTietDAO();

    public void exportToDocx(DeThi deThi, String filePath) throws Exception {
        try (XWPFDocument document = new XWPFDocument()) {
            // Load toàn bộ chi tiết đề thi và sắp xếp
            List<DeThiChiTiet> chiTietList = deThiChiTietDAO.findByDeThiId(deThi.getId()).stream()
                    .sorted(Comparator.comparingInt(DeThiChiTiet::getQuestionNumber))
                    .collect(Collectors.toList());

            // Load đầy đủ thông tin câu hỏi + đáp án
            List<CauHoi> cauHois = new ArrayList<>();
            for (DeThiChiTiet chiTiet : chiTietList) {
                CauHoi cauHoi = cauHoiDAO.findById(chiTiet.getCauHoiId());
                cauHoi.setDapAns(dapAnDAO.findByCauHoiId(cauHoi.getId()));
                cauHois.add(cauHoi);
            }

            // Phân loại câu hỏi
            Map<String, List<CauHoi>> questionsByType = cauHois.stream()
                    .collect(Collectors.groupingBy(CauHoi::getType));

            // Tạo các phần
            createSection(document, "I. VOCABULARY", questionsByType.get("Vocabulary"));
            createSection(document, "II. READING", questionsByType.get("Reading"));
            createSection(document, "III. LISTENING", questionsByType.get("Listening"));

            // Thêm đáp án
            addAnswerPage(document, cauHois);

            try (FileOutputStream out = new FileOutputStream(filePath)) {
                document.write(out);
            }
        }
    }

    private void createSection(XWPFDocument document, String title, List<CauHoi> questions) {
        if (questions == null || questions.isEmpty()) return;

        // Thêm tiêu đề section
        XWPFParagraph titlePara = document.createParagraph();
        titlePara.setStyle("Heading1");
        XWPFRun titleRun = titlePara.createRun();
        titleRun.setText(title);
        titleRun.setBold(true);
        titleRun.setFontSize(14);
        addEmptyLine(titleRun, 1);

        // Thêm từng câu hỏi
        int questionNumber = 1;
        for (CauHoi cauHoi : questions) {
            addQuestion(document, questionNumber++, cauHoi);
        }
    }

    private void addQuestion(XWPFDocument document, int number, CauHoi cauHoi) {
        XWPFParagraph para = document.createParagraph();

        // Số câu hỏi
        XWPFRun numberRun = para.createRun();
        numberRun.setText(number + ". ");
        numberRun.setBold(true);

        // Nội dung câu hỏi
        XWPFRun contentRun = para.createRun();
        contentRun.setText(cauHoi.getContent());
        addEmptyLine(contentRun, 1);

        // Thêm các đáp án
        char optionChar = 'A';
        List<DapAn> dapAns = null;
// Lấy danh sách đáp án từ DAO thay vì gọi cauHoi.getDapAns()
        try {
            dapAns = dapAnDAO.findByCauHoiId(cauHoi.getId());
        }catch (Exception e){}
        for (DapAn dapAn : dapAns) {
            XWPFParagraph optionPara = document.createParagraph();
            optionPara.setIndentationLeft(360); // Thụt lề 0.25 inch

            XWPFRun optionRun = optionPara.createRun();
            optionRun.setText(optionChar++ + ". " + dapAn.getContent());
        }

        addEmptyLine(document.createParagraph().createRun(), 1);
    }

    private void addAnswerPage(XWPFDocument document, List<CauHoi> cauHois) throws Exception {
        // Ngắt trang
        XWPFParagraph pageBreak = document.createParagraph();
        pageBreak.setPageBreak(true);

        // Tiêu đề
        XWPFParagraph titlePara = document.createParagraph();
        titlePara.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun titleRun = titlePara.createRun();
        titleRun.setText("ĐÁP ÁN");
        titleRun.setBold(true);
        titleRun.setFontSize(16);
        titleRun.addBreak();

        // Danh sách đáp án
        XWPFTable table = document.createTable();
        table.setWidth("100%");

        // Tạo header
        XWPFTableRow headerRow = table.getRow(0);
        headerRow.getCell(0).setText("Câu hỏi");
        headerRow.addNewTableCell().setText("Đáp án đúng");

        // Thêm dữ liệu
        int stt = 1;
        for (CauHoi cauHoi : cauHois) {
            XWPFTableRow row = table.createRow();
            row.getCell(0).setText(String.valueOf(stt++));
            row.getCell(1).setText(getCorrectAnswer(cauHoi));
        }
    }

    private String getCorrectAnswer(CauHoi cauHoi) {
        char option = 'A';
        for (DapAn dapAn : cauHoi.getDapAns()) {
            if (dapAn.getCorrect()) {
                return String.valueOf(option);
            }
            option++;
        }
        return "N/A";
    }

    private void addEmptyLine(XWPFRun run, int count) {
        for (int i = 0; i < count; i++) {
            run.addBreak();
        }
    }
}
