package app.NganHangDe.Model;

public class DeXuatAI {
    private Integer id;
    private Integer cauHoiId;
    private String inputText;
    private String suggestedAnswer;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCauHoiId() {
        return cauHoiId;
    }

    public void setCauHoiId(Integer cauHoiId) {
        this.cauHoiId = cauHoiId;
    }

    public String getInputText() {
        return inputText;
    }

    public void setInputText(String inputText) {
        this.inputText = inputText;
    }

    public String getSuggestedAnswer() {
        return suggestedAnswer;
    }

    public void setSuggestedAnswer(String suggestedAnswer) {
        this.suggestedAnswer = suggestedAnswer;
    }
}
