package app.NganHangDe.Model;

import java.sql.Date;
import java.util.List;

public class DeThi {
    private int id;
    private String name;
    private String description;
    private Date date;
    private List<DeThiChiTiet> chiTietList;

    public DeThi() {}

    public DeThi(int id, String name, String description, Date date) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public List<DeThiChiTiet> getChiTietList() {
        return chiTietList;
    }

    public void setChiTietList(List<DeThiChiTiet> chiTietList) {
        this.chiTietList = chiTietList;
    }
}
