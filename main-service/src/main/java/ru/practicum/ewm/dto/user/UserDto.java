package ru.practicum.ewm.dto.user;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class UserDto extends UserShortDto {
    private String email;
}
