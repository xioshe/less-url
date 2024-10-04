package com.github.xioshe.less.url.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Url {
    private Long id;

    /**
    * 短链接
    */
    @Schema(description = "短链接")
    private String shortUrl;

    /**
    * 原始链接
    */
    @Schema(description = "原始链接")
    private String originalUrl;

    /**
    * 用户ID
    */
    @Schema(description = "用户ID")
    private Long userId;

    /**
    * 状态
    */
    @Schema(description = "状态")
    private Integer status;

    /**
     * 过期时间
     */
    @Schema(description = "过期时间")
    private LocalDateTime expirationTime;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}