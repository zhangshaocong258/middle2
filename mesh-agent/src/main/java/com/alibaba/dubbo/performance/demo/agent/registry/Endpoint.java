package com.alibaba.dubbo.performance.demo.agent.registry;

public class Endpoint {
    private final String host;
    private final int port;
    private int weight;
    public Endpoint(String host,int port){
        this.host = host;
        this.port = port;
        this.weight = Integer.valueOf(host.substring(host.lastIndexOf(".") + 1));
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getWeight() {
        return weight;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String toString(){
        return host + ":" + port;
    }

    public boolean equals(Object o){
        if (!(o instanceof Endpoint)){
            return false;
        }
        Endpoint other = (Endpoint) o;
        return other.host.equals(this.host) && other.port == this.port;
    }

    public int hashCode(){
        return host.hashCode() + port;
    }
}
