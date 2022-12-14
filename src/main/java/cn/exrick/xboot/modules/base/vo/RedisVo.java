package cn.exrick.xboot.modules.base.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author 宁飞
 */
@Data
@AllArgsConstructor
public class RedisVo {

    private String key;

    private String value;

    private Long expireTime;
}
