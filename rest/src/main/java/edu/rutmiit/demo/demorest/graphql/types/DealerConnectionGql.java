package edu.rutmiit.demo.demorest.graphql.types;

import edu.rutmiit.demo.carsapicontract.dto.DealerResponse;

import java.util.List;

public record DealerConnectionGql(
        List<DealerResponse> content,
        PageInfoGql pageInfo,
        long totalElements
) {}