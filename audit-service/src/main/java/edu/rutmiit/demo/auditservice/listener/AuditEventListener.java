package edu.rutmiit.demo.auditservice.listener;

import edu.rutmiit.demo.auditservice.model.AuditEntry;
import edu.rutmiit.demo.auditservice.storage.AuditStorage;
import edu.rutmiit.demo.events.CarEvent;
import edu.rutmiit.demo.events.DealerEvent;
import edu.rutmiit.demo.events.EventMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;

/**
 * Единый слушатель всех доменных событий из RabbitMQ.
 *
 * Принимает «сырое» AMQP-сообщение (Message) и десериализует его вручную.
 * Это необходимо, потому что EventEnvelope<T> — generic тип, и Jackson
 * не может определить конкретный подтип T при автоматической десериализации.
 */
@Component
public class AuditEventListener {

    private static final Logger log = LoggerFactory.getLogger(AuditEventListener.class);

    private final AuditStorage auditStorage;
    private final JsonMapper jsonMapper;

    public AuditEventListener(AuditStorage auditStorage, JsonMapper jsonMapper) {
        this.auditStorage = auditStorage;
        this.jsonMapper = jsonMapper;
    }

    /**
     * Принимает все события из очереди q.audit.events.
     */
    @RabbitListener(queues = "q.audit.events", messageConverter = "")
    public void handleEvent(Message message) {
        try {
            byte[] body = message.getBody();
            JsonNode root = jsonMapper.readTree(body);

            // Извлекаем metadata из envelope
            JsonNode metaNode = root.get("metadata");
            EventMetadata metadata = jsonMapper.treeToValue(metaNode, EventMetadata.class);

            // Проверяем дедупликацию
            if (auditStorage.isDuplicate(metadata.eventId())) {
                log.warn("Дубликат события пропущен: eventId={}", metadata.eventId());
                return;
            }

            // Формируем человекочитаемое описание
            JsonNode payloadNode = root.get("payload");
            String description = buildDescription(metadata.eventType(), payloadNode);

            AuditEntry entry = auditStorage.save(new AuditEntry(
                    0,
                    metadata.eventId(),
                    metadata.eventType(),
                    metadata.source(),
                    metadata.timestamp(),
                    Instant.now(),
                    description
            ));

            log.info("[AUDIT #{}] {} | {}",
                    entry.sequenceNumber(),
                    metadata.eventType(),
                    description);

        } catch (Exception e) {
            log.error("Ошибка обработки события: {}", e.getMessage(), e);

            // сообщение уйдёт в DLQ
            throw new RuntimeException("Не удалось обработать событие", e);
        }
    }

    /**
     * Формирует человекочитаемое описание события.
     */
    private String buildDescription(String eventType, JsonNode payloadNode) throws Exception {

        return switch (eventType) {
            case "car.created" -> {
                CarEvent.Created e =
                        jsonMapper.treeToValue(payloadNode, CarEvent.Created.class);

                yield String.format(
                        "Создан автомобиль %s %s (%d), цена %.2f",
                        e.brand(),
                        e.model(),
                        e.year(),
                        e.price()
                );
            }

            case "car.updated" -> {
                CarEvent.Updated e =
                        jsonMapper.treeToValue(payloadNode, CarEvent.Updated.class);

                yield String.format(
                        "Обновлён автомобиль id=%d %s %s",
                        e.carId(),
                        e.brand(),
                        e.model()
                );
            }

            case "car.deleted" -> {
                CarEvent.Deleted e =
                        jsonMapper.treeToValue(payloadNode, CarEvent.Deleted.class);

                yield String.format(
                        "Удалён автомобиль id=%d %s %s",
                        e.carId(),
                        e.brand(),
                        e.model()
                );
            }

            case "car.price.changed" -> {
                CarEvent.PriceChanged e =
                        jsonMapper.treeToValue(payloadNode, CarEvent.PriceChanged.class);

                yield String.format(
                        "Изменение цены автомобиля id=%d: %.2f → %.2f",
                        e.carId(),
                        e.oldPrice(),
                        e.newPrice()
                );
            }

            case "car.assigned" -> {
                CarEvent.Assigned e =
                        jsonMapper.treeToValue(payloadNode, CarEvent.Assigned.class);

                yield String.format(
                        "Автомобиль id=%d переведён от дилера %d к дилеру %d",
                        e.carId(),
                        e.oldDealerId(),
                        e.newDealerId()
                );
            }

            case "dealer.created" -> {
                DealerEvent.Created e =
                        jsonMapper.treeToValue(payloadNode, DealerEvent.Created.class);

                yield String.format(
                        "Создан дилер в городе %s (%s)",
                        e.city(),
                        e.phone()
                );
            }

            case "dealer.updated" -> {
                DealerEvent.Updated e =
                        jsonMapper.treeToValue(payloadNode, DealerEvent.Updated.class);

                yield String.format(
                        "Обновлён дилер id=%d (%s)",
                        e.dealerId(),
                        e.city()
                );
            }

            case "dealer.deleted" -> {
                DealerEvent.Deleted e =
                        jsonMapper.treeToValue(payloadNode, DealerEvent.Deleted.class);

                yield String.format(
                        "Удалён дилер id=%d (%s), удалено машин: %d",
                        e.dealerId(),
                        e.city(),
                        e.deletedCarsCount()
                );
            }

            default -> "Неизвестное событие: " + eventType;
        };
    }
}