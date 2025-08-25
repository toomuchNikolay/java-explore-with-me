package ru.practicum.ewm.dto.user;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class UserShortDto {
    private Long id;
    private String name;
}
