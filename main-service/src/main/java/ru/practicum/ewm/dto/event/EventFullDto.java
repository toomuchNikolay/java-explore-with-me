package ru.practicum.ewm.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ru.practicum.ewm.model.event.EventState;

import java.time.LocalDateTime;

import static ru.practicum.ewm.util.DateTimeFormats.STANDARD;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class EventFullDto extends EventShortDto {
    private Location location;
    private EventState state;
    private Integer participantLimit;
    private Boolean requestModeration;

    @JsonFormat(pattern = STANDARD)
    private LocalDateTime createdOn;

    @JsonFormat(pattern = STANDARD)
    private LocalDateTime publishedOn;
}
