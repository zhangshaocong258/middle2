package com.alibaba.dubbo.performance.demo.agent.agent.serialize;

import com.alibaba.dubbo.performance.demo.agent.agent.model.AgentInvocation;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.pool.KryoFactory;
import com.esotericsoftware.kryo.pool.KryoPool;
import org.objenesis.strategy.StdInstantiatorStrategy;


public class KryoPoolFactory {

    private KryoPoolFactory() {
    }

    private static class Holder {
        private static KryoPoolFactory poolFactory = new KryoPoolFactory();
    }

    public static KryoPool getKryoPoolInstance() {
        return Holder.poolFactory.getPool();
    }

    private KryoFactory factory = new KryoFactory() {
        @Override
        public Kryo create() {
            Kryo kryo = new Kryo();
            kryo.setReferences(false);

            kryo.register(AgentInvocation.class);
            kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
            return kryo;
        }
    };

    private KryoPool pool = new KryoPool.Builder(factory).build();

    public KryoPool getPool() {
        return pool;
    }
}
