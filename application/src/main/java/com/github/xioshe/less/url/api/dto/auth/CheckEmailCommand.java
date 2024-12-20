package com.github.xioshe.less.url.api.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CheckEmailCommand {
    @Schema(description = "邮箱地址")
    @NotNull
    @Email
    private String email;
}
