package com.demo.database.mapper;

import com.demo.database.data.Tuser;
import com.demo.database.data.Tuser_face;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * MVC模式：数据访问层（业务模型）
 * @author Shen RuiJin
 * @createTime 2021/7/23 15:53
 */
public interface ITuserMapper {

    /**
     *
     * @param userName 根据用户名@<code>userName</code>查询，当@<code>userName</code>为@<code>null</code>则查询所有用户
     * @return
     * @throws Exception
     */
    @Select("<script>" +
            "SELECT * FROM users " +
            "<where>" +
            "<if test = 'name != null'>" +
            "name like '%${name}%' " +
            "</if>" +
            "</where>" +
            "</script>")
    public List<Tuser> queryAll(String userName) throws Exception;


    @Select("SELECT face FROM users WHERE name = #{name}")
    public Tuser_face queryFace(String name) throws Exception;

    @Select("SELECT * FROM users WHERE name = #{name} AND password = #{password};")
    public Tuser queryBy(@Param("name") String userName,
                         @Param("password") String password) throws Exception;



    @Insert("insert into users(name, password, identity, gender, birthday,mailbox, face)"
            +" values(#{name}, #{password}, #{identity}, #{gender}, #{birthday}, #{mailbox}, null)")
    public void add(Tuser user) throws Exception;

    /**
     * 修改用户
     * @param user
     * @throws Exception
     */
    @Update("update users set name=#{user.name}, password=#{user.password}, identity=#{user.identity}, " +
            " gender=#{user.gender}, birthday=#{user.birthday}, mailbox=#{user.mailbox} " +
            " where name=#{prevName}")
    public void update(@Param("user") Tuser user,@Param("prevName") String prevName) throws Exception;


    @Update("update users set face = #{img} where name = #{name}")
    public void updateface(@Param("img") String img, @Param("name") String name) throws Exception;

    /**
     * 删除用户
     * @param names
     * @throws Exception
     */
    @Delete("DELETE FROM users WHERE name in(${ids})")
    public void deleteUser(String names) throws Exception;
}
