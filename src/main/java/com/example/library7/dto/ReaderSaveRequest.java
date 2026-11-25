package com.example.library7.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ReaderSaveRequest {

    @NotBlank(message = "读者账号不能为空")
    private String username;

    /** 新增时可为空（默认 123456），修改时为空则保持原密码 */
    private String password;

    @NotBlank(message = "读者姓名不能为空")
    private String name;

    private String phone;

    private String email;

    @NotNull(message = "状态不能为空")
    private Integer status;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
