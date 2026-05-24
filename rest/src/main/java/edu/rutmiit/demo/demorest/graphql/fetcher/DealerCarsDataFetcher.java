package edu.rutmiit.demo.demorest.graphql.fetcher;

import com.netflix.graphql.dgs.*;
import edu.rutmiit.demo.carsapicontract.dto.*;
import edu.rutmiit.demo.demorest.graphql.types.*;
import edu.rutmiit.demo.demorest.service.CarService;

@DgsComponent
public class DealerCarsDataFetcher {

    private final CarService carService;

    public DealerCarsDataFetcher(CarService carService) {
        this.carService = carService;
    }

    @DgsData(parentType = "Dealer", field = "cars")
    public CarConnectionGql cars(
            DgsDataFetchingEnvironment dfe,
            @InputArgument Integer page,
            @InputArgument Integer size) {

        DealerResponse dealer = dfe.getSource();

        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 20;

        PagedResponse<CarResponse> paged =
                carService.findAll(
                        dealer.getId(),
                        null, null, null, null, null,
                        pageNum, pageSize
                );

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
}