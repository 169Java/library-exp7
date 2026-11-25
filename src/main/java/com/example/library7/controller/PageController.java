package com.example.library7.controller;

import com.example.library7.common.LoginUser;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login(HttpSession session) {
        LoginUser user = (LoginUser) session.getAttribute("loginUser");
        if (user != null) {
            return user.isAdmin() ? "redirect:/admin" : "redirect:/";
        }
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/reader/borrows")
    public String myBorrows(HttpSession session) {
        LoginUser user = (LoginUser) session.getAttribute("loginUser");
        if (user == null || !"READER".equals(user.getRole())) {
            return "redirect:/login";
        }
        return "reader/borrows";
    }

    @GetMapping("/admin")
    public String admin(HttpSession session) {
        LoginUser user = (LoginUser) session.getAttribute("loginUser");
        if (user == null || !user.isAdmin()) {
            return "redirect:/login";
        }
        return "admin/index";
    }
}
