package com.demo.database.data;

/**
 * @author Shen RuiJin
 * @createTime 2021/7/28 11:22
 */
public class TOption {
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getSpd() {
        return spd;
    }

    public void setSpd(int spd) {
        this.spd = spd;
    }

    public int getPit() {
        return pit;
    }

    public void setPit(int pit) {
        this.pit = pit;
    }

    public int getPer() {
        return per;
    }

    public void setPer(int per) {
        this.per = per;
    }

    private String userName;
    private int  spd;
    private int pit;
    private int per;

    public TOption(){
        spd = 5;
        pit = 5;
        per = 0;
    }
}
