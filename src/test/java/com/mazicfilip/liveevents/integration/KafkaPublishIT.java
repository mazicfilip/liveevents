package com.mazicfilip.liveevents.integration;

import com.mazicfilip.liveevents.LiveeventsApplication;
import com.mazicfilip.liveevents.dto.ScoreMessage;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = LiveeventsApplication.class)
@EmbeddedKafka(partitions = 1,
        topics = {"score-updates"},
        brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
class KafkaPublishIT {

    @Autowired
    private KafkaScoreProducer producer;

    @Autowired
    private EmbeddedKafkaBroker broker;

    @Test
    void messageIsPublishedToKafka() {
        Map<String, Object> props =
                KafkaTestUtils.consumerProps("testGroup", "false", broker);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        Consumer<String, ScoreMessage> consumer =
                new DefaultKafkaConsumerFactory<>(props,
                        new StringDeserializer(),
                        new JsonDeserializer<>(ScoreMessage.class))
                        .createConsumer();

        broker.consumeFromAnEmbeddedTopic(consumer, "score-updates");

        ScoreMessage msg = new ScoreMessage("it-99", "3:2", Instant.now());
        producer.send(msg);

        ConsumerRecord<String, ScoreMessage> record =
                KafkaTestUtils.getSingleRecord(consumer, "score-updates");
        assertThat(record.value().currentScore()).isEqualTo("3:2");
        assertThat(record.key()).isEqualTo("it-99");
    }

    @Test
    void messagePublishing_failure_isHandledGracefully() {
        KafkaTemplate<String, ScoreMessage> kafkaTemplate = mock(KafkaTemplate.class);
        KafkaScoreProducer producer = new KafkaScoreProducer(kafkaTemplate);

        ReflectionTestUtils.setField(producer, "topic", "score-updates");

        CompletableFuture<SendResult<String, ScoreMessage>> failedFuture =
                new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Kafka down"));

        ScoreMessage msg = new ScoreMessage("fail", "0:0", Instant.now());

        when(kafkaTemplate.send(eq("score-updates"), eq(msg.eventId()), eq(msg)))
                .thenReturn(failedFuture);

        assertThatCode(() -> producer.send(msg))
                .doesNotThrowAnyException();

        verify(kafkaTemplate, times(1))
                .send("score-updates", msg.eventId(), msg);
    }
}
