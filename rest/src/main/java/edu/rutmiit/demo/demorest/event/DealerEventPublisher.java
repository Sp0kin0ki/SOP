package edu.rutmiit.demo.demorest.event;

import edu.rutmiit.demo.carsapicontract.dto.DealerResponse;
import edu.rutmiit.demo.events.DealerEvent;
import edu.rutmiit.demo.events.EventEnvelope;
import edu.rutmiit.demo.events.RoutingKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Публикация доменных событий дилеров в RabbitMQ.
 *
 * Аналогичен CarEventPublisher — тот же fire-and-forget паттерн.
 */
@Component
public class DealerEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(DealerEventPublisher.class);
    private static final String SOURCE = "demo-rest";

    private final RabbitTemplate rabbitTemplate;

    public DealerEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Публикует событие «дилер создан».
     */
    public void publishCreated(DealerResponse dealer) {
        var event = new DealerEvent.Created(
                dealer.getId(),
                dealer.getCity(),
                dealer.getAddress(),
                dealer.getPhone()
        );
        send(RoutingKeys.DEALER_CREATED, event);
    }

    /**
     * Публикует событие «дилер обновлён».
     */
    public void publishUpdated(DealerResponse dealer) {
        var event = new DealerEvent.Updated(
                dealer.getId(),
                dealer.getCity(),
                dealer.getAddress(),
                dealer.getPhone()
        );
        send(RoutingKeys.DEALER_UPDATED, event);
    }

    /**
     * Публикует событие «дилер удалён».
     */
    public void publishDeleted(Long dealerId, String city, int deletedCarsCount) {
        var event = new DealerEvent.Deleted(dealerId, city, deletedCarsCount);
        send(RoutingKeys.DEALER_DELETED, event);
    }

    /**
     * Отправляет событие в RabbitMQ, обёрнутое в EventEnvelope.
     */
    private void send(String routingKey, DealerEvent event) {
        try {
            EventEnvelope<DealerEvent> envelope = EventEnvelope.wrap(event, SOURCE, routingKey);
            rabbitTemplate.convertAndSend(RoutingKeys.EXCHANGE, routingKey, envelope);
            log.info("Событие отправлено: {} [eventId={}]", routingKey, envelope.metadata().eventId());
        } catch (Exception e) {
            log.error("Не удалось отправить событие {}: {}", routingKey, e.getMessage());
        }
    }
}