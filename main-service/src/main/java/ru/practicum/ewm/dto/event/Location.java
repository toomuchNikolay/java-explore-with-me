package ru.practicum.ewm.dto.event;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Location {
    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    private Double lat;

    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    private Double lon;
}
