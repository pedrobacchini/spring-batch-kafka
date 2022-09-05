package com.github.pedrobacchini.batchkafka.consumer;

import com.github.pedrobacchini.batchkafka.Customer;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.kafka.KafkaItemReader;
import org.springframework.batch.item.kafka.builder.KafkaItemReaderBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.Properties;

@EnableBatchProcessing
@SpringBootApplication
@RequiredArgsConstructor
public class ConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConsumerApplication.class, args);
    }

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final KafkaProperties properties;

    @Bean
    Job consumerJob() {
        return jobBuilderFactory.get("consumerJob")
            .start(consumerStep())
            .incrementer(new RunIdIncrementer())
            .build();
    }

    @Bean
    Step consumerStep() {
        final var itemWriter = new ItemWriter<Customer>() {
            @Override
            public void write(final List<? extends Customer> items) {
                items.forEach(System.out::println);
            }
        };

        return stepBuilderFactory.get("consumerStep")
            .<Customer, Customer>chunk(10)
            .reader(kafkaItemReader())
            .writer(itemWriter)
            .build();
    }

    private KafkaItemReader<Long, Customer> kafkaItemReader() {
        var props = new Properties();
        props.putAll(this.properties.buildConsumerProperties());
        return new KafkaItemReaderBuilder<Long, Customer>()
            .name("kafkaItemReader")
            .partitions(0)
            .consumerProperties(props)
            .saveState(true)
            .topic("customers")
            .build();
    }

}
