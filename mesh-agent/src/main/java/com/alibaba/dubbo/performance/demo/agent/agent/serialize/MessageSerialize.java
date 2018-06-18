package com.alibaba.dubbo.performance.demo.agent.agent.serialize;/**
 * Created by msi- on 2018/5/18.
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @program: dubbo-mesh
 * @description: 消息的序列化和反序列化接口定义
 * @author: XSL
 * @create: 2018-05-18 15:38
 **/

public interface MessageSerialize {
    public void serialize(OutputStream outputStream,Object object) throws IOException;
    public Object deserialize(InputStream inputStream) throws IOException;
}
