package edu.rutmiit.demo.grpcenrichment.listener;

import edu.rutmiit.demo.events.CarEvent;
import edu.rutmiit.demo.events.EventMetadata;
import edu.rutmiit.demo.grpc.CarMetadataServiceGrpc;
import edu.rutmiit.demo.grpc.ResolveSpecificationsRequest;
import edu.rutmiit.demo.grpc.ResolveSpecificationsResponse;
import edu.rutmiit.demo.grpcenrichment.publisher.EnrichmentEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Слушатель событий car.created из RabbitMQ.
 *
 * Десериализация — ручная (как в audit-service),
 * потому что EventEnvelope<T> является generic-типом,
 * и Jackson не может определить конкретный подтип T.
 */
@Component
public class CarCreatedListener {

    private static final Logger log = LoggerFactory.getLogger(CarCreatedListener.class);

    private final CarMetadataServiceGrpc.CarMetadataServiceBlockingStub metadataStub;
    private final EnrichmentEventPublisher enrichmentPublisher;
    private final JsonMapper jsonMapper;

    public CarCreatedListener(
            CarMetadataServiceGrpc.CarMetadataServiceBlockingStub metadataStub,
            EnrichmentEventPublisher enrichmentPublisher,
            JsonMapper jsonMapper
    ) {
        this.metadataStub = metadataStub;
        this.enrichmentPublisher = enrichmentPublisher;
        this.jsonMapper = jsonMapper;
    }

    /**
     * Обрабатывает событие car.created:
     *
     * 1. Десериализует событие из JSON
     * 2. Формирует gRPC-запрос
     * 3. Вызывает gRPC metadata service (синхронно)
     * 4. Публикует результат как событие car.enriched
     */
    @RabbitListener(queues = "q.enrichment.car-created", messageConverter = "")
    public void handleCarCreated(Message message) {
        try {
            // 1. Парсим JSON envelope
            byte[] body = message.getBody();
            JsonNode root = jsonMapper.readTree(body);

            JsonNode metaNode = root.get("metadata");
            EventMetadata metadata =
                    jsonMapper.treeToValue(
                            metaNode,
                            EventMetadata.class
                    );

            JsonNode payloadNode = root.get("payload");
            CarEvent.Created carCreated =
                    jsonMapper.treeToValue(
                            payloadNode,
                            CarEvent.Created.class
                    );

            log.info(
                    "Получено событие car.created: carId={}, {} {} [eventId={}]",
                    carCreated.carId(),
                    carCreated.brand(),
                    carCreated.model(),
                    metadata.eventId()
            );

            // 2. Формируем gRPC request
            ResolveSpecificationsRequest grpcRequest =
                    ResolveSpecificationsRequest.newBuilder()
                            .setCarId(carCreated.carId())
                            .setBrand(carCreated.brand() != null ? carCreated.brand() : "")
                            .setModel(carCreated.model() != null ? carCreated.model() : "")
                            .setYear(carCreated.year() != null ? carCreated.year() : 0)
                            .build();

            // 3. Вызываем gRPC metadata service
            log.info(
                    "Вызов gRPC: CarMetadataService.ResolveSpecifications(carId={})",
                    carCreated.carId()
            );

            ResolveSpecificationsResponse grpcResponse =
                    metadataStub.resolveSpecifications(grpcRequest);

            log.info(
                    "gRPC ответ получен: carId={}, engine={}, hp={}, bodyType={}, transmission={}, drivetrain={}",
                    grpcResponse.getCarId(),
                    grpcResponse.getEngine(),
                    grpcResponse.getHorsepower(),
                    grpcResponse.getBodyType(),
                    grpcResponse.getTransmission(),
                    grpcResponse.getDrivetrain()
            );

            // 4. Публикуем событие car.enriched
            CarEvent.Enriched enrichedEvent =
                    new CarEvent.Enriched(
                            grpcResponse.getCarId(),
                            grpcResponse.getEngine(),
                            grpcResponse.getHorsepower(),
                            grpcResponse.getBodyType(),
                            grpcResponse.getTransmission(),
                            grpcResponse.getDrivetrain(),
                            grpcResponse.getSpecificationsMap()
                    );

            enrichmentPublisher.publishEnriched(enrichedEvent);
            log.info(
                    "Автомобиль обогащён: carId={}, {} {} и car.enriched отправлено",
                    carCreated.carId(),
                    carCreated.brand(),
                    carCreated.model()
            );
        } catch (io.grpc.StatusRuntimeException e) {

            log.error(
                    "gRPC ошибка при enrichment автомобиля: {} ({})",
                    e.getStatus().getDescription(),
                    e.getStatus().getCode()
            );

            throw new RuntimeException("gRPC-вызов завершился ошибкой", e);
        } catch (Exception e) {
            log.error(
                    "Ошибка обработки события car.created: {}",
                    e.getMessage(),
                    e
            );
            throw new RuntimeException("Не удалось обработать событие car.created", e);
        }
    }
}