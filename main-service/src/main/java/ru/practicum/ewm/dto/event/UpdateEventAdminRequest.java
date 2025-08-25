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
public class UpdateEventAdminRequest extends BaseUpdateEventRequest {

    @ValidEventDate(hours = 1)
    @Override
    public LocalDateTime getEventDate() {
        return super.getEventDate();
    }
}
