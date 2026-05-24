package edu.rutmiit.demo.demorest.graphql.fetcher;

import com.netflix.graphql.dgs.*;
import edu.rutmiit.demo.carsapicontract.dto.*;
import edu.rutmiit.demo.demorest.graphql.types.*;
import edu.rutmiit.demo.demorest.service.CarService;

@DgsComponent
public class CarDataFetcher {

    private final CarService carService;

    public CarDataFetcher(CarService carService) {
        this.carService = carService;
    }

    /**
     * Получение машины по ID
     */
    @DgsQuery
    public CarResponse car(@InputArgument String id) {
        return carService.findById(Long.parseLong(id));
    }

    /**
     * Список машин (page + size, без перегруза фильтрами)
     */
    @DgsQuery
    public CarConnectionGql cars(
            @InputArgument Integer page,
            @InputArgument Integer size) {

        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 20;

        PagedResponse<CarResponse> paged =
                carService.findAll(null, null, null, null, null, null, pageNum, pageSize);

        return new CarConnectionGql(
                paged.content(),
                new PageInfoGql(
                        paged.pageNumber(),
                        paged.pageSize(),
                        paged.totalPages(),
                        paged.last()
                ),
                paged.totalElements()
        );
    }

    /**
     * Создание машины
     */
    @DgsMutation
    public CarResponse createCar(@InputArgument CreateCarInputGql input) {

        CarRequest request = new CarRequest(
                input.brand(),
                input.model(),
                input.year(),
                input.price(),
                input.quantity(),
                input.dealerId(),
                input.specifications()
        );

        return carService.create(request);
    }

    /**
     * Полное обновление
     */
    @DgsMutation
    public CarResponse updateCar(
            @InputArgument String id,
            @InputArgument UpdateCarInputGql input) {

        UpdateCarRequest request = new UpdateCarRequest(
                input.brand(),
                input.model(),
                input.year(),
                input.price(),
                input.quantity(),
                input.dealerId(),
                input.specifications()
        );

        return carService.update(Long.parseLong(id), request);
    }

    /**
     * Удаление
     */
    @DgsMutation
    public boolean deleteCar(@InputArgument String id) {
        carService.delete(Long.parseLong(id));
        return true;
    }
}