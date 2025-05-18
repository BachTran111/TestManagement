package app.NganHangDe.Model;

public class DeThiChiTiet {
    private Integer id;
    private Integer deThiId;
    private Integer cauHoiId;
    private Integer questionNumber;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getDeThiId() {
        return deThiId;
    }

    public void setDeThiId(Integer deThiId) {
        this.deThiId = deThiId;
    }

    public Integer getCauHoiId() {
        return cauHoiId;
    }

    public void setCauHoiId(Integer cauHoiId) {
        this.cauHoiId = cauHoiId;
    }

    public Integer getQuestionNumber() {
        return questionNumber;
    }

    public void setQuestionNumber(Integer questionNumber) {
        this.questionNumber = questionNumber;
    }
}
