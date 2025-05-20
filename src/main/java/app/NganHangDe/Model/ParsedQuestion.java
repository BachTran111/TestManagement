package app.NganHangDe.Model;

import java.util.ArrayList;
import java.util.List;

public class ParsedQuestion {
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

