package com.demo.database.mapper;

import com.demo.database.data.TComment;
import com.demo.database.data.TPassage;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author Shen RuiJin
 */
public interface ITCommentMapper {

    @Select("SELECT * FROM comments WHERE passage_id = #{passageId}")
    public List<TPassage> query_comment_by_passage(int passageId) throws Exception;

    @Select(" <script>" +
            " SELECT comments.*, passages.title as title" +
            " FROM comments, passages " +
            " <where>" +
            " <if test = 'keyWord != null'>" +
            " comment like '%${keyWord}%'" +
            " </if>" +
            " AND user_name = '${userName}'" +
            " AND passages.id = comments.passage_id" +
            " </where>" +
            " ORDER BY pub_time DESC" +
            " </script>")
    public List<TComment> query_comment_by_user(@Param("userName") String userName,@Param("keyWord") String keyWord) throws Exception;

    @Insert("INSERT INTO comments(passage_id, user_name, comment, pub_time)" +
            " VALUES(#{passageId}, #{userName}, #{comment}, #{pubTime})")
    public void insert_comment(TComment cmt) throws Exception;

    @Select("SELECT * FROM comments WHERE id=#{id}")
    public TComment query_comment_by_id(int id) throws Exception;

    @Update("UPDATE comments SET comment=#{comment}, pub_time=#{pubTime} WHERE id=#{id}")
    public void update_comment_content(TComment cmt) throws Exception;

    @Delete("DELETE FROM comments WHERE id in (${ids})")
    public void delele_comment(@Param("ids") String ids) throws Exception;
}
