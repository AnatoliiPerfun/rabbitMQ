package com.example.rabbitmq.service;


import com.example.rabbitmq.config.RabbitConfig;
import com.rabbitmq.client.Delivery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.rabbitmq.Receiver;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitRunner implements CommandLineRunner {

    private final QuoteGeneratorService quoteGeneratorService;
    private final QuoteMessageSender quoteMessageSender;
    private final Receiver receiver;

    @Override
    public void run(String... args) {
        CountDownLatch latch = new CountDownLatch(25);

        quoteGeneratorService.fetchQuoteStream(Duration.ofMillis(100))
                .take(25)
                .log("got rabbit")
                .flatMap(quoteMessageSender::sendQuoteMessage)
                .subscribe(result -> { log.debug("sent message to rabbit");
                latch.countDown(); },
                        throwable -> log.error("error", throwable),
                        () -> log.debug("boohoo"));

        AtomicInteger recievedCount = new AtomicInteger();

        receiver.consumeAutoAck(RabbitConfig.QUEUE)
                .log("msg r")
                .subscribe(msg -> {
                   log.debug("msg # {} - {}", recievedCount.incrementAndGet(), new String(msg.getBody()));
                }, throwable -> {
                    log.debug("error", throwable);
                }, () ->
                        log.debug("done"));
    }
}
