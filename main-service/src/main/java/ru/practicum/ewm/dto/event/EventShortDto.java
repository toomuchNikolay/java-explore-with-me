package ru.practicum.ewm.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ru.practicum.ewm.dto.category.CategoryDto;
import ru.practicum.ewm.dto.user.UserShortDto;

import java.time.LocalDateTime;

import static ru.practicum.ewm.util.DateTimeFormats.STANDARD;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class EventShortDto {
    private Long id;
    private String title;
    private String annotation;
    private UserShortDto initiator;
    private CategoryDto category;

    @JsonFormat(pattern = STANDARD)
    private LocalDateTime eventDate;

    private Boolean paid;
    private String description;
    private Long confirmedRequests;
    private Long views;
}
