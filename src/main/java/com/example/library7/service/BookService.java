package com.example.library7.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.library7.common.BusinessException;
import com.example.library7.common.PageResult;
import com.example.library7.entity.Book;
import com.example.library7.entity.BorrowRecord;
import com.example.library7.mapper.BookMapper;
import com.example.library7.mapper.BorrowRecordMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class BookService {

    private final BookMapper bookMapper;
    private final BorrowRecordMapper borrowRecordMapper;

    public BookService(BookMapper bookMapper, BorrowRecordMapper borrowRecordMapper) {
        this.bookMapper = bookMapper;
        this.borrowRecordMapper = borrowRecordMapper;
    }

    public PageResult<Book> page(String keyword, long page, long size) {
        long safePage = Math.max(page, 1);
        long safeSize = Math.min(Math.max(size, 1), 100);

        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(q -> q.like(Book::getTitle, keyword)
                    .or().like(Book::getAuthor, keyword)
                    .or().like(Book::getIsbn, keyword));
        }
        wrapper.orderByDesc(Book::getId);

        List<Book> all = bookMapper.selectList(wrapper);
        int from = (int) Math.min((safePage - 1) * safeSize, all.size());
        int to = (int) Math.min(from + safeSize, all.size());
        return new PageResult<>(all.subList(from, to), all.size(), safePage, safeSize);
    }

    public Book getById(Long id) {
        Book book = bookMapper.selectById(id);
        if (book == null) {
            throw new BusinessException(404, "图书不存在");
        }
        return book;
    }

    public Book create(Book book) {
        ensureIsbnUnique(book.getIsbn(), null);
        if (book.getPrice() == null) {
            book.setPrice(java.math.BigDecimal.ZERO);
        }
        book.setAvailableCopies(book.getTotalCopies());
        bookMapper.insert(book);
        return bookMapper.selectById(book.getId());
    }

    public Book update(Long id, Book request) {
        Book old = getById(id);
        ensureIsbnUnique(request.getIsbn(), id);

        int borrowed = old.getTotalCopies() - old.getAvailableCopies();
        int newAvailable = request.getTotalCopies() - borrowed;
        if (newAvailable < 0) {
            throw new BusinessException(400, "馆藏总数不能小于当前借出数量（" + borrowed + " 本）");
        }

        request.setId(id);
        request.setAvailableCopies(newAvailable);
        bookMapper.updateById(request);
        return getById(id);
    }

    public void delete(Long id) {
        getById(id);
        Long active = borrowRecordMapper.selectCount(new LambdaQueryWrapper<BorrowRecord>()
                .eq(BorrowRecord::getBookId, id)
                .eq(BorrowRecord::getStatus, "BORROWED"));
        if (active > 0) {
            throw new BusinessException(409, "该图书仍有未归还的借阅记录，无法删除");
        }
        // 无未归还记录时，连同历史借阅记录一并清理，避免外键约束阻止删除
        borrowRecordMapper.delete(new LambdaQueryWrapper<BorrowRecord>()
                .eq(BorrowRecord::getBookId, id));
        bookMapper.deleteById(id);
    }

    public Map<String, Object> stats() {
        List<Book> books = bookMapper.selectList(null);
        int titles = books.size();
        int totalCopies = books.stream().mapToInt(Book::getTotalCopies).sum();
        int available = books.stream().mapToInt(Book::getAvailableCopies).sum();
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("titles", titles);
        stats.put("totalCopies", totalCopies);
        stats.put("available", available);
        stats.put("borrowed", totalCopies - available);
        return stats;
    }

    private void ensureIsbnUnique(String isbn, Long excludeId) {
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<Book>().eq(Book::getIsbn, isbn);
        if (excludeId != null) {
            wrapper.ne(Book::getId, excludeId);
        }
        if (bookMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(409, "ISBN 已存在，请检查后重新填写");
        }
    }
}
