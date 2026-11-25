package com.example.library7.controller;

import com.example.library7.common.ApiResponse;
import com.example.library7.common.PageResult;
import com.example.library7.dto.ReaderSaveRequest;
import com.example.library7.entity.Reader;
import com.example.library7.service.AuthService;
import com.example.library7.service.ReaderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/readers")
@Tag(name = "03. 读者管理", description = "管理员维护读者信息（增删改查）")
public class ReaderController {

    private final ReaderService readerService;
    private final AuthService authService;

    public ReaderController(ReaderService readerService, AuthService authService) {
        this.readerService = readerService;
        this.authService = authService;
    }

    @GetMapping
    @Operation(summary = "分页查询读者（管理员）")
    public ApiResponse<PageResult<Reader>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            HttpSession session) {
        authService.requireAdmin(session);
        return ApiResponse.ok(readerService.page(keyword, page, size));
    }

    @PostMapping
    @Operation(summary = "新增读者（管理员）", description = "未填写密码时默认 123456")
    public ApiResponse<Reader> create(@Valid @RequestBody ReaderSaveRequest request,
                                      HttpSession session) {
        authService.requireAdmin(session);
        return ApiResponse.ok(readerService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改读者（管理员）", description = "密码留空表示不修改")
    public ApiResponse<Reader> update(@PathVariable Long id,
                                      @Valid @RequestBody ReaderSaveRequest request,
                                      HttpSession session) {
        authService.requireAdmin(session);
        return ApiResponse.ok(readerService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除读者（管理员）")
    public ApiResponse<Void> delete(@PathVariable Long id, HttpSession session) {
        authService.requireAdmin(session);
        readerService.delete(id);
        return ApiResponse.ok();
    }
}
