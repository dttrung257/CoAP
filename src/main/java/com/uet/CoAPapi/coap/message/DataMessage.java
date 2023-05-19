package com.uet.CoAPapi.coap.message;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DataMessage {
    private long id;
    private String name;
    private double humidity;
    private long timestamp;
    private long latency;
    private double throughput;
}
