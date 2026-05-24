package edu.rutmiit.demo.demorest.event;

import edu.rutmiit.demo.carsapicontract.dto.CarResponse;
import edu.rutmiit.demo.events.CarEvent;
import edu.rutmiit.demo.events.EventEnvelope;
import edu.rutmiit.demo.events.RoutingKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Публикация доменных событий машин в RabbitMQ.
 *
 * Используется после успешного завершения бизнес-операции в CarService.
 * Паттерн fire-and-forget: если RabbitMQ временно недоступен, основная
 * операция не откатывается, а ошибка только логируется.
 */
@Component
public class CarEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(CarEventPublisher.class);
    private static final String SOURCE = "demo-rest";

    private final RabbitTemplate rabbitTemplate;

    public CarEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Публикует событие «машина создана».
     */
    public void publishCreated(CarResponse car) {
        var event = new CarEvent.Created(
                car.getId(),
                car.getBrand(),
                car.getModel(),
                car.getYear(),
                car.getPrice(),
                car.getQuantity(),
                car.getDealerId()
        );
        send(RoutingKeys.CAR_CREATED, event);
    }

    /**
     * Публикует событие «машина обновлена».
     */
    public void publishUpdated(CarResponse car) {
        var event = new CarEvent.Updated(
                car.getId(),
                car.getBrand(),
                car.getModel(),
                car.getYear(),
                car.getPrice(),
                car.getQuantity(),
                car.getDealerId()
        );
        send(RoutingKeys.CAR_UPDATED, event);
    }

    /**
     * Публикует событие «машина удалена».
     */
    public void publishDeleted(Long carId, String brand, String model) {
        var event = new CarEvent.Deleted(carId, brand, model);
        send(RoutingKeys.CAR_DELETED, event);
    }

    /**
     * Публикует событие «изменилась цена машины».
     */
    public void publishPriceChanged(Long carId, Integer oldPrice, Integer newPrice) {
        var event = new CarEvent.PriceChanged(carId, oldPrice, newPrice);
        send(RoutingKeys.CAR_PRICE_CHANGED, event);
    }

    /**
     * Публикует событие «машина переведена к другому дилеру».
     */
    public void publishAssigned(Long carId, Long oldDealerId, Long newDealerId) {
        var event = new CarEvent.Assigned(carId, oldDealerId, newDealerId);
        send(RoutingKeys.CAR_ASSIGNED, event);
    }

    /**
     * Отправляет событие в RabbitMQ, обёрнутое в EventEnvelope.
     */
    private void send(String routingKey, CarEvent event) {
        try {
            EventEnvelope<CarEvent> envelope = EventEnvelope.wrap(event, SOURCE, routingKey);
            rabbitTemplate.convertAndSend(RoutingKeys.EXCHANGE, routingKey, envelope);
            log.info("Событие отправлено: {} [eventId={}]", routingKey, envelope.metadata().eventId());
        } catch (Exception e) {
            log.error("Не удалось отправить событие {}: {}", routingKey, e.getMessage());
        }
    }
}