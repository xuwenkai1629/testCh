package com.test.netty.message;


import com.qif.biz.commons.enums.TcpResponseCodeEnum;
import io.netty.channel.ChannelHandler;

/**
 * 消息分发处理器
 *
 * @author xu wen kai
 * @date 2023/08/26 16:00
 */
//@ChannelHandler.Sharable 用于标记一个自定义的 ChannelHandler 是否可以被多个 Channel 共享使用。当一个 ChannelHandler 使用了该注解时，
// 它可以被多个 Channel（即多个客户端连接）共享使用，而不需要创建多个实例。这样可以提高应用程序的性能和效率。
@ChannelHandler.Sharable
public class MessageDispatcherHandler extends MessageHandler<Message, MessageHandlerResponse> {
    @Override
    protected MessageHandlerResponse handleMessage(Message message) {
        MessageHandler<Message,MessageHandlerResponse> handler = MessageHandlerFactory.getHandler(message.getType());
        if (handler != null) {
            return handler.handleMessage(message);
        } else {
            return MessageHandlerResponse.builder().code(TcpResponseCodeEnum.REJECT.getCode()).build();
        }
    }
}
