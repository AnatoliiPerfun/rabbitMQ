package com.example.rabbitmq.service;


import com.example.rabbitmq.config.RabbitConfig;
import com.example.rabbitmq.model.Quote;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.*;



@Slf4j
@Component
@RequiredArgsConstructor
public class QuoteMessageSender {
    private final ObjectMapper objectMapper;
    private final Sender sender;

    @SneakyThrows
    public Mono<Void> sendQuoteMessage(Quote quote) {
        byte[] jsonBytes = objectMapper.writeValueAsBytes(quote);
        Flux<OutboundMessageResult> confirmations = sender
                .sendWithPublishConfirms(Flux.just(new OutboundMessage("",
                RabbitConfig.QUEUE, jsonBytes)));

        sender.declareQueue(QueueSpecification.queue(RabbitConfig.QUEUE))
                .thenMany(confirmations)
                .doOnError(e -> log.error("failed", e))
                .subscribe(r -> {
                    if (r.isAck()) {
                        log.info("sent successfully {}", new String(r.getOutboundMessage().getBody()));
                    }
                });

        return Mono.empty();
    }
}
