package com.test.netty.message;


import lombok.Builder;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 响应
 *
 * @author xu wen kai
 * @date 2023/08/30 17:01
 */
@Getter
@Builder
public class MessageHandlerResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 4898991618522718395L;

    private Integer code;

    private Integer sessionFlag;

    private transient Object object;

}
