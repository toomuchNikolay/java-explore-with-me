package ru.practicum.ewm.dto.event;

import lombok.*;
import lombok.experimental.SuperBuilder;
import ru.practicum.ewm.validation.annotation.ValidEventDate;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class UpdateEventUserRequest extends BaseUpdateEventRequest {

    @ValidEventDate(hours = 2)
    @Override
    public LocalDateTime getEventDate() {
        return super.getEventDate();
    }
}
