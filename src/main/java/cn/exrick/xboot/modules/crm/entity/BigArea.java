package cn.exrick.xboot.modules.crm.entity;

import cn.exrick.xboot.base.XbootBaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author 宁飞
 */
@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "t_big_area")
@TableName("t_big_area")
@ApiModel(value = "大区定义")
public class BigArea extends XbootBaseEntity {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "编码")
    private String number;

    @ApiModelProperty(value = "大区")
    private String title;

    @ApiModelProperty(value = "分公司")
    private String sonCompany;

    @ApiModelProperty(value = "钉钉ID")
    private String dingId;

    @ApiModelProperty(value = "是否封存")
    private String ban;

    @ApiModelProperty(value = "备注")
    private String remark;
}