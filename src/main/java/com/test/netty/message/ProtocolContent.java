package com.test.netty.message;

import com.qif.netty.server.tcp.receive.XmlContent;
import lombok.Data;

/**
 * 发送信息
 *
 * @author Zhouzj
 * @date 2023-05-10 16:26
 */
@Data
public class ProtocolContent {

    private String stationId;

    private String taskId;

    private String recordId;
    // 1-创建 2-启用 3-禁用 4-删除 5-编辑
    private Integer flag;

    private XmlContent<?> xmlContent;

    public ProtocolContent(String stationId, XmlContent<?> xmlContent) {
        this.stationId = stationId;
        this.xmlContent = xmlContent;
    }

}
