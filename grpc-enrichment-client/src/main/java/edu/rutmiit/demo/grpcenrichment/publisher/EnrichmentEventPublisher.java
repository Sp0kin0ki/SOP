package edu.rutmiit.demo.grpcenrichment.publisher;

import edu.rutmiit.demo.events.CarEvent;
import edu.rutmiit.demo.events.EventEnvelope;
import edu.rutmiit.demo.events.RoutingKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Публикация событий обогащения (car.enriched) в RabbitMQ.
 *
 * Аналогичен CarEventPublisher в demo-rest,
 * но публикует другой тип события.
 *
 * Паттерн fire-and-forget:
 * если RabbitMQ недоступен — ошибка логируется,
 * но enrichment уже выполнен.
 */
@Component
public class EnrichmentEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(EnrichmentEventPublisher.class);
    private static final String SOURCE = "grpc-enrichment-client";

    private final RabbitTemplate rabbitTemplate;

    public EnrichmentEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Публикует событие car.enriched с результатами metadata enrichment.
     */
    public void publishEnriched(CarEvent.Enriched enrichedEvent) {
        try {
            EventEnvelope<CarEvent> envelope =
                    EventEnvelope.wrap(
                            enrichedEvent,
                            SOURCE,
                            RoutingKeys.CAR_ENRICHED
                    );

            rabbitTemplate.convertAndSend(
                    RoutingKeys.EXCHANGE,
                    RoutingKeys.CAR_ENRICHED,
                    envelope
            );

            log.info(
                    "Событие отправлено: {} [carId={}, eventId={}]",
                    RoutingKeys.CAR_ENRICHED,
                    enrichedEvent.carId(),
                    envelope.metadata().eventId()
            );

        } catch (Exception e) {

            log.error(
                    "Не удалось отправить событие {}: {}",
                    RoutingKeys.CAR_ENRICHED,
                    e.getMessage()
            );
        }
    }
}