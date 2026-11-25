package com.example.library7.controller;

import com.example.library7.common.ApiResponse;
import com.example.library7.common.PageResult;
import com.example.library7.entity.Book;
import com.example.library7.service.AuthService;
import com.example.library7.service.BookService;
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

import java.util.Map;

@RestController
@RequestMapping("/api/books")
@Tag(name = "02. 图书管理", description = "图书查询（公开）与图书增删改（管理员）")
public class BookController {

    private final BookService bookService;
    private final AuthService authService;

    public BookController(BookService bookService, AuthService authService) {
        this.bookService = bookService;
        this.authService = authService;
    }

    @GetMapping
    @Operation(summary = "分页查询图书", description = "支持按书名、作者、ISBN 模糊搜索")
    public ApiResponse<PageResult<Book>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size) {
        return ApiResponse.ok(bookService.page(keyword, page, size));
    }

    @GetMapping("/stats")
    @Operation(summary = "馆藏统计")
    public ApiResponse<Map<String, Object>> stats() {
        return ApiResponse.ok(bookService.stats());
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询图书详情")
    public ApiResponse<Book> detail(@PathVariable Long id) {
        return ApiResponse.ok(bookService.getById(id));
    }

    @PostMapping
    @Operation(summary = "新增图书（管理员）")
    public ApiResponse<Book> create(@Valid @RequestBody Book book, HttpSession session) {
        authService.requireAdmin(session);
        return ApiResponse.ok(bookService.create(book));
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改图书（管理员）")
    public ApiResponse<Book> update(@PathVariable Long id, @Valid @RequestBody Book book,
                                    HttpSession session) {
        authService.requireAdmin(session);
        return ApiResponse.ok(bookService.update(id, book));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除图书（管理员）")
    public ApiResponse<Void> delete(@PathVariable Long id, HttpSession session) {
        authService.requireAdmin(session);
        bookService.delete(id);
        return ApiResponse.ok();
    }
}
