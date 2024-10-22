package com.github.xioshe.less.url.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MigrateResponse {
    private int links;
    private long analytics;

    public MigrateResponse(int links) {
        this.links = links;
    }
}
