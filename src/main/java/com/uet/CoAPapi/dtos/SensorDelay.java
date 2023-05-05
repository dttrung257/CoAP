package com.uet.CoAPapi.dtos;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SensorDelay {
    @NotNull(message = "delay is mandatory")
    @DecimalMin(value = "1.0", message = "Min speed is 1.0 seconds")
    private double delay;
}
