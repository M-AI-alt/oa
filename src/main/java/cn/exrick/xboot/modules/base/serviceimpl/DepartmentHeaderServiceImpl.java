package cn.exrick.xboot.modules.base.serviceimpl;

import cn.exrick.xboot.modules.base.dao.DepartmentHeaderDao;
import cn.exrick.xboot.modules.base.entity.Department;
import cn.exrick.xboot.modules.base.entity.DepartmentHeader;
import cn.exrick.xboot.modules.base.entity.User;
import cn.exrick.xboot.modules.base.service.DepartmentHeaderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 部门负责人接口实现
 * @author 宁飞
 */
@Slf4j
@Service
@Transactional
public class DepartmentHeaderServiceImpl implements DepartmentHeaderService {

    @Autowired
    private DepartmentHeaderDao departmentHeaderDao;

    @Override
    public DepartmentHeaderDao getRepository() {
        return departmentHeaderDao;
    }


    @Override
    public List<String> findHeaderByDepartmentId(String departmentId, Integer type) {

        List<String> list = new ArrayList<>();
        List<DepartmentHeader> headers = departmentHeaderDao.findByDepartmentIdAndType(departmentId, type);
        headers.forEach(e->{
            list.add(e.getUserId());
        });
        return list;
    }

    @Override
    public List<DepartmentHeader> findByDepartmentIdIn(List<String> departmentIds) {

        return departmentHeaderDao.findByDepartmentIdIn(departmentIds);
    }

    @Override
    public void deleteByDepartmentId(String departmentId) {

        departmentHeaderDao.deleteByDepartmentId(departmentId);
    }

    @Override
    public void deleteByUserId(String userId) {

        departmentHeaderDao.deleteByUserId(userId);
    }

    @Override
    public User findMainHeaderNameZwz(String departmentId) {
        return departmentHeaderDao.findMainHeaderNameZwz(departmentId);
    }

    @Override
    public User findViceHeaderNameZwz(String departmentId) {
        return departmentHeaderDao.findViceHeaderNameZwz(departmentId);
    }

    @Override
    public List<Department> findDepartmentHeaderByUserIdZwz(String userId) {
        return departmentHeaderDao.findDepartmentHeaderByUserIdZwz(userId);
    }
}