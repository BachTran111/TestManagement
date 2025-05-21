package app.NganHangDe.Model;

import java.util.List;

public class CauHoi {
    private Integer id;
    private String content;
    private Integer amThanhId;
    private String type;
    private List<DapAn> dapAns;
    private AmThanh amThanh;

    public CauHoi(){}

    public CauHoi(Integer id, String content, String type, Integer amThanhId) {
        this.id = id;
        this.content = content;
        this.type = type;
        this.amThanhId = amThanhId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getAmThanhId() {
        return amThanhId;
    }

    public void setAmThanhId(Integer amThanhId) {
        this.amThanhId = amThanhId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<DapAn> getDapAns() {
        return dapAns;
    }

    public void setDapAns(List<DapAn> dapAns) {
        this.dapAns = dapAns;
    }

    public AmThanh getAmThanh() {
        return amThanh;
    }

    public void setAmThanh(AmThanh amThanh) {
        this.amThanh = amThanh;
    }
}