package com.example.learningapp;

import com.apollographql.apollo.ApolloClient;

public class Apollo {
    ApolloClient apolloClient = ApolloClient.builder()
            .serverUrl("http://192.168.1.88:4466")
            .build();

    public ApolloClient getApolloClient() {
        return apolloClient;
    }
}
