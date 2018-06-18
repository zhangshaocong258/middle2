package com.alibaba.dubbo.performance.demo.agent.agent.util;/**
 * Created by msi- on 2018/5/22.
 */

import com.alibaba.dubbo.performance.demo.agent.dubbo.model.Bytes;
import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;

/**
 * @program: dubbo-mesh
 * @description: 工具类
 * @author: XSL
 * @create: 2018-05-22 15:13
 **/

public class Common {
    public static byte[] endpoint2bytes(Endpoint endpoint) {
        String[] ip_split = endpoint.getHost().split("\\.");
        byte[] data = new byte[8];
        for(int i = 0;i<4;i++) {
            data[i] = (byte) Short.parseShort(ip_split[i]);
        }
        Bytes.int2bytes(endpoint.getPort(),data,4);
        return data;
    }

    public static Endpoint bytes2endpoint(byte[] data) {
        String ip = String.format("%d.%d.%d.%d",
                (int)data[0] & 0xff,
                (int)data[1] & 0xff,
                (int)data[2] & 0xff,
                (int)data[3] & 0xff);
        return new Endpoint(ip,Bytes.bytes2int(data,4));
    }
    public static void main(String[] args) {
        Endpoint endpoint = new Endpoint("192.168.0.1" ,9200);
        byte[] bytes = Common.endpoint2bytes(endpoint);
        Endpoint endpoint1 = Common.bytes2endpoint(bytes);
        System.out.println(endpoint1);
    }
}
