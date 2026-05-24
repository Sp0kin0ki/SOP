package edu.rutmiit.demo.demorest.graphql.types;

import edu.rutmiit.demo.carsapicontract.dto.CarResponse;

import java.util.List;

public record CarConnectionGql(
        List<CarResponse> content,
        PageInfoGql pageInfo,
        long totalElements
) {}