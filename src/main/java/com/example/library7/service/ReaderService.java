package com.example.library7.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.library7.common.BusinessException;
import com.example.library7.common.PageResult;
import com.example.library7.dto.ReaderSaveRequest;
import com.example.library7.entity.Admin;
import com.example.library7.entity.BorrowRecord;
import com.example.library7.entity.Reader;
import com.example.library7.mapper.AdminMapper;
import com.example.library7.mapper.BorrowRecordMapper;
import com.example.library7.mapper.ReaderMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class ReaderService {

    private final ReaderMapper readerMapper;
    private final AdminMapper adminMapper;
    private final BorrowRecordMapper borrowRecordMapper;

    public ReaderService(ReaderMapper readerMapper, AdminMapper adminMapper,
                         BorrowRecordMapper borrowRecordMapper) {
        this.readerMapper = readerMapper;
        this.adminMapper = adminMapper;
        this.borrowRecordMapper = borrowRecordMapper;
    }

    public PageResult<Reader> page(String keyword, long page, long size) {
        long safePage = Math.max(page, 1);
        long safeSize = Math.min(Math.max(size, 1), 100);

        LambdaQueryWrapper<Reader> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(q -> q.like(Reader::getUsername, keyword)
                    .or().like(Reader::getName, keyword)
                    .or().like(Reader::getPhone, keyword));
        }
        wrapper.orderByDesc(Reader::getId);

        List<Reader> all = readerMapper.selectList(wrapper);
        int from = (int) Math.min((safePage - 1) * safeSize, all.size());
        int to = (int) Math.min(from + safeSize, all.size());
        return new PageResult<>(all.subList(from, to), all.size(), safePage, safeSize);
    }

    public Reader create(ReaderSaveRequest request) {
        ensureUsernameUnique(request.getUsername(), null);
        Reader reader = new Reader();
        reader.setUsername(request.getUsername().trim());
        reader.setPassword(StringUtils.hasText(request.getPassword()) ? request.getPassword() : "123456");
        reader.setName(request.getName().trim());
        reader.setPhone(request.getPhone());
        reader.setEmail(request.getEmail());
        reader.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        readerMapper.insert(reader);
        return readerMapper.selectById(reader.getId());
    }

    public Reader update(Long id, ReaderSaveRequest request) {
        Reader reader = readerMapper.selectById(id);
        if (reader == null) {
            throw new BusinessException(404, "读者不存在");
        }
        ensureUsernameUnique(request.getUsername(), id);
        reader.setUsername(request.getUsername().trim());
        reader.setName(request.getName().trim());
        reader.setPhone(request.getPhone());
        reader.setEmail(request.getEmail());
        reader.setStatus(request.getStatus());
        if (StringUtils.hasText(request.getPassword())) {
            reader.setPassword(request.getPassword());
        }
        readerMapper.updateById(reader);
        return readerMapper.selectById(id);
    }

    public void delete(Long id) {
        Reader reader = readerMapper.selectById(id);
        if (reader == null) {
            throw new BusinessException(404, "读者不存在");
        }
        Long active = borrowRecordMapper.selectCount(new LambdaQueryWrapper<BorrowRecord>()
                .eq(BorrowRecord::getReaderId, id)
                .eq(BorrowRecord::getStatus, "BORROWED"));
        if (active > 0) {
            throw new BusinessException(409, "该读者仍有未归还的图书，无法删除");
        }
        // 无未归还记录时，连同历史借阅记录一并清理，避免外键约束阻止删除
        borrowRecordMapper.delete(new LambdaQueryWrapper<BorrowRecord>()
                .eq(BorrowRecord::getReaderId, id));
        readerMapper.deleteById(id);
    }

    private void ensureUsernameUnique(String username, Long excludeId) {
        LambdaQueryWrapper<Reader> wrapper = new LambdaQueryWrapper<Reader>()
                .eq(Reader::getUsername, username.trim());
        if (excludeId != null) {
            wrapper.ne(Reader::getId, excludeId);
        }
        boolean exists = readerMapper.selectCount(wrapper) > 0
                || adminMapper.selectCount(new LambdaQueryWrapper<Admin>()
                .eq(Admin::getUsername, username.trim())) > 0;
        if (exists) {
            throw new BusinessException(409, "账号已存在，请更换");
        }
    }
}
