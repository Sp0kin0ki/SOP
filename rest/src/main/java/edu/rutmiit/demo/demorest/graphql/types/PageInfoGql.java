package edu.rutmiit.demo.demorest.graphql.types;

public record PageInfoGql(
        int page,
        int size,
        int totalPages,
        boolean last
) {}