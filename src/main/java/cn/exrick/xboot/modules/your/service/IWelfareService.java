package cn.exrick.xboot.modules.your.service;

import com.baomidou.mybatisplus.extension.service.IService;
import cn.exrick.xboot.modules.your.entity.Welfare;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 岗级福利接口
 * @author 宁飞
 */
public interface IWelfareService extends IService<Welfare> {
    Welfare findByIdZwz(String id);

    Welfare findByRosterIdZwz(String rosterId);
}