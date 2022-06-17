package com.example.rabbitmq.service;


import com.example.rabbitmq.model.Quote;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SynchronousSink;
import reactor.util.function.Tuple2;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;


@Service
public class QuoteGenServiceImp implements QuoteGeneratorService{


    private final MathContext mathContext = new MathContext(2);
    private final Random random = new Random();
    private final List<Quote> quotes = new ArrayList<>();

    public QuoteGenServiceImp() {
        this.quotes.add(new Quote("AAPL", 150.29));
        this.quotes.add(new Quote("TSLA", 300.99));
        this.quotes.add(new Quote("NFLX", 250.19));
        this.quotes.add(new Quote("DIS", 178.45));
        this.quotes.add(new Quote("PLTR", 233.37));
        this.quotes.add(new Quote("MFST", 260.11));
        this.quotes.add(new Quote("ARKK", 41.23));
    }

    @Override
    public Flux<Quote> fetchQuoteStream(Duration period) {

        return Flux.generate(() -> 0,
                        (BiFunction<Integer, SynchronousSink<Quote>, Integer>) (index, sink) -> {
                            Quote updatedQuote = updateQuote(this.quotes.get(index));
                            sink.next(updatedQuote);
                            return  ++index % this.quotes.size();
                        })
                .zipWith(Flux.interval(period))
                .map(Tuple2::getT1)
                .map(quote -> {
                    quote.setInstant(Instant.now());
                    return quote;
                })
                .log("QuoteGeneratorService");
    }
    private Quote updateQuote(Quote quote) {
        BigDecimal priceChange = quote.getPrice()
                .multiply(BigDecimal.valueOf(0.05 * this.random.nextDouble()), this.mathContext);
        return new Quote(quote.getTicker(), quote.getPrice().add(priceChange));
    }
}
