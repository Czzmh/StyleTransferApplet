package com.demo.servers.impl;

import com.demo.database.data.Tuser;
import com.demo.database.mapper.ITuserMapper;
import com.demo.servers.IUserService;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Shen RuiJin
 * @createTime 2021/7/23 19:43
 */
@Service
public class UserServiceImpl implements IUserService {

    @Resource
    private ITuserMapper iTuserMapper;

    @Override
    public String query(Model model, String userName) throws Exception {
        //调用数据访问接口的查询方法
        List<Tuser> list = iTuserMapper.queryAll(userName);
        model.addAttribute("list", list);
        return "admin/user/userlist";
    }

    @Override
    public String addView(Model model) throws Exception {
        return "admin/user/userAddView";
    }

    @Override
    public String add(Model model, Tuser user) throws Exception {
        iTuserMapper.add(user);
        return query(model,null);
    }

    @Override
    public String deleting(Model model, String id) throws Exception {
        return query(model,null);
    }

    @Override
    public String update(Model model, Tuser user) throws Exception {
        //iTuserMapper.update(user);
        return query(model, null);
    }

    @Override
    public String updateView(Model model, Integer id) throws Exception {
        //FIXME:这里的user设置是否有问题？
        return "admin/user/userUpdateView";
    }
}
