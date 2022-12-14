package cn.exrick.xboot.modules.crm.serviceimpl;

import cn.exrick.xboot.modules.crm.mapper.DianPuShangHaoMapper;
import cn.exrick.xboot.modules.crm.entity.DianPuShangHao;
import cn.exrick.xboot.modules.crm.service.IDianPuShangHaoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 商号名称接口实现
 * @author 宁飞
 */
@Slf4j
@Service
@Transactional
public class IDianPuShangHaoServiceImpl extends ServiceImpl<DianPuShangHaoMapper, DianPuShangHao> implements IDianPuShangHaoService {

    @Autowired
    private DianPuShangHaoMapper dianPuShangHaoMapper;
}