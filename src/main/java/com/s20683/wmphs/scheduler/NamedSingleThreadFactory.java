package com.s20683.wmphs.scheduler;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedSingleThreadFactory implements ThreadFactory{

    private final String baseName;
    private final AtomicInteger threadNumber = new AtomicInteger(1);

    public NamedSingleThreadFactory(String baseName) {
        this.baseName = baseName;
    }

    @Override
    public Thread newThread(Runnable r) {
        return new Thread(r, baseName + "-" + threadNumber.getAndIncrement());
    }
}
