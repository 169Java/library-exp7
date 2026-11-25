package com.example.library7.controller;

import com.example.library7.common.ApiResponse;
import com.example.library7.common.LoginUser;
import com.example.library7.entity.BorrowRecord;
import com.example.library7.service.AuthService;
import com.example.library7.service.BorrowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/borrows")
@Tag(name = "04. 借阅管理", description = "读者借阅、归还与借阅记录查询")
public class BorrowController {

    private final BorrowService borrowService;
    private final AuthService authService;

    public BorrowController(BorrowService borrowService, AuthService authService) {
        this.borrowService = borrowService;
        this.authService = authService;
    }

    @PostMapping
    @Operation(summary = "借阅图书（读者）")
    public ApiResponse<BorrowRecord> borrow(@RequestParam Long bookId, HttpSession session) {
        LoginUser user = authService.requireReader(session);
        return ApiResponse.ok(borrowService.borrow(user, bookId));
    }

    @PutMapping("/{id}/return")
    @Operation(summary = "归还图书", description = "读者归还自己的记录，管理员也可代为归还")
    public ApiResponse<Void> returnBook(@PathVariable Long id, HttpSession session) {
        LoginUser user = authService.requireLogin(session);
        borrowService.returnBook(user, id);
        return ApiResponse.ok();
    }

    @GetMapping("/my")
    @Operation(summary = "我的借阅记录（读者）")
    public ApiResponse<List<BorrowRecord>> myRecords(HttpSession session) {
        LoginUser user = authService.requireReader(session);
        return ApiResponse.ok(borrowService.myRecords(user.getId()));
    }

    @GetMapping
    @Operation(summary = "全部借阅记录（管理员）")
    public ApiResponse<List<BorrowRecord>> allRecords(
            @RequestParam(required = false) String keyword,
            HttpSession session) {
        authService.requireAdmin(session);
        return ApiResponse.ok(borrowService.allRecords(keyword));
    }
}
