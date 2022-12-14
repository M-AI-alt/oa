package cn.exrick.xboot.modules.base.serviceimpl.mybatis;

import cn.exrick.xboot.modules.base.dao.mapper.UserRoleMapper;
import cn.exrick.xboot.modules.base.entity.Role;
import cn.exrick.xboot.modules.base.entity.UserRole;
import cn.exrick.xboot.modules.base.service.mybatis.IUserRoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author 宁飞
 */
@Service
public class IUserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRole> implements IUserRoleService {

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Override
    public List<Role> findByUserId(String userId) {

        return userRoleMapper.findByUserId(userId);
    }

    @Override
    public List<String> findDepIdsByUserId(String userId) {

        return userRoleMapper.findDepIdsByUserId(userId);
    }

    @Override
    public List<Role> findByUserIdZwz(String userId) {
        return userRoleMapper.findByUserIdZwz(userId);
    }
}
