package com.alibaba.dubbo.performance.demo.agent.agent.serialize;/**
 * Created by msi- on 2018/5/18.
 */

import com.alibaba.dubbo.performance.demo.agent.agent.model.Invocation;
import com.alibaba.dubbo.performance.demo.agent.agent.model.MessageRequest;
import com.alibaba.dubbo.performance.demo.agent.agent.model.MessageResponse;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.pool.KryoFactory;
import com.esotericsoftware.kryo.pool.KryoPool;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.util.HashMap;

/**
 * @program: dubbo-mesh
 * @description:
 * @author: XSL
 * @create: 2018-05-18 15:07
 **/

public class KryoPoolFactory {
    private static KryoPoolFactory poolFactory = null;

    private KryoFactory factory = () ->{
            Kryo kryo = new Kryo();
            kryo.setReferences(false);
            //把已知的结构注册到Kryo注册器里面,提高序列化/反序列化效率
//            kryo.register(MessageRequest.class);
//            kryo.register(MessageResponse.class);
            kryo.register(Invocation.class);
            kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
            return kryo;
    };
    private KryoPool pool = new KryoPool.Builder(factory).build();
    private KryoPoolFactory() {

    }

    public static KryoPool getKryoPoolInstance() {
        if (poolFactory == null) {
            synchronized (KryoPoolFactory.class) {
                if (poolFactory == null) {
                    poolFactory = new KryoPoolFactory();
                }
            }
        }
        return poolFactory.getPool();
    }

    public KryoPool getPool() {
        return pool;
    }
}
