package com.example.library7.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.library7.common.BusinessException;
import com.example.library7.common.LoginUser;
import com.example.library7.entity.Book;
import com.example.library7.entity.BorrowRecord;
import com.example.library7.entity.Reader;
import com.example.library7.mapper.BookMapper;
import com.example.library7.mapper.BorrowRecordMapper;
import com.example.library7.mapper.ReaderMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class BorrowService {

    private final BorrowRecordMapper borrowRecordMapper;
    private final BookMapper bookMapper;
    private final ReaderMapper readerMapper;

    public BorrowService(BorrowRecordMapper borrowRecordMapper, BookMapper bookMapper,
                         ReaderMapper readerMapper) {
        this.borrowRecordMapper = borrowRecordMapper;
        this.bookMapper = bookMapper;
        this.readerMapper = readerMapper;
    }

    @Transactional
    public BorrowRecord borrow(LoginUser user, Long bookId) {
        Reader reader = readerMapper.selectById(user.getId());
        if (reader == null || reader.getStatus() == null || reader.getStatus() != 1) {
            throw new BusinessException(403, "读者账号状态异常，无法借阅");
        }
        Book book = bookMapper.selectById(bookId);
        if (book == null) {
            throw new BusinessException(404, "图书不存在");
        }
        if (book.getAvailableCopies() <= 0) {
            throw new BusinessException(409, "该图书暂无可借库存");
        }
        Long active = borrowRecordMapper.selectCount(new LambdaQueryWrapper<BorrowRecord>()
                .eq(BorrowRecord::getReaderId, user.getId())
                .eq(BorrowRecord::getBookId, bookId)
                .eq(BorrowRecord::getStatus, "BORROWED"));
        if (active > 0) {
            throw new BusinessException(409, "您已借阅该书且尚未归还");
        }

        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookMapper.updateById(book);

        BorrowRecord record = new BorrowRecord();
        record.setReaderId(user.getId());
        record.setBookId(bookId);
        record.setBorrowDate(LocalDate.now());
        record.setDueDate(LocalDate.now().plusDays(30));
        record.setStatus("BORROWED");
        borrowRecordMapper.insert(record);
        return borrowRecordMapper.selectById(record.getId());
    }

    @Transactional
    public void returnBook(LoginUser user, Long recordId) {
        BorrowRecord record = borrowRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException(404, "借阅记录不存在");
        }
        if (!"BORROWED".equals(record.getStatus())) {
            throw new BusinessException(409, "该记录已归还，无需重复操作");
        }
        if (!user.isAdmin() && !record.getReaderId().equals(user.getId())) {
            throw new BusinessException(403, "只能归还自己的借阅记录");
        }

        record.setStatus("RETURNED");
        record.setReturnDate(LocalDate.now());
        borrowRecordMapper.updateById(record);

        Book book = bookMapper.selectById(record.getBookId());
        if (book != null) {
            int available = Math.min(book.getAvailableCopies() + 1, book.getTotalCopies());
            book.setAvailableCopies(available);
            bookMapper.updateById(book);
        }
    }

    public List<BorrowRecord> myRecords(Long readerId) {
        return borrowRecordMapper.selectByReader(readerId);
    }

    public List<BorrowRecord> allRecords(String keyword) {
        return borrowRecordMapper.selectAllWithDetail(keyword);
    }
}
