package edu.rutmiit.demo.demorest.graphql.types;

public record UpdateDealerInputGql(
        String city,
        String address,
        String phone
) {}