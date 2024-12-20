package com.github.xioshe.less.url.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.xioshe.less.url.entity.auth.Permission;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {
    List<Permission> listEnabledPermissionsByRoleId(Long roleId);
}