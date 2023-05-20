package com.uet.CoAPapi.dtos;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Performance {
    private double usageCpu;
    private double usageRam;
    private double throughput;
    private String timestamp;
}
