package com.test.netty.message;


import io.netty.channel.ChannelHandlerContext;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 消息基类
 *
 * @author xu wen kai
 * @date 2023/08/26 15:44
 */
@Data
public class Message implements Serializable {
    @Serial
    private static final long serialVersionUID = 7993712144173567262L;
    /**
     * 消息类型
     */
    private MessageType type;

    private transient ChannelHandlerContext channelHandlerContext;
}
