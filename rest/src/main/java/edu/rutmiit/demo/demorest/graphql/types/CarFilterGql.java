package edu.rutmiit.demo.demorest.graphql.types;

public record CarFilterGql(
        Long dealerId,
        String brand,
        String model,
        Integer year,
        Double minPrice,
        Double maxPrice
) {}