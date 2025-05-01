package com.mazicfilip.liveevents.integration;

import com.mazicfilip.liveevents.dto.ScoreMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaScoreProducer {
    private final KafkaTemplate<String, ScoreMessage> kafkaTemplate;

    @Value("${score.topic:score-updates}")
    private String topic;

    public void send(ScoreMessage msg) {
        kafkaTemplate.send(topic, msg.eventId(), msg)
                .whenComplete((SendResult<String, ScoreMessage> result,
                               Throwable ex) -> {

                    if (ex == null) {
                        RecordMetadata meta = result.getRecordMetadata();
                        log.info("Sent to Kafka topic={} partition={} offset={}: {}",
                                meta.topic(), meta.partition(), meta.offset(), msg);
                    } else {
                        log.error("Kafka publish failed for {}: {}", msg, ex.getMessage(), ex);
                    }
                });
    }
}
