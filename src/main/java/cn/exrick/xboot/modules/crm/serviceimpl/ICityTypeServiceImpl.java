package cn.exrick.xboot.modules.crm.serviceimpl;

import cn.exrick.xboot.modules.crm.mapper.CityTypeMapper;
import cn.exrick.xboot.modules.crm.entity.CityType;
import cn.exrick.xboot.modules.crm.service.ICityTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 城市类型接口实现
 * @author 宁飞
 */
@Slf4j
@Service
@Transactional
public class ICityTypeServiceImpl extends ServiceImpl<CityTypeMapper, CityType> implements ICityTypeService {

    @Autowired
    private CityTypeMapper cityTypeMapper;
}