package com.example.library7.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.library7.common.BusinessException;
import com.example.library7.common.LoginUser;
import com.example.library7.dto.RegisterRequest;
import com.example.library7.entity.Admin;
import com.example.library7.entity.Reader;
import com.example.library7.mapper.AdminMapper;
import com.example.library7.mapper.ReaderMapper;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AuthService {

    public static final String SESSION_KEY = "loginUser";

    private final AdminMapper adminMapper;
    private final ReaderMapper readerMapper;

    public AuthService(AdminMapper adminMapper, ReaderMapper readerMapper) {
        this.adminMapper = adminMapper;
        this.readerMapper = readerMapper;
    }

    public LoginUser login(String username, String password, HttpSession session) {
        Admin admin = adminMapper.selectOne(
                new LambdaQueryWrapper<Admin>().eq(Admin::getUsername, username));
        if (admin != null) {
            if (!admin.getPassword().equals(password)) {
                throw new BusinessException(401, "账号或密码错误");
            }
            LoginUser user = new LoginUser(admin.getId(), admin.getUsername(), admin.getName(), "ADMIN");
            session.setAttribute(SESSION_KEY, user);
            return user;
        }

        Reader reader = readerMapper.selectOne(
                new LambdaQueryWrapper<Reader>().eq(Reader::getUsername, username));
        if (reader == null || !reader.getPassword().equals(password)) {
            throw new BusinessException(401, "账号或密码错误");
        }
        if (reader.getStatus() == null || reader.getStatus() != 1) {
            throw new BusinessException(403, "该读者账号已被禁用，请联系管理员");
        }
        LoginUser user = new LoginUser(reader.getId(), reader.getUsername(), reader.getName(), "READER");
        session.setAttribute(SESSION_KEY, user);
        return user;
    }

    public void register(RegisterRequest request) {
        if (!StringUtils.hasText(request.getUsername())) {
            throw new BusinessException(400, "账号不能为空");
        }
        boolean exists = readerMapper.selectCount(
                new LambdaQueryWrapper<Reader>().eq(Reader::getUsername, request.getUsername())) > 0
                || adminMapper.selectCount(
                new LambdaQueryWrapper<Admin>().eq(Admin::getUsername, request.getUsername())) > 0;
        if (exists) {
            throw new BusinessException(409, "该账号已被使用，请更换账号");
        }

        Reader reader = new Reader();
        reader.setUsername(request.getUsername().trim());
        reader.setPassword(request.getPassword());
        reader.setName(request.getName().trim());
        reader.setPhone(request.getPhone());
        reader.setEmail(request.getEmail());
        reader.setStatus(1);
        readerMapper.insert(reader);
    }

    public LoginUser current(HttpSession session) {
        return (LoginUser) session.getAttribute(SESSION_KEY);
    }

    public LoginUser requireLogin(HttpSession session) {
        LoginUser user = current(session);
        if (user == null) {
            throw new BusinessException(401, "请先登录");
        }
        return user;
    }

    public LoginUser requireReader(HttpSession session) {
        LoginUser user = requireLogin(session);
        if (!"READER".equals(user.getRole())) {
            throw new BusinessException(403, "仅读者可以执行该操作");
        }
        return user;
    }

    public LoginUser requireAdmin(HttpSession session) {
        LoginUser user = requireLogin(session);
        if (!"ADMIN".equals(user.getRole())) {
            throw new BusinessException(403, "仅管理员可以执行该操作");
        }
        return user;
    }

    public void logout(HttpSession session) {
        session.invalidate();
    }
}
