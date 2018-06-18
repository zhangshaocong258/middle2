package com.alibaba.dubbo.performance.demo.agent.agent.model;

import java.util.concurrent.*;

public class AgentFuture<T> implements Future<T> {
    private CompletableFuture<T> future = new CompletableFuture<T>();

    public AgentFuture<T> addListener(Runnable listener, Executor executor) {
        if (executor == null) {
            executor = Runnable::run;
        }
        future.whenCompleteAsync((r,v) -> listener.run(),executor);
        return this;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        return future.get();
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return future.get();
    }

    public void done(T result){
        future.complete(result);
    }
}

