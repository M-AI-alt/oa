package cn.exrick.xboot.modules.crm.serviceimpl;

import cn.exrick.xboot.modules.crm.mapper.FrameworkMapper;
import cn.exrick.xboot.modules.crm.entity.Framework;
import cn.exrick.xboot.modules.crm.service.IFrameworkService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 组织架构档案接口实现
 * @author 宁飞
 */
@Slf4j
@Service
@Transactional
public class IFrameworkServiceImpl extends ServiceImpl<FrameworkMapper, Framework> implements IFrameworkService {

    @Autowired
    private FrameworkMapper frameworkMapper;
}