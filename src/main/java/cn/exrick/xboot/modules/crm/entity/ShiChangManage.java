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
@Table(name = "t_shi_chang_manage")
@TableName("t_shi_chang_manage")
@ApiModel(value = "市场占有率管理档案")
public class ShiChangManage extends XbootBaseEntity {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "编码")
    private String number;

    @ApiModelProperty(value = "管理")
    private String title;

    @ApiModelProperty(value = "是否封存")
    private String ban;

    @ApiModelProperty(value = "备注")
    private String remark;
}