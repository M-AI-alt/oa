package cn.exrick.xboot.modules.your.serviceimpl;

import cn.exrick.xboot.modules.your.mapper.PatternMapper;
import cn.exrick.xboot.modules.your.entity.Pattern;
import cn.exrick.xboot.modules.your.service.IPatternService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 薪资模式接口实现
 * @author 宁飞
 */
@Slf4j
@Service
@Transactional
public class IPatternServiceImpl extends ServiceImpl<PatternMapper, Pattern> implements IPatternService {

    @Autowired
    private PatternMapper patternMapper;
}