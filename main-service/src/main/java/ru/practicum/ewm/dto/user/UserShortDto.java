package ru.practicum.ewm.dto.user;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
//@SuperBuilder(toBuilder = true)
public class UserShortDto {
    private Long id;
    private String name;
}
