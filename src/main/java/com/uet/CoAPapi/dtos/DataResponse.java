package com.uet.CoAPapi.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataResponse {
    private long id;
    private String name;
    private double humidity;
    private String timestamp;
    private long latency;
}
