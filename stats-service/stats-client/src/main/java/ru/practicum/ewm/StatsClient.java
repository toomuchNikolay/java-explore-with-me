package ru.practicum.ewm;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatsClient {
    private final RestTemplate rest;

    public StatsClient(@Value("${ewm-server.url}") String serverUrl, RestTemplateBuilder builder) {
        rest = builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                .build();
    }

    public void create(EndpointHit endpointHit) {
        rest.postForObject("/hit", endpointHit, Void.class);
    }

    public List<ViewStats> get(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("start", start.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        parameters.put("end", end.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        parameters.put("unique", unique);
        if (uris != null) {
            parameters.put("uris", uris);
        }

        ViewStats[] response = rest.getForObject(
                "/stats?start={start}&end={end}&uris={uris}&unique={unique}",
                ViewStats[].class,
                parameters
        );

        return response != null ? List.of(response) : Collections.emptyList();
    }
}
