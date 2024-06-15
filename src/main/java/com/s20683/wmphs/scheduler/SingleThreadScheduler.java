package com.s20683.wmphs.scheduler;

import com.s20683.wmphs.gui2wmphs.request.SimpleResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@Service
public class SingleThreadScheduler {
    @Autowired
    private final ExecutorService executorService;

    public SingleThreadScheduler(ExecutorService executorService) {
        this.executorService = executorService;
    }
    public Future<?> submitTask(Runnable task) {
        return executorService.submit(task);
    }

    public <T> Future<T> submitTask(Callable<T> task) {
        return executorService.submit(task);
    }
    public  <T> SimpleResponse proceedRequestWithSingleResponse(Callable<T> task) throws ExecutionException, InterruptedException {
        Future<T> resultFuture = submitTask(task);
        T result = resultFuture.get();
        if ("OK".equals(result)) {
            return SimpleResponse.createSimpleOk();
        } else {
            return new SimpleResponse(false, result.toString());
        }
    }
}
