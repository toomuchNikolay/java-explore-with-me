package ru.practicum.ewm.dto.reaction;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

import static ru.practicum.ewm.util.DateTimeFormats.STANDARD;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ReactionDto {
    private Long id;
    private Long user;
    private Long event;
    private Boolean isLike;

    @JsonFormat(pattern = STANDARD)
    private LocalDateTime created;
}
