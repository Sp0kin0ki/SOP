package edu.rutmiit.demo.events;

/**
 * Семейство событий, связанных с машинами.
 *
 * Sealed interface ограничивает набор наследников — компилятор гарантирует,
 * что CarEvent может быть ТОЛЬКО Created, Updated, Deleted, PriceChanged или Assigned.
 *
 * Десериализация выполняется не через Jackson-аннотации на типе, а через
 * field eventType в EventMetadata — consumer определяет конкретный тип по нему.
 */
public sealed interface CarEvent {

    /**
     * Машина создана. Содержит все ключевые атрибуты новой машины.
     */
    record Created(
            Long carId,
            String brand,
            String model,
            Integer year,
            Double price,
            Integer quantity,
            Long dealerId
    ) implements CarEvent {}

    /**
     * Машина обновлена. Содержит актуальное состояние после изменения.
     */
    record Updated(
            Long carId,
            String brand,
            String model,
            Integer year,
            Double price,
            Integer quantity,
            Long dealerId
    ) implements CarEvent {}

    /**
     * Машина удалена. Достаточно идентификатора и краткого описания.
     */
    record Deleted(
            Long carId,
            String brand,
            String model
    ) implements CarEvent {}

    /**
     * Изменена цена машины.
     */
    record PriceChanged(
            Long carId,
            Integer oldPrice,
            Integer newPrice
    ) implements CarEvent {}

    /**
     * Машина переведена к другому дилеру.
     */
    record Assigned(
            Long carId,
            Long oldDealerId,
            Long newDealerId
    ) implements CarEvent {}
    /**
    * Автомобиль обогащён metadata-характеристиками.
    */
    record Enriched(
            Long carId,
            String engine,
            Integer horsepower,
            String bodyType,
            String transmission,
            String drivetrain,
            java.util.Map<String, String> specifications
    ) implements CarEvent {}
}