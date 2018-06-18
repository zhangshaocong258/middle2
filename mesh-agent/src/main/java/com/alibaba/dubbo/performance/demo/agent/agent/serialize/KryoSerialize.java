package com.alibaba.dubbo.performance.demo.agent.agent.serialize;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.pool.KryoPool;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class KryoSerialize {
    private KryoPool pool = null;

    public KryoSerialize(final KryoPool pool) {
        this.pool = pool;
    }

    public void serialize(OutputStream outputStream, Object object) throws IOException {
        Kryo kryo = pool.borrow();
        Output output = new Output(outputStream);
        kryo.writeClassAndObject(output, object);
        output.close();
        pool.release(kryo);
    }

    public Object deserialize(InputStream inputStream) throws IOException {
        Kryo kryo = pool.borrow();
        Input input = new Input(inputStream);
        Object result = kryo.readClassAndObject(input);
        input.close();
        pool.release(kryo);
        return result;
    }
}
