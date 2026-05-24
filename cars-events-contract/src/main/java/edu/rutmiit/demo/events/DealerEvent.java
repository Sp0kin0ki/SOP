package edu.rutmiit.demo.events;

/**
 * Семейство событий, связанных с дилерами.
 *
 * Аналогично CarEvent — sealed interface гарантирует полный перечень вариантов.
 */
public sealed interface DealerEvent {

    /**
     * Дилер создан. Содержит основные атрибуты нового дилера.
     */
    record Created(
            Long dealerId,
            String city,
            String address,
            String phone
    ) implements DealerEvent {}

    /**
     * Дилер обновлён. Содержит актуальное состояние после изменения.
     */
    record Updated(
            Long dealerId,
            String city,
            String address,
            String phone
    ) implements DealerEvent {}

    /**
     * Дилер удалён. В событии можно указать, сколько машин было связано с ним.
     */
    record Deleted(
            Long dealerId,
            String city,
            int deletedCarsCount
    ) implements DealerEvent {}
}