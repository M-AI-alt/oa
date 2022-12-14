package cn.exrick.xboot.modules.crm.serviceimpl;

import cn.exrick.xboot.modules.crm.mapper.ShiChangRentMapper;
import cn.exrick.xboot.modules.crm.entity.ShiChangRent;
import cn.exrick.xboot.modules.crm.service.IShiChangRentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 市场店面租金档案接口实现
 * @author 宁飞
 */
@Slf4j
@Service
@Transactional
public class IShiChangRentServiceImpl extends ServiceImpl<ShiChangRentMapper, ShiChangRent> implements IShiChangRentService {

    @Autowired
    private ShiChangRentMapper shiChangRentMapper;
}