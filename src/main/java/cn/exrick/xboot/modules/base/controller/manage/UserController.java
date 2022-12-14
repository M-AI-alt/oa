package cn.exrick.xboot.modules.base.controller.manage;

import cn.exrick.xboot.common.annotation.SystemLog;
import cn.exrick.xboot.common.redis.RedisTemplateHelper;
import cn.exrick.xboot.common.utils.*;
import cn.exrick.xboot.config.security.SecurityUserDetails;
import cn.exrick.xboot.common.constant.CommonConstant;
import cn.exrick.xboot.common.enums.LogType;
import cn.exrick.xboot.common.exception.XbootException;
import cn.exrick.xboot.common.vo.PageVo;
import cn.exrick.xboot.common.vo.Result;
import cn.exrick.xboot.common.vo.SearchVo;
import cn.exrick.xboot.modules.base.dao.mapper.DeleteMapper;
import cn.exrick.xboot.modules.base.entity.*;
import cn.exrick.xboot.modules.base.entity.dingtalk.DingtalkUser;
import cn.exrick.xboot.modules.base.service.*;
import cn.exrick.xboot.modules.base.service.mybatis.IUserRoleService;
import cn.exrick.xboot.modules.base.service.mybatis.IUserService;
import cn.exrick.xboot.modules.base.utils.CityUtil;
import cn.exrick.xboot.modules.base.utils.DingtalkUtils;
import cn.exrick.xboot.modules.base.vo.RoleDTO;
import cn.exrick.xboot.modules.your.entity.PostLevel;
import cn.exrick.xboot.modules.your.entity.Roster;
import cn.exrick.xboot.modules.your.service.IPostLevelService;
import cn.exrick.xboot.modules.your.service.IRosterService;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import com.github.qcloudsms.SmsSingleSender;
import com.github.qcloudsms.SmsSingleSenderResult;
import com.github.qcloudsms.httpclient.HTTPException;
import org.json.JSONException;
import cn.exrick.xboot.modules.base.utils.RandomCode;

/**
 * @author ??????
 */
@Slf4j
@RestController
@Api(description = "????????????")
@RequestMapping("/xboot/user")
@CacheConfig(cacheNames = "user")
@Transactional
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private IUserService iUserService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private DepartmentHeaderService departmentHeaderService;

    @Autowired
    private IUserRoleService iUserRoleService;

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private DeleteMapper deleteMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedisTemplateHelper redisTemplateHelper;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private IRosterService iRosterService;

    @Autowired
    private UserDepartmentService userDepartmentService;

    @Autowired
    private IPostLevelService iPostLevelService;

    @PersistenceContext
    private EntityManager entityManager;

    @RequestMapping(value = "/QCloudSmsLogin", method = RequestMethod.POST)
    @ApiOperation(value = "???????????????")
    public Result<Object> QCloudSmsLogin(@RequestParam(value = "mobile") String mobile, HttpServletRequest request){
        User u = userService.findByMobile(mobile);
        if(u==null){
            throw new XbootException("??????????????????");
        }
        HttpSession session = request.getSession();
        // ???????????? SDK AppID
        int appid = 1400435511; // SDK AppID ???1400??????
        // ???????????? SDK AppKey
        String appkey = "a7d937297f1ce4064c078624e1e13eb2";
        // ?????????????????????????????????
        String[] phoneNumbers = {""};
        phoneNumbers[0] =  mobile;
        // ???????????? ID?????????????????????????????????
        int templateId = 740449; // NOTE: ??????????????? ID`7839`?????????????????????????????? ID ?????????????????????????????????
        // ??????

        String smsSign = "IT?????????"; // NOTE: ????????????????????????`????????????`????????????`??????ID`??????????????????"?????????"????????????????????????????????????????????????????????????
        try {
            String randomCode = RandomCode.getRandomCode();
            String[] params = {randomCode,"5"};
            SmsSingleSender ssender = new SmsSingleSender(appid, appkey);
            SmsSingleSenderResult result = ssender.sendWithParam("86", phoneNumbers[0],
                    templateId, params, smsSign, "", "");
            System.out.println("?????????????????? = " + randomCode);
            System.out.println(result);
            session.setAttribute("tel"+mobile, randomCode);
        } catch (HTTPException e) {
            // HTTP ???????????????
            e.printStackTrace();
        } catch (JSONException e) {
            // JSON ????????????
            e.printStackTrace();
        } catch (IOException e) {
            // ?????? IO ??????
            e.printStackTrace();
        }

        return ResultUtil.success("????????????id???????????????????????????");
    }
    @RequestMapping(value = "/QCloudSmsLoginIn", method = RequestMethod.POST)
    @ApiOperation(value = "?????????????????????")
    public Result<Object> QCloudSmsLoginIn(@RequestParam(value = "mobile") String mobile, @RequestParam(value = "code") String code, HttpServletRequest request){
        HttpSession session = request.getSession();
        String codeAns = (String) session.getAttribute("tel" + mobile);
        if(codeAns.equals(code)){
            User u = userService.findByMobile(mobile);
            String accessToken = securityUtil.getToken(u.getUsername(), true);
            // ??????????????????
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(new SecurityUserDetails(u), null, null);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return ResultUtil.data(accessToken);
        }
        return ResultUtil.data(false,"NO");
    }

    @RequestMapping(value = "/smsLogin", method = RequestMethod.POST)
    @SystemLog(description = "????????????", type = LogType.LOGIN)
    @ApiOperation(value = "??????????????????")
    public Result<Object> smsLogin(@RequestParam String mobile,
                                   @RequestParam(required = false) Boolean saveLogin){

        User u = userService.findByMobile(mobile);
        if(u==null){
            throw new XbootException("??????????????????");
        }
        String accessToken = securityUtil.getToken(u.getUsername(), saveLogin);
        // ??????????????????
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(new SecurityUserDetails(u), null, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return ResultUtil.data(accessToken);
    }

    // ??????????????????????????????
    @SystemLog(description = "??????????????????", type = LogType.OPERATION)
    @RequestMapping(value = "/getByCondition2", method = RequestMethod.GET)
    @ApiOperation(value = "?????????????????????????????????")
    public Result<List<User>> getByCondition2(String roleid){

        List<User> users = userService.findByRolesId(roleid);
        return new ResultUtil<List<User>>().setData(users);
    }

    @RequestMapping(value = "/getNewJobNumber", method = RequestMethod.GET)
    @ApiOperation(value = "???????????????????????????")
    public Result<String> getNewJobNumber(){
        List<User> list = userService.findAllZwz();
        if(list.size() < 2) return new ResultUtil<String>().setData("00" + list.size());
        Collections.sort(list, new Comparator<User>() {
            @Override
            public int compare(User user, User user2) {
                if(user.getUsername().equals("admin")) return 1;
                if(user2.getUsername().equals("admin")) return 0;
                return Integer.parseInt(user.getUsername()) - Integer.parseInt(user2.getUsername());
            }
        });
        // ???????????????admin
        int jobNumber = -1;
        if(list.get(list.size() - 1).getUsername().equals("admin")){
            jobNumber =Integer.parseInt(list.get(list.size() - 2).getUsername()) + 1;
        }else{
            jobNumber =Integer.parseInt(list.get(list.size() - 1).getUsername()) + 1;
        }

        String result = String.format("%03d", jobNumber);
        return new ResultUtil<String>().setData(result);
    }

    /**
     * ????????????????????????
     * @param ids
     * @return
     * @throws ParseException
     */
    // ?????? ?????? ??????try catch?????????????????????????????????
    @SystemLog(description = "?????????????????????", type = LogType.OPERATION)
    @Transactional(propagation = Propagation.SUPPORTS)
    @RequestMapping(value = "/importFromRoster", method = RequestMethod.POST)
    @ApiOperation(value = "????????????id???????????????")
    public Result<Object> importAllByIds(@RequestParam String[] ids) throws ParseException {
        Result<Object> a = ResultUtil.success("????????????id???????????????????????????");
        for(String id : ids){
             Roster roster = iRosterService.findById(id);
            if(roster != null){
                User user = new User();
                user.setUsername(roster.getJobNumber());
                user.setNickname(roster.getUserName());
                user.setEmail(roster.getEmail() == null ? roster.getJobNumber() + "@zwz.com" : roster.getEmail());
                user.setMobile(roster.getMobile());
                user.setPassword(new BCryptPasswordEncoder().encode("123456"));
                user.setType(0);
                //user.setDepartmentId("[380137548]");
                user.setStatus(-1);
                user.setRosterId(roster.getId());
                // ???????????????
                user.setPost("1294462401827180544");
                user.setPostName("???????????????");
                // ??????????????? ?????? ?????? ??????
                String idcard = roster.getIdCard();
                if(idcard != null && idcard.length()>17){
                    SimpleDateFormat format=new SimpleDateFormat("yyyy-mm-dd");
                    user.setBirth(new Date(format.parse(roster.getBirthday()).getTime()));
                    user.setSex((idcard.charAt(16) - '0') % 2 == 1 ?"???":"???");
                    user.setAddress("[\"" + idcard.substring(0,2) + "0000\",\"" + idcard.substring(0,4) + "00\",\"" + idcard.substring(0,6) + "\"]");
                }
                try{
                    String dingtalkId = DingtalkUtils.createUser(userToDingtalkUser(user),"394196146");
                    user.setId(dingtalkId);
                    userService.save(user);
                    // ???????????????
                    UserRole ur = new UserRole();
                    ur.setUserId(user.getId());
                    ur.setRoleId("1293777172439371777");
                    userRoleService.save(ur);
                    // ?????????????????????
                    UserDepartment ud = new UserDepartment();
                    ud.setDepartmentId("394196146");
                    ud.setUserId(user.getId());
                    userDepartmentService.save(ud);
                }
                catch (Exception e){
                    a = ResultUtil.error("???????????????????????????????????????????????????????????????");
                }
            }
        }
        return a;
    }



    @RequestMapping(value = "/resetByMobile", method = RequestMethod.POST)
    @ApiOperation(value = "????????????????????????")
    public Result<Object> resetByMobile(@RequestParam String mobile,
                                        @RequestParam String password,
                                        @RequestParam String passStrength){

        User u = userService.findByMobile(mobile);
        String encryptPass = new BCryptPasswordEncoder().encode(password);
        u.setPassword(encryptPass).setPassStrength(passStrength);
        userService.update(u);
        // ????????????
        redisTemplate.delete("user::"+u.getUsername());
        return ResultUtil.success("??????????????????");
    }

    @RequestMapping(value = "/regist", method = RequestMethod.POST)
    @ApiOperation(value = "????????????")
    public Result<Object> regist(@Valid User u){

        // ?????????????????????
        checkUserInfo(u.getUsername(), u.getMobile(), u.getEmail());

        String encryptPass = new BCryptPasswordEncoder().encode(u.getPassword());
        u.setPassword(encryptPass).setType(CommonConstant.USER_TYPE_NORMAL);
        User user = userService.save(u);

        // ????????????
        List<Role> roleList = roleService.findByDefaultRole(true);
        if(roleList!=null&&roleList.size()>0){
            for(Role role : roleList){
                UserRole ur = new UserRole().setUserId(user.getId()).setRoleId(role.getId());
                userRoleService.save(ur);
            }
        }
        return ResultUtil.data(user);
    }

    @RequestMapping(value = "/info", method = RequestMethod.GET)
    @ApiOperation(value = "??????????????????????????????")
    public Result<User> getUserInfo(){

        User u = securityUtil.getCurrUser();
        // ??????????????????????????? ?????????????????????????????????
        entityManager.clear();
        u.setPassword(null);
        return new ResultUtil<User>().setData(u);
    }

    @RequestMapping(value = "/changeMobile", method = RequestMethod.POST)
    @ApiOperation(value = "??????????????????")
    public Result<Object> changeMobile(@RequestParam String mobile,String code,HttpServletRequest request){
        HttpSession session = request.getSession();
        String codeAns = (String) session.getAttribute("tel" + mobile);
        if(codeAns == null){
            return ResultUtil.error("?????????????????????!");
        }
        if(codeAns.equals(code)){
            User u = securityUtil.getCurrUser();
            u.setMobile(mobile);
            userService.update(u);
            // ????????????
            redisTemplate.delete("user::"+u.getUsername());
            return ResultUtil.success("?????????????????????!");
        }
        return ResultUtil.error("???????????????!");
    }

    @RequestMapping(value = "/unlock", method = RequestMethod.POST)
    @ApiOperation(value = "??????????????????")
    public Result<Object> unLock(@RequestParam String password){

        User u = securityUtil.getCurrUser();
        if(!new BCryptPasswordEncoder().matches(password, u.getPassword())){
            return ResultUtil.error("???????????????");
        }
        return ResultUtil.data(null);
    }

    @RequestMapping(value = "/resetPass", method = RequestMethod.POST)
    @ApiOperation(value = "????????????")
    public Result<Object> resetPass(@RequestParam String[] ids){

        for(String id:ids){
            User u = userService.get(id);
            // ??????DEMO??????
            if("test".equals(u.getUsername())||"test2".equals(u.getUsername())||"admin".equals(u.getUsername())){
                throw new XbootException("??????????????????????????????????????????");
            }
            u.setPassword(new BCryptPasswordEncoder().encode("123456"));
            userService.update(u);
            redisTemplate.delete("user::"+u.getUsername());
        }
        return ResultUtil.success("????????????");
    }

    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    @ApiOperation(value = "????????????????????????",notes = "??????????????????????????? ??????username????????????")
    @CacheEvict(key = "#u.username")
    public Result<Object> editOwn(User u){

        User old = securityUtil.getCurrUser();
        u.setUsername(old.getUsername());
        u.setPassword(old.getPassword());
        User user = userService.update(u);
        if(user==null){
            return ResultUtil.error("????????????");
        }
        return ResultUtil.success("????????????");
    }

    /**
     * ??????demo??????????????????????????????
     * @param password
     * @param newPass
     * @return
     */
    @RequestMapping(value = "/modifyPass", method = RequestMethod.POST)
    @ApiOperation(value = "????????????")
    public Result<Object> modifyPass(@ApiParam("?????????") @RequestParam String password,
                                     @ApiParam("?????????") @RequestParam String newPass,
                                     @ApiParam("????????????") @RequestParam String passStrength){

        User user = securityUtil.getCurrUser();
        // ??????DEMO??????
        if("test".equals(user.getUsername())||"test2".equals(user.getUsername())){
            return ResultUtil.error("?????????????????????????????????");
        }

        if(!new BCryptPasswordEncoder().matches(password, user.getPassword())){
            return ResultUtil.error("??????????????????");
        }

        String newEncryptPass= new BCryptPasswordEncoder().encode(newPass);
        user.setPassword(newEncryptPass);
        user.setPassStrength(passStrength);
        userService.update(user);

        // ??????????????????
        redisTemplate.delete("user::"+user.getUsername());

        return ResultUtil.success("??????????????????");
    }

    @SystemLog(description = "??????????????????", type = LogType.OPERATION)
    @RequestMapping(value = "/getByCondition", method = RequestMethod.GET)
    @ApiOperation(value = "?????????????????????????????????")
    public Result<IPage<User>> getByCondition(@ModelAttribute User user,
                                              @ModelAttribute PageVo page){
        List<User> users = null;
        if(StrUtil.isNotBlank(user.getDepartmentId())) {
            users = userService.findByDepartmentAndNameZwz(user.getNickname(),user.getDepartmentId());
        }else{
            users =  userService.findByNameZwz(user.getNickname());
        }
        for (User u : users) {
            // ????????????
            List<Role> list = iUserRoleService.findByUserIdZwz(u.getId());
            u.setNewRoles(list);
            // ????????????
            List<String> titles = userDepartmentService.findDepartmentsTitleByUserIdZwz(u.getId());
            List<String> ids = userDepartmentService.findDepartmentsIdByUserIdZwz(u.getId());
            String titleTemp = "";
            for (String title : titles) {
                if(titleTemp.equals("")) titleTemp +=title;
                else titleTemp += "," + title;
            }
            u.setDepartmentIds(ids);
            u.setDepartmentTitle(titleTemp);
            // ????????? ?????????????????????????????????
            u.setPassword(null);
            u.setRoles(new ArrayList<>());
        }
        IPage<User> data = iUserService.page(PageUtil.initMpPage(page));
        List<User> ans = new ArrayList<>();
        for(int i = (page.getPageNumber()-1) * page.getPageSize() ; i < page.getPageSize() * page.getPageNumber() && i < users.size(); i ++){
            ans.add(users.get(i));
        }
        data.setRecords(ans);
//        data.setPages(page.getPageNumber());
//        data.setSize(page.getPageSize());
//        data.setTotal(users.size());
        return new ResultUtil<IPage<User>>().setData(data);
    }

    @RequestMapping(value = "/getUserByPostLevel", method = RequestMethod.GET)
    @ApiOperation(value = "????????????????????????")
    public Result<IPage<User>> getUserByPostLevel(@RequestParam String id,@ModelAttribute PageVo page){
        List<User> users = null;
        if(id.equals("-1")){
            users = iUserService.list();
        }else {
            users = userService.findByPostZwz(id);
        }
        for (User u : users) {
            // ????????????
            List<Role> list = iUserRoleService.findByUserId(u.getId());
            List<RoleDTO> roleDTOList = list.stream().map(e->{
                return new RoleDTO().setId(e.getId()).setName(e.getName()).setDescription(e.getDescription());
            }).collect(Collectors.toList());
            u.setRoles(roleDTOList);
            // ????????????
            List<String> titles = userDepartmentService.findDepartmentsTitleByUserIdZwz(u.getId());
            List<String> ids = userDepartmentService.findDepartmentsIdByUserIdZwz(u.getId());
            String titleTemp = "";
            for (String title : titles) {
                if(titleTemp.equals("")) titleTemp +=title;
                else titleTemp += "," + title;
            }
            u.setDepartmentIds(ids);
            u.setDepartmentTitle(titleTemp);
            // ????????? ?????????????????????????????????
            u.setPassword(null);
        }
        IPage<User> data = iUserService.page(PageUtil.initMpPage(page));
        data.setRecords(users);
        data.setPages(page.getPageNumber());
        data.setSize(page.getPageSize());
        data.setTotal(users.size());
        return new ResultUtil<IPage<User>>().setData(data);
    }


    @RequestMapping(value = "/getByDepartmentId/{departmentId}", method = RequestMethod.GET)
    @ApiOperation(value = "?????????????????????????????????")
    public Result<List<User>> getByCondition(@PathVariable String departmentId){

        List<User> list = userService.findByDepartmentId(departmentId);
        entityManager.clear();
        list.forEach(u -> {
            u.setPassword(null);
        });
        return new ResultUtil<List<User>>().setData(list);
    }

    @RequestMapping(value = "/searchByName/{username}", method = RequestMethod.GET)
    @ApiOperation(value = "???????????????????????????")
    public Result<List<User>> searchByName(@PathVariable String username) throws UnsupportedEncodingException {

        List<User> list = userService.findByUsernameLikeAndStatus(URLDecoder.decode(username, "utf-8"), CommonConstant.STATUS_NORMAL);
        entityManager.clear();
        list.forEach(u -> {
            u.setPassword(null);
        });
        return new ResultUtil<List<User>>().setData(list);
    }

    @RequestMapping(value = "/getAll", method = RequestMethod.GET)
    @ApiOperation(value = "????????????????????????")
    public Result<List<User>> getByCondition(){

        List<User> list = userService.getAll();
        for(User u: list){
            // ??????????????????????????? ?????????????????????????????????
            entityManager.clear();
            u.setPassword(null);
        }
        return new ResultUtil<List<User>>().setData(list);
    }

    // ??????
    @SystemLog(description = "????????????", type = LogType.INSERT)
    @RequestMapping(value = "/admin/add", method = RequestMethod.POST)
    @ApiOperation(value = "????????????")
    public Result<Object> add(@Valid User u,
                              @RequestParam(required = false) String[] departmentIds,
                              @RequestParam(required = false) String[] roleIds){

        // ?????????????????????
        checkUserInfo(u.getUsername(), u.getMobile(), u.getEmail());

        String encryptPass = new BCryptPasswordEncoder().encode(u.getPassword());
        u.setPassword(encryptPass);
        if(StrUtil.isNotBlank(u.getDepartmentId())){
            Department d = departmentService.get(u.getDepartmentId());
            if(d!=null){
                u.setDepartmentTitle(d.getTitle());
            }
        }else{
            u.setDepartmentId(null);
            u.setDepartmentTitle("");
        }
        User user = userService.save(u);
        //???????????????
        if(departmentIds != null){
            userDepartmentService.deleteByUserId(u.getId());
            for (String id : departmentIds) {
                UserDepartment ud = new UserDepartment();
                ud.setUserId(u.getId()).setDepartmentId(id);
                userDepartmentService.save(ud);
            }
        }
        if(roleIds!=null){
            // ????????????
            List<UserRole> userRoles = Arrays.asList(roleIds).stream().map(e -> {
                return new UserRole().setUserId(u.getId()).setRoleId(e);
            }).collect(Collectors.toList());
            userRoleService.saveOrUpdateAll(userRoles);
        }
        return ResultUtil.success("????????????");
    }

    @SystemLog(description = "??????????????????", type = LogType.INSERT)
    @RequestMapping(value = "/admin/edit", method = RequestMethod.POST)
    @ApiOperation(value = "?????????????????????",notes = "????????????id????????????????????? ??????username????????????")
    @CacheEvict(key = "#u.username")
    public Result<Object> edit(User u,
                               @RequestParam(required = false) String[] departmentIds,
                               @RequestParam(required = false) String roleIds){

        SimpleDateFormat format=new SimpleDateFormat("yyyy-mm-dd");
        Roster roster = iRosterService.findById(u.getRosterId());
        // ??????????????????,???????????????????????????
        if(roster != null){
            roster.setMobile(u.getMobile());
            roster.setIntroduce(u.getDescription());
            roster.setUserName(u.getNickname());
            roster.setSex(u.getSex());
            roster.setEmail(u.getEmail());
            if(u.getAddress() != null && u.getAddress().length()>8){
                roster.setNativePlace(new CityUtil().getNativePlace(Integer.parseInt(u.getAddress().substring(u.getAddress().length()-8, u.getAddress().length()-2))));
            }
            roster.setBirthday(format.format(u.getBirth()));
            iRosterService.saveOrUpdate(roster);
        }
        //??????
        userDepartmentService.deleteByUserId(u.getId());
        if(departmentIds != null && departmentIds.length > 0){
            for (String id : departmentIds) {
                UserDepartment ud = new UserDepartment();
                ud.setUserId(u.getId()).setDepartmentId(id);
                userDepartmentService.save(ud);
            }
        }else if(departmentIds != null){
            // ????????????,????????????????????????
            UserDepartment ud = new UserDepartment();
            ud.setUserId(u.getId()).setDepartmentId("394196146");
            userDepartmentService.save(ud);
        }
        User old = userService.get(u.getId());
        u.setUsername(old.getUsername());
        // ?????????????????????????????????????????????
        if(!old.getMobile().equals(u.getMobile())&&userService.findByMobile(u.getMobile())!=null){
            return ResultUtil.error("?????????????????????????????????");
        }
        if(old.getEmail()!= null && !old.getEmail().equals(u.getEmail())&&userService.findByEmail(u.getEmail())!=null){
            return ResultUtil.error("??????????????????????????????");
        }

        if(StrUtil.isNotBlank(u.getDepartmentId())){
            Department d = departmentService.get(u.getDepartmentId());
            if(d!=null){
                u.setDepartmentTitle(d.getTitle());
            }
        }else{
            u.setDepartmentId(null);
            u.setDepartmentTitle("");
        }
        // ??????????????????
        if(u.getPost() != null){
            PostLevel postLevel = iPostLevelService.findByIdZwz(u.getPost());
            u.setPostName(postLevel == null ? null : postLevel.getTitle());
        }
        u.setPassword(old.getPassword());
        userService.update(u);
        // ????????????
        List<Department> departments = userDepartmentService.findDepartmentsByUserIdZwz(u.getId());
        DingtalkUtils.updateUser(userToDingtalkUser(u),DingtalkUtils.getDepartmentIdByDepartment(departments));
        // ?????????????????????
        userRoleService.deleteByUserId(u.getId());
        if(u.getRoleIds() != null){
            String[] roleEditRoleIds = u.getRoleIds().split(",");
            for (String roleId : roleEditRoleIds) {
                UserRole userRole = new UserRole();
                userRole.setRoleId(roleId);
                userRole.setUserId(u.getId());
                userRoleService.save(userRole);
            }
        }
        // ??????????????????
        redisTemplate.delete("userRole::"+u.getId());
        redisTemplate.delete("userRole::depIds:"+u.getId());
        redisTemplate.delete("permission::userMenuList:"+u.getId());
        return ResultUtil.success("????????????");
    }

    @RequestMapping(value = "/admin/disable/{userId}", method = RequestMethod.POST)
    @ApiOperation(value = "??????????????????")
    public Result<Object> disable(@ApiParam("????????????id??????") @PathVariable String userId){

        User user = userService.get(userId);
        if(user==null){
            return ResultUtil.error("??????userId??????????????????");
        }
        // ???????????????????????????
        List<Department> headerDepartment = departmentHeaderService.findDepartmentHeaderByUserIdZwz(user.getId());
        if(!headerDepartment.isEmpty()){
            String headerErrorMsg = "??????????????????????????????????????????: ";
            for (Department department : headerDepartment) {
                headerErrorMsg += department.getTitle();
            }
            return ResultUtil.error(headerErrorMsg);
        }
        user.setStatus(CommonConstant.USER_STATUS_LOCK);
        userService.update(user);
        // ????????????????????? ??????????????????
        if(user.getRosterId() != null){
            Roster roster = iRosterService.findById(user.getRosterId());
            if(roster != null){
                roster.setStatus("-1");
                iRosterService.saveOrUpdate(roster);
            }
        }
        // ??????????????????????????????ID?????????,??????????????????????????????????????????
        // DingtalkUtils.deleteUser(userId);
        // ??????????????????
        redisTemplate.delete("user::"+user.getUsername());
        return ResultUtil.success("????????????");
    }

    @RequestMapping(value = "/admin/enable/{userId}", method = RequestMethod.POST)
    @ApiOperation(value = "??????????????????")
    public Result<Object> enable(@ApiParam("????????????id??????") @PathVariable String userId){

        User user = userService.get(userId);
        if(user==null){
            return ResultUtil.error("??????userId??????????????????");
        }
        user.setStatus(CommonConstant.USER_STATUS_NORMAL);
        userService.update(user);
        // ????????????????????? ??????????????????
        if(user.getRosterId() != null){
            Roster roster = iRosterService.findById(user.getRosterId());
            if(roster != null){
                roster.setStatus("0");
                iRosterService.saveOrUpdate(roster);
            }
        }
        // ??????????????????????????????ID?????????,??????????????????????????????????????????
        // DingtalkUtils.createUser(userToDingtalkUser(user));
        // ??????????????????
        redisTemplate.delete("user::"+user.getUsername());
        return ResultUtil.success("????????????");
    }

    @SystemLog(description = "????????????", type = LogType.DELETE)
    @RequestMapping(value = "/delByIds", method = RequestMethod.POST)
    @ApiOperation(value = "????????????ids??????")
    public Result<Object> delAllByIds(@RequestParam String[] ids){

        for(String id:ids){
            // ??????????????? ????????????
            if(id.equals("201726206536267972")) continue;
            // ???????????????????????????
            List<Department> headerDepartment = departmentHeaderService.findDepartmentHeaderByUserIdZwz(id);
            if(!headerDepartment.isEmpty()){
                String headerErrorMsg = "??????????????????????????????????????????: ";
                for (Department department : headerDepartment) {
                    headerErrorMsg += department.getTitle();
                }
                return ResultUtil.error(headerErrorMsg);
            }
            // ???????????????????????????
            User u = userService.get(id);
            String rosterId = u.getRosterId();
            if(rosterId != null){
                Roster roster = iRosterService.findById(rosterId);
                if(roster != null){
                    roster.setStatus("-1");
                    iRosterService.saveOrUpdate(roster);
                }
            }
            // ?????????????????????
            userDepartmentService.deleteByUserId(u.getId());
            // ??????????????????
            redisTemplate.delete("user::" + u.getUsername());
            redisTemplate.delete("userRole::" + u.getId());
            redisTemplate.delete("userRole::depIds:" + u.getId());
            redisTemplate.delete("permission::userMenuList:" + u.getId());
            Set<String> keys = redisTemplateHelper.keys("department::*");
            redisTemplate.delete(keys);
            DingtalkUtils.deleteUser(id);
            userService.delete(id);

            // ??????????????????
            userRoleService.deleteByUserId(id);
            // ???????????????????????????
            departmentHeaderService.deleteByUserId(id);

            // ???????????????????????????????????????
            try {
                deleteMapper.deleteActNode(u.getId());
                deleteMapper.deleteActStarter(u.getId());
                deleteMapper.deleteSocial(u.getUsername());
            }catch (Exception e){
                log.warn(e.toString());
            }
        }
        return ResultUtil.success("????????????id??????????????????");
    }

    @RequestMapping(value = "/importData", method = RequestMethod.POST)
    @ApiOperation(value = "??????????????????")
    public Result<Object> importData(@RequestBody List<User> users){

        List<Integer> errors = new ArrayList<>();
        List<String> reasons = new ArrayList<>();
        int count = 0;
        for(User u: users){
            count++;
            // ??????????????????????????????
            if(StrUtil.isBlank(u.getUsername())||StrUtil.isBlank(u.getPassword())){
                errors.add(count);
                reasons.add("????????????????????????");
                continue;
            }
            // ?????????????????????
            if(userService.findByUsername(u.getUsername())!=null){
                errors.add(count);
                reasons.add("??????????????????");
                continue;
            }
            // ????????????
            u.setPassword(new BCryptPasswordEncoder().encode(u.getPassword()));
            // ????????????id?????????
            if(StrUtil.isNotBlank(u.getDepartmentId())){
                try {
                    Department d = departmentService.get(u.getDepartmentId());
                    log.info(d.toString());
                }catch (Exception e){
                    errors.add(count);
                    reasons.add("??????id?????????");
                    continue;
                }
            }
            if(u.getStatus()==null){
                u.setStatus(CommonConstant.USER_STATUS_NORMAL);
            }
            userService.save(u);
            // ??????????????????
            if(u.getDefaultRole()!=null&&u.getDefaultRole()==1){
                List<Role> roleList = roleService.findByDefaultRole(true);
                if(roleList!=null&&roleList.size()>0){
                    for(Role role : roleList){
                        UserRole ur = new UserRole().setUserId(u.getId()).setRoleId(role.getId());
                        userRoleService.save(ur);
                    }
                }
            }
        }
        // ??????????????????
        int successCount = users.size() - errors.size();
        String successMessage = "??????????????????????????? " + successCount + " ?????????";
        String failMessage = "???????????? " + successCount + " ???????????? " + errors.size() + " ????????????<br>" +
                "??? " + errors.toString() + " ????????????????????????????????????????????????<br>" + reasons.toString();
        String message = "";
        if(errors.size()==0){
            message = successMessage;
        }else{
            message = failMessage;
        }
        return ResultUtil.success(message);
    }

    /**
     * ??????
     * @param username ????????? ????????????????????????null ??????
     * @param mobile ?????????
     * @param email ??????
     */
    public void checkUserInfo(String username, String mobile, String email){

        // ?????????
        CommonUtil.stopwords(username);

        if(StrUtil.isNotBlank(username)&&userService.findByUsername(username)!=null){
            throw new XbootException("???????????????????????????");
        }
        if(StrUtil.isNotBlank(email)&&userService.findByEmail(email)!=null){
            throw new XbootException("?????????????????????");
        }
        if(StrUtil.isNotBlank(mobile)&&userService.findByMobile(mobile)!=null){
            throw new XbootException("????????????????????????");
        }
    }

    /**
     * OA???????????????????????????
     * @param user
     * @return
     */
    private DingtalkUser userToDingtalkUser(User user){
        DingtalkUser dingtalkUser = new DingtalkUser();
        dingtalkUser.setEmail(user.getEmail());
        dingtalkUser.setJobnumber(user.getUsername());
        dingtalkUser.setMobile(user.getMobile());
        dingtalkUser.setName(user.getNickname());
        dingtalkUser.setUserid(user.getId());
        List<Long> depertmentId = new ArrayList<>();
        List<String> ids = userDepartmentService.findDepartmentsIdByUserIdZwz(user.getId());
        for (String id : ids) {
            depertmentId.add(Long.parseLong(id));
        }
        dingtalkUser.setDepartment(depertmentId);
        return dingtalkUser;
    }
}
