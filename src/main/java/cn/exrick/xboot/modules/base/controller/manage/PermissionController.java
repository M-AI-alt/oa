package cn.exrick.xboot.modules.base.controller.manage;

import cn.exrick.xboot.common.annotation.SystemLog;
import cn.exrick.xboot.common.constant.CommonConstant;
import cn.exrick.xboot.common.enums.LogType;
import cn.exrick.xboot.common.redis.RedisTemplateHelper;
import cn.exrick.xboot.common.utils.PageUtil;
import cn.exrick.xboot.common.utils.ResultUtil;
import cn.exrick.xboot.common.utils.SecurityUtil;
import cn.exrick.xboot.common.vo.PageVo;
import cn.exrick.xboot.common.vo.Result;
import cn.exrick.xboot.config.security.permission.MySecurityMetadataSource;
import cn.exrick.xboot.modules.base.entity.Permission;
import cn.exrick.xboot.modules.base.entity.Role;
import cn.exrick.xboot.modules.base.entity.RolePermission;
import cn.exrick.xboot.modules.base.entity.User;
import cn.exrick.xboot.modules.base.service.PermissionService;
import cn.exrick.xboot.modules.base.service.RolePermissionService;
import cn.exrick.xboot.modules.base.service.RoleService;
import cn.exrick.xboot.modules.base.service.UserService;
import cn.exrick.xboot.modules.base.service.mybatis.IPermissionService;
import cn.exrick.xboot.modules.base.service.mybatis.IUserService;
import cn.exrick.xboot.modules.base.utils.VoUtil;
import cn.exrick.xboot.modules.base.vo.MenuVo;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author ??????
 */
@Slf4j
@RestController
@Api(description = "??????/??????????????????")
@RequestMapping("/xboot/permission")
@CacheConfig(cacheNames = "permission")
@Transactional
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private RolePermissionService rolePermissionService;

    @Autowired
    private IPermissionService iPermissionService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedisTemplateHelper redisTemplateHelper;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private MySecurityMetadataSource mySecurityMetadataSource;

    @Autowired
    private UserService userService;

    @Autowired
    private IUserService iUserService;

    @Autowired
    private RoleService roleService;

    @RequestMapping(value = "/getUserByPermission", method = RequestMethod.GET)
    @ApiOperation(value = "?????????????????????")
    public Result<IPage<User>> getByPage(@RequestParam String pid, @ModelAttribute PageVo page){
        List<User> ans = new ArrayList<>();
        if(StrUtil.isNotBlank(pid)) {
            List<RolePermission> permissionList = rolePermissionService.findByPermissionId(pid);
            for (RolePermission rolePermission : permissionList) {
                String roleId = rolePermission.getRoleId();
                if(roleId != null){
                    List<User> users = userService.getUserByRoles(rolePermission.getRoleId());
                    for (User user : users) {
                        boolean flag = true;
                        for (User an : ans) {
                            if(an.getUsername().equals(user.getUsername())){
                                flag = false;
                                break;
                            }
                        }
                        if(flag){
                            ans.add(user);
                        }
                        List<String> roleNames = iPermissionService.findRoleName(user.getId(), pid);
                        user.setRoleName(roleNames.toString());
                    }
                }
            }
        }
        IPage<User> result = iUserService.page(PageUtil.initMpPage(page));
        result.setRecords(ans);
        return new ResultUtil<IPage<User>>().setData(result);
    }

    @RequestMapping(value = "/getMenuList", method = RequestMethod.GET)
    @ApiOperation(value = "??????????????????????????????")
    public Result<List<MenuVo>> getAllMenuList(){

        List<MenuVo> menuList = new ArrayList<>();
        // ????????????
        User u = securityUtil.getCurrUser();
        String key = "permission::userMenuList:" + u.getId();
        String v = redisTemplate.opsForValue().get(key);
        if(StrUtil.isNotBlank(v)){
            menuList = new Gson().fromJson(v, new TypeToken<List<MenuVo>>(){}.getType());
            return new ResultUtil<List<MenuVo>>().setData(menuList);
        }

        // ?????????????????? ???????????????
        List<Permission> list = iPermissionService.findByUserId(u.getId());

        // ??????0?????????
        for(Permission p : list){
            if(CommonConstant.PERMISSION_NAV.equals(p.getType())&&CommonConstant.LEVEL_ZERO.equals(p.getLevel())){
                menuList.add(VoUtil.permissionToMenuVo(p));
            }
        }
        // ??????????????????
        List<MenuVo> firstMenuList = new ArrayList<>();
        for(Permission p : list){
            if(CommonConstant.PERMISSION_PAGE.equals(p.getType())&&CommonConstant.LEVEL_ONE.equals(p.getLevel())){
                firstMenuList.add(VoUtil.permissionToMenuVo(p));
            }
        }
        // ??????????????????
        List<MenuVo> secondMenuList = new ArrayList<>();
        for(Permission p : list){
            if(CommonConstant.PERMISSION_PAGE.equals(p.getType())&&CommonConstant.LEVEL_TWO.equals(p.getLevel())){
                secondMenuList.add(VoUtil.permissionToMenuVo(p));
            }
        }
        // ???????????????????????????????????????
        List<MenuVo> buttonPermissions = new ArrayList<>();
        for(Permission p : list){
            if(CommonConstant.PERMISSION_OPERATION.equals(p.getType())&&CommonConstant.LEVEL_THREE.equals(p.getLevel())){
                buttonPermissions.add(VoUtil.permissionToMenuVo(p));
            }
        }

        // ??????????????????????????????
        for(MenuVo m : secondMenuList){
            List<String> permTypes = new ArrayList<>();
            for(MenuVo me : buttonPermissions){
                if(m.getId().equals(me.getParentId())){
                    permTypes.add(me.getButtonType());
                }
            }
            m.setPermTypes(permTypes);
        }
        // ????????????????????????????????????
        for(MenuVo m : firstMenuList){
            List<MenuVo> secondMenu = new ArrayList<>();
            for(MenuVo me : secondMenuList){
                if(m.getId().equals(me.getParentId())){
                    secondMenu.add(me);
                }
            }
            m.setChildren(secondMenu);
        }
        // ??????0???????????????????????????
        for(MenuVo m : menuList){
            List<MenuVo> firstMenu = new ArrayList<>();
            for(MenuVo me : firstMenuList){
                if(m.getId().equals(me.getParentId())){
                    firstMenu.add(me);
                }
            }
            m.setChildren(firstMenu);
        }

        // ??????
        redisTemplate.opsForValue().set(key, new Gson().toJson(menuList), 15L, TimeUnit.DAYS);
        return new ResultUtil<List<MenuVo>>().setData(menuList);
    }

    @RequestMapping(value = "/getAllList", method = RequestMethod.GET)
    @ApiOperation(value = "?????????????????????")
    @Cacheable(key = "'allList'")
    public Result<List<Permission>> getAllList(){

        // 0???
        List<Permission> list0 = permissionService.findByLevelOrderBySortOrder(CommonConstant.LEVEL_ZERO);
        for(Permission p0 : list0){
            // ??????
            List<Permission> list1 = permissionService.findByParentIdOrderBySortOrder(p0.getId());
            p0.setChildren(list1);
            // ??????
            for(Permission p1 : list1){
                List<Permission> children1 = permissionService.findByParentIdOrderBySortOrder(p1.getId());
                p1.setChildren(children1);
                // ??????
                for(Permission p2 : children1){
                    List<Permission> children2 = permissionService.findByParentIdOrderBySortOrder(p2.getId());
                    p2.setChildren(children2);
                }
            }
        }
        return new ResultUtil<List<Permission>>().setData(list0);
    }

    @SystemLog(description = "????????????", type = LogType.INSERT)
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ApiOperation(value = "??????")
    @CacheEvict(key = "'menuList'")
    public Result<Permission> add(Permission permission){

        // ?????????????????????????????????????????????????????????
        if(CommonConstant.PERMISSION_OPERATION.equals(permission.getType())){
            List<Permission> list = permissionService.findByTitle(permission.getTitle());
            if(list!=null&&list.size()>0){
                return new ResultUtil<Permission>().setErrorMsg("???????????????");
            }
        }
        Permission u = permissionService.save(permission);
        //??????????????????
        mySecurityMetadataSource.loadResourceDefine();
        //??????????????????
        redisTemplate.delete("permission::allList");
        return new ResultUtil<Permission>().setData(u);
    }

    @SystemLog(description = "????????????", type = LogType.INSERT)
    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    @ApiOperation(value = "??????")
    public Result<Permission> edit(Permission permission){

        // ?????????????????????????????????????????????????????????
        if(CommonConstant.PERMISSION_OPERATION.equals(permission.getType())){
            // ???????????????
            Permission p = permissionService.get(permission.getId());
            if(!p.getTitle().equals(permission.getTitle())){
                List<Permission> list = permissionService.findByTitle(permission.getTitle());
                if(list!=null&&list.size()>0){
                    return new ResultUtil<Permission>().setErrorMsg("???????????????");
                }
            }
        }
        Permission u = permissionService.update(permission);
        // ??????????????????
        mySecurityMetadataSource.loadResourceDefine();
        // ????????????????????????
        Set<String> keysUser = redisTemplateHelper.keys("user:" + "*");
        redisTemplate.delete(keysUser);
        Set<String> keysUserMenu = redisTemplateHelper.keys("permission::userMenuList:*");
        redisTemplate.delete(keysUserMenu);
        redisTemplate.delete("permission::allList");
        return new ResultUtil<Permission>().setData(u);
    }

    @SystemLog(description = "????????????", type = LogType.DELETE)
    @RequestMapping(value = "/delByIds", method = RequestMethod.POST)
    @ApiOperation(value = "????????????id??????")
    @CacheEvict(key = "'menuList'")
    public Result<Object> delByIds(@RequestParam String[] ids){

        for(String id:ids){
            List<RolePermission> list = rolePermissionService.findByPermissionId(id);
            if(list!=null&&list.size()>0){
                return ResultUtil.error("???????????????????????????????????????????????????????????????");
            }
        }
        for(String id:ids){
            permissionService.delete(id);
        }
        // ??????????????????
        mySecurityMetadataSource.loadResourceDefine();
        // ??????????????????
        redisTemplate.delete("permission::allList");
        return ResultUtil.success("????????????id??????????????????");
    }

    @RequestMapping(value = "/search", method = RequestMethod.GET)
    @ApiOperation(value = "????????????")
    public Result<List<Permission>> searchPermissionList(@RequestParam String title){

        List<Permission> list = permissionService.findByTitleLikeOrderBySortOrder("%"+title+"%");
        return new ResultUtil<List<Permission>>().setData(list);
    }
}
