package com.example.library7.controller;

import com.example.library7.common.ApiResponse;
import com.example.library7.common.LoginUser;
import com.example.library7.dto.LoginRequest;
import com.example.library7.dto.RegisterRequest;
import com.example.library7.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "01. 认证管理", description = "读者注册、登录、退出与当前登录用户")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "登录", description = "管理员与读者共用登录入口，返回角色 ADMIN / READER")
    public ApiResponse<Map<String, Object>> login(@Valid @RequestBody LoginRequest request,
                                                  HttpSession session) {
        LoginUser user = authService.login(request.getUsername(), request.getPassword(), session);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("name", user.getName());
        data.put("role", user.getRole());
        return ApiResponse.ok(data);
    }

    @PostMapping("/register")
    @Operation(summary = "读者注册")
    public ApiResponse<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ApiResponse.ok();
    }

    @PostMapping("/logout")
    @Operation(summary = "退出登录")
    public ApiResponse<Void> logout(HttpSession session) {
        authService.logout(session);
        return ApiResponse.ok();
    }

    @GetMapping("/me")
    @Operation(summary = "获取当前登录用户", description = "未登录时返回 data 为 null")
    public ApiResponse<LoginUser> me(HttpSession session) {
        return ApiResponse.ok(authService.current(session));
    }
}
