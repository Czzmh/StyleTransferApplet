package com.demo.servers;

import org.springframework.ui.Model;
import org.springframework.web.bind.support.SessionStatus;

import javax.servlet.http.HttpSession;

/**
 * @author Shen RuiJin
 */
public interface ILoginService {
    public String login(Model model, String userName, String password) throws Exception;
    public String logout(Model model, HttpSession session, SessionStatus sessionStatus) throws Exception;
}
