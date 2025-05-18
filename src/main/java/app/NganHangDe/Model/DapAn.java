package app.NganHangDe.Model;

public class DapAn {
    private Integer id;
    private Integer cauHoiId;
    private String content;
    private Boolean isCorrect;

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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Boolean getCorrect() {
        return isCorrect;
    }

    public void setCorrect(Boolean correct) {
        isCorrect = correct;
    }
}
