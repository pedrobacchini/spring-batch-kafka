package com.github.pedrobacchini.batchkafka.producer;

import com.github.pedrobacchini.batchkafka.Customer;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.kafka.KafkaItemWriter;
import org.springframework.batch.item.kafka.builder.KafkaItemWriterBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.atomic.AtomicLong;

@EnableBatchProcessing
@SpringBootApplication
@RequiredArgsConstructor
public class ProducerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProducerApplication.class, args);
    }

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final KafkaTemplate<Long, Customer> kafkaTemplate;

    @Bean
    Job producerJob() {
        return jobBuilderFactory.get("producerJob")
            .start(producerStep())
            .incrementer(new RunIdIncrementer())
            .build();
    }

    @Bean
    Step producerStep() {

        var id = new AtomicLong();
        final var customerItemReader = new ItemReader<Customer>() {
            @Override
            public Customer read() {
                if (id.incrementAndGet() < 10_1000)
                    return new Customer(id.incrementAndGet(), Math.random() > .5 ? "Jane" : "Jose");

                return null;
            }
        };

        return stepBuilderFactory.get("producerStep")
            .<Customer, Customer>chunk(10)
            .reader(customerItemReader)
            .writer(kafkaItemWriter())
            .build();
    }

    @Bean
    KafkaItemWriter<Long, Customer> kafkaItemWriter() {
        return new KafkaItemWriterBuilder<Long, Customer>()
            .kafkaTemplate(kafkaTemplate)
            .itemKeyMapper(Customer::getId)
            .build();
    }

}
