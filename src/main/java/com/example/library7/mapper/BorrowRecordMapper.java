package com.example.library7.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.library7.entity.BorrowRecord;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface BorrowRecordMapper extends BaseMapper<BorrowRecord> {

    @Select("SELECT br.*, b.title AS book_title, r.name AS reader_name, r.username AS reader_username " +
            "FROM borrow_record br " +
            "LEFT JOIN book b ON br.book_id = b.id " +
            "LEFT JOIN reader r ON br.reader_id = r.id " +
            "WHERE br.reader_id = #{readerId} ORDER BY br.id DESC")
    List<BorrowRecord> selectByReader(@Param("readerId") Long readerId);

    @Select("<script>" +
            "SELECT br.*, b.title AS book_title, r.name AS reader_name, r.username AS reader_username " +
            "FROM borrow_record br " +
            "LEFT JOIN book b ON br.book_id = b.id " +
            "LEFT JOIN reader r ON br.reader_id = r.id " +
            "<where>" +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (b.title LIKE CONCAT('%', #{keyword}, '%') " +
            "OR r.name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR r.username LIKE CONCAT('%', #{keyword}, '%'))" +
            "</if>" +
            "</where>" +
            "ORDER BY br.id DESC" +
            "</script>")
    List<BorrowRecord> selectAllWithDetail(@Param("keyword") String keyword);
}
