package cn.exrick.xboot.modules.base.service.mybatis;

import cn.exrick.xboot.modules.base.entity.Permission;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author 宁飞
 */
public interface IPermissionService extends IService<Permission> {

    /**
     * 通过用户id获取
     * @param userId
     * @return
     */
    List<Permission> findByUserId(String userId);

    List<String> findRoleName(String userId, String permissionId);
}
