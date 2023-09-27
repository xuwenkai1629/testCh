package com.test.netty.message;


import com.qif.biz.commons.constant.ApplicationProtocolConstant;
import com.qif.biz.commons.constant.Const;
import com.qif.biz.commons.constant.LOGGER;
import com.qif.biz.commons.utils.DateUtil;
import com.qif.biz.data.cache.CacheService;
import com.qif.netty.server.tcp.engine.decoder.ProtocolMessage;
import com.qif.netty.server.tcp.receive.XmlContent;
import com.qif.netty.server.tcp.send.XmlMessage;
import com.qif.netty.server.utils.XmlUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;


/**
 * 消息处理器抽象类
 *
 * @author xu wen kai
 * @date 2023/08/26 15:45
 */
public abstract class MessageHandler<T extends Message, R extends MessageHandlerResponse> extends SimpleChannelInboundHandler<T> {
    @Autowired
    public CacheService cacheService;

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, T t) {
        if (t instanceof ProtocolMessage protocolMessage) {
            String xmlStr = protocolMessage.getXmlStr();
            XmlContent<?> xmlContent = XmlUtil.parseXml(xmlStr);
            if (xmlContent == null) {
                //这里需要返回错误码
                return;
            }
            int command = xmlContent.getCommand() == null ? Const.Number.ZERO : xmlContent.getCommand();
            int type = xmlContent.getType();
            Integer sessionNo = protocolMessage.getSessionNo();
            MessageType messageType = getMessageType(type, command, sessionNo);
            t.setType(messageType);
            t.setChannelHandlerContext(channelHandlerContext);
            MessageHandlerResponse objectMessageHandlerResponse = handleMessage(t);
            replyMessage(channelHandlerContext, xmlContent.getSendCode(), xmlContent.getReceiveCode(), protocolMessage.getSend(), protocolMessage.getReceive(), objectMessageHandlerResponse);
        }
    }

    protected abstract MessageHandlerResponse handleMessage(T message);

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }

    /**
     * Calls {@link ChannelHandlerContext#fireChannelActive()} to forward
     * to the next {@link ChannelInboundHandler} in the {@link ChannelPipeline}.
     * <p>
     * Sub-classes may override this method to change behavior.
     *
     * @param ctx
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.TCP.info("channelActive:{}", ctx.channel().remoteAddress());
        super.channelActive(ctx);
    }

    /**
     * Calls {@link ChannelHandlerContext#fireChannelReadComplete()} to forward
     * to the next {@link ChannelInboundHandler} in the {@link ChannelPipeline}.
     * <p>
     * Sub-classes may override this method to change behavior.
     *
     * @param ctx
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        System.out.println("=================================================");
        super.channelReadComplete(ctx);
    }

    private MessageType getMessageType(int type, int command, int sessionFlag) {
        //注册请求
        if (ApplicationProtocolConstant.MESSAGE_TYPE_SYSTEM_MESSAGE == type
                && ApplicationProtocolConstant.COMMAND_REGISTERED == command) {
            return MessageType.REGISTRATION;
        }
        //主动上报 监视类消息
        if (type != ApplicationProtocolConstant.MESSAGE_TYPE_SYSTEM_MESSAGE) {
            return MessageType.MONITORING;
        }
        //主动上报 心跳
        if (command == ApplicationProtocolConstant.COMMAND_HEART_BEAT) {
            return MessageType.HEARTBEAT;
        }
        //响应类消息
        if (sessionFlag == 1) {
            return MessageType.RESPONSE;
        }
        return null;
    }

    //回复消息
    private void replyMessage(ChannelHandlerContext channelHandlerContext, String sendCode, String receiveCode, long sendSessionNum, long receiveSerialNum, MessageHandlerResponse messageHandlerResponse) {
        XmlContent<Object> xmlContent = new XmlContent<>();
        xmlContent.setSendCode(receiveCode);
        xmlContent.setReceiveCode(sendCode);
        xmlContent.setType(ApplicationProtocolConstant.MESSAGE_TYPE_SYSTEM_MESSAGE);
        xmlContent.setCode(String.valueOf(messageHandlerResponse.getCode()));
        int sessionFlag = messageHandlerResponse.getSessionFlag() == null ? 0 : messageHandlerResponse.getSessionFlag();
        if (messageHandlerResponse.getObject() != null) {
            xmlContent.setItems(Collections.singletonList(messageHandlerResponse.getObject()));
            xmlContent.setCommand(ApplicationProtocolConstant.COMMAND_HAVE_ITEM);
            sessionFlag = 1;
        } else {
            xmlContent.setCommand(ApplicationProtocolConstant.COMMAND_UN_ITEM);
        }
        xmlContent.setTime(DateUtil.getNowTimeStr());
        XmlMessage xmlMessage = XmlMessage.buildXmlMessage(receiveSerialNum, sendSessionNum, sessionFlag, XmlUtil.javaBeanToXmlStr(xmlContent));
        channelHandlerContext.writeAndFlush(xmlMessage);
    }

}
