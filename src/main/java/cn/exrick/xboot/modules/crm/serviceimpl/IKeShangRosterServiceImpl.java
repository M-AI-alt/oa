package cn.exrick.xboot.modules.crm.serviceimpl;

import cn.exrick.xboot.modules.crm.mapper.KeShangRosterMapper;
import cn.exrick.xboot.modules.crm.entity.KeShangRoster;
import cn.exrick.xboot.modules.crm.service.IKeShangRosterService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 客商档案接口实现
 * @author 宁飞
 */
@Slf4j
@Service
@Transactional
public class IKeShangRosterServiceImpl extends ServiceImpl<KeShangRosterMapper, KeShangRoster> implements IKeShangRosterService {

    @Autowired
    private KeShangRosterMapper keShangRosterMapper;
}