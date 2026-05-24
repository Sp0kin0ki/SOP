package edu.rutmiit.demo.events;

/**
 * Константы для маршрутизации событий в RabbitMQ.
 *
 * Routing key в topic exchange работает как почтовый индекс:
 * - "car.created"     — конкретное событие
 * - "car.*"           — все события машин
 * - "#"               — все события вообще
 *
 * Вынесены в контракт, чтобы publisher и consumer использовали одни и те же строки.
 * Рассогласование routing key — частая ошибка, которую трудно отследить.
 */
public final class RoutingKeys {

    private RoutingKeys() {
        // утилитарный класс — экземпляры не создаём
    }

    // Имя общего topic exchange для доменных событий
    public static final String EXCHANGE = "cars.events";

    // Routing keys для событий машин
    public static final String CAR_CREATED = "car.created";
    public static final String CAR_UPDATED = "car.updated";
    public static final String CAR_DELETED = "car.deleted";
    public static final String CAR_PRICE_CHANGED = "car.price.changed";
    public static final String CAR_ASSIGNED = "car.assigned";
    public static final String CAR_ENRICHED = "car.enriched";

    // Routing keys для событий дилеров
    public static final String DEALER_CREATED = "dealer.created";
    public static final String DEALER_UPDATED = "dealer.updated";
    public static final String DEALER_DELETED = "dealer.deleted";

    // Паттерны для подписки (wildcard)
    public static final String ALL_CAR_EVENTS = "car.*";
    public static final String ALL_DEALER_EVENTS = "dealer.*";
    public static final String ALL_EVENTS = "#";
}