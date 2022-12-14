package cn.exrick.xboot.modules.your.controller;

import cn.exrick.xboot.common.annotation.SystemLog;
import cn.exrick.xboot.common.enums.LogType;
import cn.exrick.xboot.common.utils.PageUtil;
import cn.exrick.xboot.common.utils.ResultUtil;
import cn.exrick.xboot.common.vo.PageVo;
import cn.exrick.xboot.common.vo.Result;
import cn.exrick.xboot.modules.your.entity.Dormitory;
import cn.exrick.xboot.modules.your.entity.DormitoryLease;
import cn.exrick.xboot.modules.your.service.IDormitoryLeaseService;
import cn.exrick.xboot.modules.your.service.IDormitoryService;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author 宁飞
 */
@Slf4j
@RestController
@Api(description = "宿舍管理管理接口")
@RequestMapping("/xboot/dormitory")
@Transactional
public class DormitoryController {

    @Autowired
    private IDormitoryService iDormitoryService;

    @Autowired
    private IDormitoryLeaseService iDormitoryLeaseService;

    @RequestMapping(value = "/get/{id}", method = RequestMethod.GET)
    @ApiOperation(value = "通过id获取")
    public Result<Dormitory> get(@PathVariable String id){

        Dormitory dormitory = iDormitoryService.getById(id);
        return new ResultUtil<Dormitory>().setData(dormitory);
    }

    @RequestMapping(value = "/getAll", method = RequestMethod.GET)
    @ApiOperation(value = "获取全部数据")
    public Result<List<Dormitory>> getAll(){

        List<Dormitory> list = iDormitoryService.list();
        return new ResultUtil<List<Dormitory>>().setData(list);
    }

    @RequestMapping(value = "/getByPage", method = RequestMethod.GET)
    @ApiOperation(value = "分页获取")
    public Result<IPage<Dormitory>> getByPage(@ModelAttribute Dormitory dormitory, @ModelAttribute PageVo page){
        QueryWrapper<Dormitory> qw = new QueryWrapper<Dormitory>();
        if(StrUtil.isNotBlank(dormitory.getHouseNumber())) {
            qw.like("house_number", dormitory.getHouseNumber());
        }
        if(StrUtil.isNotBlank(dormitory.getHouseAddress())) {
            qw.like("house_address", dormitory.getHouseAddress());
        }
        if(StrUtil.isNotBlank(dormitory.getHouseType())) {
            qw.eq("house_type", dormitory.getHouseType());
        }
        IPage<Dormitory> data = iDormitoryService.page(PageUtil.initMpPage(page),qw);
        return new ResultUtil<IPage<Dormitory>>().setData(data);
    }

    @SystemLog(description = "新增/修改宿舍信息", type = LogType.INSERT)
    @RequestMapping(value = "/insertOrUpdate", method = RequestMethod.POST)
    @ApiOperation(value = "编辑或更新数据")
    public Result<Dormitory> saveOrUpdate(Dormitory dormitory){

        if(iDormitoryService.saveOrUpdate(dormitory)){
            return new ResultUtil<Dormitory>().setData(dormitory);
        }
        return new ResultUtil<Dormitory>().setErrorMsg("操作失败");
    }

    @SystemLog(description = "删除宿舍", type = LogType.DELETE)
    @RequestMapping(value = "/delByIds", method = RequestMethod.POST)
    @ApiOperation(value = "批量通过id删除")
    public Result<Object> delAllByIds(@RequestParam String[] ids){

        for(String id : ids){
            List<DormitoryLease> leases = iDormitoryLeaseService.findByHouseId(id);
            if(!leases.isEmpty()) return ResultUtil.error("请先删除宿舍租户!");
            iDormitoryService.removeById(id);
        }
        return ResultUtil.success("删除宿舍成功");
    }
}
