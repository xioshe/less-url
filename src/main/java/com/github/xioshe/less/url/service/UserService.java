package com.github.xioshe.less.url.service;

import com.github.xioshe.less.url.api.dto.MigrateResponse;
import com.github.xioshe.less.url.entity.User;
import com.github.xioshe.less.url.repository.UserRepository;
import com.github.xioshe.less.url.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final LinkService linkService;

    public void update(User user) {
        if (StringUtils.hasText(user.getPassword())) {
            user.setPassword(null);
        }
        if (!userRepository.updateById(user)) {
            throw new IllegalArgumentException("更新用户信息失败");
        }
    }

    public MigrateResponse migrate(String guestId, SecurityUser user) {
        if (user.isGuest()) {
            throw new AuthorizationServiceException("游客不允许迁移数据，请注册正式用户");
        }
        return linkService.migrate(guestId, user.getUserId());
    }
}
