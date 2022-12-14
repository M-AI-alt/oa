package cn.exrick.xboot.modules.base.dao;

import cn.exrick.xboot.base.XbootBaseDao;
import cn.exrick.xboot.modules.base.entity.Department;
import org.apache.ibatis.annotations.Delete;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * 部门数据处理层
 * @author 宁飞
 */
public interface DepartmentDao extends XbootBaseDao<Department,String> {

    /**
     * 通过父id获取 升序
     * @param parentId
     * @return
     */
    List<Department> findByParentIdOrderBySortOrder(String parentId);

    /**
     * 通过父id获取 升序 数据权限
     * @param parentId
     * @param departmentIds
     * @return
     */
    List<Department> findByParentIdAndIdInOrderBySortOrder(String parentId, List<String> departmentIds);

    /**
     * 通过父id和状态获取 升序
     * @param parentId
     * @param status
     * @return
     */
    List<Department> findByParentIdAndStatusOrderBySortOrder(String parentId, Integer status);

    /**
     * 部门名模糊搜索 升序
     * @param title
     * @return
     */
    List<Department> findByTitleLikeOrderBySortOrder(String title);

    /**
     * 部门名模糊搜索 升序 数据权限
     * @param title
     * @param departmentIds
     * @return
     */
    List<Department> findByTitleLikeAndIdInOrderBySortOrder(String title, List<String> departmentIds);

    /**
     * 根据ID找部门
     * @param id
     * @return
     */
    @Query("select d from Department d where d.id =?1")
    Department findByDepartmentZwzId(String id);

    /**
     * 根据部门档案ID找部门
     * @param id
     * @return
     */
    @Query("select d from Department d where d.archiveId =?1")
    Department findByArchiveId(String id);

    @Modifying
    @Query("delete from UserDepartment ud where ud.userId = ?1 and ud.departmentId = '394196146'")
    void deleteNotFlag(String userId);

    @Query("select d from Department d where d.parentId = ?1")
    List<Department> findByParentId(String parentId);

    @Query("select d from Department d where d.id = ?1")
    Department findByIdZwz(String id);
}