package cn.exrick.xboot.modules.crm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import cn.exrick.xboot.modules.crm.entity.Shi;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 城市定义数据处理层
 * @author 宁飞
 */
public interface ShiMapper extends BaseMapper<Shi> {
    List<Shi> findShiBySheng(@Param("sheng") String sheng);
}