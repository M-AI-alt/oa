package cn.exrick.xboot.common.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author 宁飞
 */
@Data
@AllArgsConstructor
public class TokenMember implements Serializable{

    private String username;

    private Integer platform;
}
