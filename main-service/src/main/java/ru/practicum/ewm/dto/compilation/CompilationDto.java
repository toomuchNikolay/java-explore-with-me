package ru.practicum.ewm.dto.compilation;

import lombok.*;
import ru.practicum.ewm.dto.event.EventShortDto;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
//@Builder(toBuilder = true)
public class CompilationDto {
    private Long id;
    private String title;
    private Boolean pinned;
    private List<EventShortDto> events;
}
