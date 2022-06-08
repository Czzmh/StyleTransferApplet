package com.demo.database.data;

import java.sql.Date;

/**
 * 表t_user（用户）的持久化类
 * @author Shen RuiJin
 * @createTime 2021/7/23 15:50
 */
public class Tuser {

    private String name;
    private String password;
    private String identity;
    private String gender;
    private Date birthday;
    private String mailbox;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public String getMailbox() {
        return mailbox;
    }

    public void setMailbox(String mailbox) {
        this.mailbox = mailbox;
    }


}
