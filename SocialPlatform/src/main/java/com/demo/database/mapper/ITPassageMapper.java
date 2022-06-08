package com.demo.database.mapper;

import com.demo.database.data.TComment;
import com.demo.database.data.TPassage;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author Shen RuiJin
 * @createTime 2021/7/26 21:59
 */
public interface ITPassageMapper {

    @Select("<script>" +
            " SELECT passages.*, count(comments.id) as comment_num" +
            " FROM passages left outer join comments on passages.id = comments.passage_id"+
            " <where>" +
            " <if test = 'title != null'>" +
            " AND title like '%${title}%' " +
            " </if>" +
            " <if test = 'name != null'>" +
            " AND author = '${name}' " +
            " </if>"+
            " </where>"+
            " GROUP BY passages.id"+
            " ORDER BY passages.pub_time desc" +
            "</script>")
    public List<TPassage> query_all(@Param("title") String title, @Param("name") String username) throws Exception;

    @Select("SELECT * FROM passages WHERE id = #{id}")
    public TPassage query_by_id(@Param("id") int id) throws Exception;

    @Select("SELECT * FROM comments WHERE passage_id = #{passageId}")
    public List<TComment> query_comment_by_passage_id(int passageId) throws Exception;


    @Insert("INSERT INTO passages(author, title, content, tag, pub_time,imgurl)" +
            " VALUES(#{author},#{title},#{content}, #{tag}, #{pubTime}, #{imgurl})")
    public void addPsg(TPassage passage) throws Exception;

    @Delete("DELETE FROM passages WHERE id in(${ids})")
    public void delPsg(String ids) throws Exception;

    @Update(" UPDATE passages SET title=#{title}, content=#{content}, tag=#{tag}, pub_time=#{pubTime}, imgurl=#{imgurl}" +
            " WHERE id = #{id}")
    public void updatepsg(TPassage psg) throws Exception;
}

