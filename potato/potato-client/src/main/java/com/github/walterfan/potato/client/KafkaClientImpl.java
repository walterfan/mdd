package com.github.walterfan.potato.client;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.IOException;
import java.util.concurrent.*;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;

@Slf4j
public class KafkaClientImpl<P, M> implements KafkaClient<P, M> {

    private final KafkaProducer<P, M> producer;
    private final String topic;
    private final long producerCloseTimeMillis;
    private ExecutorService executorService;

    public KafkaClientImpl(String topic, KafkaProducer producer, long  producerCloseTimeMillis, ExecutorService executorService) {
        checkNotNull(topic);
        this.producer = producer;
        this.topic = topic;
        this.producerCloseTimeMillis = producerCloseTimeMillis;
        this.executorService = executorService;
    }

    @Override
    public void pushMessage(P partitionKey, M message) {
        //currently all implementations are asynchronous
        pushMessageAsync(partitionKey, message, e -> {
            // If an exception is thrown log an error
            log.error("Kafka pushMessage exception: ", e);
        });
    }

    @Override
    public Future pushMessageAsync(P partitionKey, M message, Consumer<Exception> exceptionHandler) {
        ProducerRecord<P, M> record = new ProducerRecord<>(topic, partitionKey, message);
        if (executorService == null) {
            /**
             * pushMessageAsync is a misleading name. The producer.send(...) will block if it needs to retrieve
             * metadata for a topic. It waits max.block.ms before throwning a TimeoutException. This happens
             * when the Kafka server cannot be reached.
             */
            return producer.send(record, (metadata, exception) -> {
                if (exception != null && exceptionHandler != null) {
                    exceptionHandler.accept(exception);
                }
            });

        } else {
            /**
             * if @executorService is provided, then producer.send(...) will not block the user thread.
             */
            final FutureWrapper futureWrapper = new FutureWrapper();
            executorService.submit(() -> {
                Future future = producer.send(record, (metadata, exception) -> {
                    if (exception != null && exceptionHandler != null) {
                        exceptionHandler.accept(exception);
                    }
                });
                futureWrapper.setFuture(future);
            });
            return futureWrapper;
        }
    }

    @Override
    public void close() throws IOException {
        producer.close(producerCloseTimeMillis, TimeUnit.MILLISECONDS);
    }

    public class FutureWrapper<V> implements Future<V> {

        private volatile Future<V> future;
        private CountDownLatch latch = new CountDownLatch(1);

        public void setFuture(Future<V> other) {
            if (future != null) throw new IllegalStateException("Cannot reset a future that is already been set!");
            this.future = Preconditions.checkNotNull(other);
            this.latch.countDown();
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            try {
                latch.await();
                assert (future != null);
                return future.cancel(mayInterruptIfRunning);
            } catch (InterruptedException e) {
                return false;
            }
        }

        @Override
        public boolean isCancelled() {
            return future != null && future.isCancelled();
        }

        @Override
        public boolean isDone() {
            return future != null && future.isDone();
        }

        @Override
        public V get() throws InterruptedException, ExecutionException {
            latch.await();
            assert (future != null);
            return future.get();
        }

        @Override
        public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            long timeoutInNanos = unit.toNanos(timeout);
            Stopwatch stopwatch = Stopwatch.createStarted();
            boolean isCountZero = latch.await(timeoutInNanos, TimeUnit.NANOSECONDS);
            long effectiveTimeout = timeoutInNanos - stopwatch.elapsed(TimeUnit.NANOSECONDS);

            if (!isCountZero || (effectiveTimeout <= 0)) {
                throw new TimeoutException("Delegated future is not set in the timeout period!");
            }
            assert (future != null);
            return future.get(effectiveTimeout, TimeUnit.NANOSECONDS);
        }
    }
}

