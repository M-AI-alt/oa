package cn.exrick.xboot.common.exception;

import lombok.Data;

/**
 * @author 宁飞
 */
@Data
public class XbootException extends RuntimeException {

    private String msg;

    public XbootException(String msg){
        super(msg);
        this.msg = msg;
    }
}
