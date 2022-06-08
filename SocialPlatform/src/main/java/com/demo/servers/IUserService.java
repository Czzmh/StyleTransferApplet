package com.demo.servers;

import com.demo.database.data.Tuser;
import org.springframework.ui.Model;

/**
 * 用户管理服务接口
 * @author Shen RuiJin
 * @createTime 2021/7/23 19:40
 */
public interface IUserService {

    /**
     * 查询用户数据
     * @param model
     * @param userName
     * @return
     * @throws Exception
     */
    public String query(Model model, String userName) throws Exception;

    public String addView(Model model) throws Exception;

    public String add(Model model, Tuser tuser) throws Exception;

    public String deleting(Model model, String id) throws Exception;

    public String update(Model model, Tuser user) throws Exception;

    public String updateView(Model model, Integer id) throws Exception;
}
