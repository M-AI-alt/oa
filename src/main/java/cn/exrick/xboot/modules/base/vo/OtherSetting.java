package cn.exrick.xboot.modules.base.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author 宁飞
 */
@Data
public class OtherSetting implements Serializable{

    @ApiModelProperty(value = "域名")
    private String domain;

    @ApiModelProperty(value = "单点登录域名")
    private String ssoDomain;

    @ApiModelProperty(value = "IP黑名单")
    private String blacklist;
}
