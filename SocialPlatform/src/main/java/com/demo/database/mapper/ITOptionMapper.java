package com.demo.database.mapper;

import com.demo.database.data.TOption;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * @author Shen RuiJin
 * @createTime 2021/7/28 11:24
 */
public interface ITOptionMapper {

    @Select("SELECT * FROM options WHERE user_name=#{userName}")
    public TOption query_by_user_name(String userName) throws Exception;

    @Insert("INSERT INTO options(user_name) values(#{userName})")
    public void create_new_user(String userName) throws Exception;

    @Update("UPDATE options SET spd=#{spd}, pit=#{pit}, per=#{per} WHERE user_name=#{userName}")
    public void updateOption(TOption option) throws Exception;
}
