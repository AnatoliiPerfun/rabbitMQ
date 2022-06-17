package com.example.rabbitmq.service;

import com.example.rabbitmq.model.Quote;
import reactor.core.publisher.Flux;
import java.time.Duration;

public interface QuoteGeneratorService {

    Flux<Quote> fetchQuoteStream(Duration period);
}
