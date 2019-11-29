package com.github.walterfan.potato.client;


import java.io.IOException;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public interface KafkaClient<P, M> extends AutoCloseable {
    void pushMessage(P partitionKey, M message);

    Future pushMessageAsync(P partitionKey, M message, Consumer<Exception> exceptionHandler) ;

    void close() throws IOException ;
}

