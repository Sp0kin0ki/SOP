package edu.rutmiit.demo.demorest.graphql.fetcher;

import com.netflix.graphql.dgs.*;
import edu.rutmiit.demo.carsapicontract.dto.*;
import edu.rutmiit.demo.demorest.graphql.types.*;
import edu.rutmiit.demo.demorest.service.DealerService;

@DgsComponent
public class DealerDataFetcher {

    private final DealerService dealerService;

    public DealerDataFetcher(DealerService dealerService) {
        this.dealerService = dealerService;
    }

    /**
     * Получить дилера
     */
    @DgsQuery
    public DealerResponse dealer(@InputArgument String id) {
        return dealerService.findById(Long.parseLong(id));
    }

    /**
     * Список дилеров
     */
    @DgsQuery
    public DealerConnectionGql dealers(
            @InputArgument Integer page,
            @InputArgument Integer size) {

        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 20;

        PagedResponse<DealerResponse> paged =
                dealerService.findAll(null, pageNum, pageSize);

        return new DealerConnectionGql(
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
     * Создать дилера
     */
    @DgsMutation
    public DealerResponse createDealer(@InputArgument CreateDealerInputGql input) {

        DealerRequest request = new DealerRequest(
                input.city(),
                input.address(),
                input.phone()
        );

        return dealerService.create(request);
    }

    /**
     * Обновить дилера
     */
    @DgsMutation
    public DealerResponse updateDealer(
            @InputArgument String id,
            @InputArgument UpdateDealerInputGql input) {

        UpdateDealerRequest request = new UpdateDealerRequest(
                input.city(),
                input.address(),
                input.phone()
        );

        return dealerService.update(Long.parseLong(id), request);
    }

    /**
     * Удаление
     */
    @DgsMutation
    public boolean deleteDealer(@InputArgument String id) {
        dealerService.delete(Long.parseLong(id));
        return true;
    }
}