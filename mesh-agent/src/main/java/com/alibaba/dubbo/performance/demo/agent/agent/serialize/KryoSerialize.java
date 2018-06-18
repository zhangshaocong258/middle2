package com.alibaba.dubbo.performance.demo.agent.agent.serialize;/**
 * Created by msi- on 2018/5/18.
 */

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.pool.KryoPool;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @program: dubbo-mesh
 * @description: 使用kryo框架序列化/反序列化消息
 * @author: XSL
 * @create: 2018-05-18 15:40
 **/

public class KryoSerialize implements MessageSerialize {
    private KryoPool pool = null;

    public KryoSerialize(final KryoPool pool) {
        this.pool = pool;
    }

    @Override
    public void serialize(OutputStream outputStream, Object object) throws IOException {
        Kryo kryo = pool.borrow();
        Output output = new Output(outputStream);
        kryo.writeClassAndObject(output, object);
        output.close();
        pool.release(kryo);
    }

    @Override
    public Object deserialize(InputStream inputStream) throws IOException {
        Kryo kryo = pool.borrow();
        Input input = new Input(inputStream);
        Object result = kryo.readClassAndObject(input);
        input.close();
        pool.release(kryo);
        return result;
    }
}
