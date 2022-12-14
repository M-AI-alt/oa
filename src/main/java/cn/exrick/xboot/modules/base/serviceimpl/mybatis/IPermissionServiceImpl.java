package cn.exrick.xboot.modules.base.serviceimpl.mybatis;

import cn.exrick.xboot.modules.base.dao.mapper.PermissionMapper;
import cn.exrick.xboot.modules.base.entity.Permission;
import cn.exrick.xboot.modules.base.service.mybatis.IPermissionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author 宁飞
 */
@Service
public class IPermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements IPermissionService {

    @Autowired
    private PermissionMapper permissionMapper;

    @Override
    public List<Permission> findByUserId(String userId) {

        return permissionMapper.findByUserId(userId);
    }

    @Override
    public List<String> findRoleName(String userId, String permissionId) {
        return permissionMapper.findRoleName(userId,permissionId);
    }
}
