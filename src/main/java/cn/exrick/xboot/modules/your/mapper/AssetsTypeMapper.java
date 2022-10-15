package cn.exrick.xboot.modules.your.mapper;

import cn.exrick.xboot.modules.your.entity.Archive;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import cn.exrick.xboot.modules.your.entity.AssetsType;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 资产种类数据处理层
 * @author 宁飞
 */
public interface AssetsTypeMapper extends BaseMapper<AssetsType> {
    AssetsType findByIdZwz(@Param("id") String id);
}