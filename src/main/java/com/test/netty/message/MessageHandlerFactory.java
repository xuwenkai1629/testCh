package com.test.netty.message;


import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

/**
 * 创建和管理消息处理器
 *
 * @author xu wen kai
 * @date 2023/08/26 15:58
 */
@Component
public class MessageHandlerFactory implements BeanPostProcessor {
    private static final Map<MessageType, MessageHandler<Message, MessageHandlerResponse>> HANDLER_MAP = new EnumMap<>(MessageType.class);

    private static void registerHandler(MessageType messageType, MessageHandler<Message, MessageHandlerResponse> handler) {
        HANDLER_MAP.put(messageType, handler);
    }

    public static MessageHandler<Message, MessageHandlerResponse> getHandler(MessageType messageType) {
        return HANDLER_MAP.get(messageType);
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // 判断Bean是否被自定义注解标示
        if (bean instanceof MessageHandler && bean.getClass().isAnnotationPresent(MessageHandlerType.class)) {
            MessageType messageType = bean.getClass().getAnnotation(MessageHandlerType.class).value();
            registerHandler(messageType, (MessageHandler<Message,MessageHandlerResponse>) bean);
        }
        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }
}
