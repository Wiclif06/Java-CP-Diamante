package br.edu.campusgigs.integration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class ViaCepConfig {
    @Bean
    ViaCepClient viaCepClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(2000);
        requestFactory.setReadTimeout(3000);
        RestClient client = RestClient.builder()
                .baseUrl("https://viacep.com.br")
                .requestFactory(requestFactory)
                .build();
        return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(client))
                .build().createClient(ViaCepClient.class);
    }
}
