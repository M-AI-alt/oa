package cn.exrick.xboot.config.jpa;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

/**
 * 审计记录创建或修改用户
 * @author 宁飞
 */
@Configuration
@Slf4j
public class UserAuditor implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {

        UserDetails user;
        try {
            user = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            return Optional.ofNullable(user.getUsername());
        }catch (Exception e){
            return Optional.empty();
        }
    }
}
