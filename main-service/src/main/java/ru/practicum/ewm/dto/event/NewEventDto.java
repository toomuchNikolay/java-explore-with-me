package ru.practicum.ewm.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import ru.practicum.ewm.validation.annotation.ValidEventDate;

import java.time.LocalDateTime;

import static ru.practicum.ewm.util.DateTimeFormats.STANDARD;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class NewEventDto {
    @NotBlank
    @Size(min = 3, max = 120)
    private String title;

    @NotBlank
    @Size(min = 20, max = 2000)
    private String annotation;

    @NotNull
    @Positive
    private Long category;

    private Boolean paid;

    @NotNull
    @ValidEventDate(hours = 2)
    @JsonFormat(pattern = STANDARD)
    private LocalDateTime eventDate;

    @NotBlank
    @Size(min = 20, max = 7000)
    private String description;

    @PositiveOrZero
    private Integer participantLimit;

    @NotNull
    @Valid
    private Location location;

    private Boolean requestModeration;
}
