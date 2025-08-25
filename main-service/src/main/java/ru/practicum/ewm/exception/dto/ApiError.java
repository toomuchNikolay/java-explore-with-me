package ru.practicum.ewm.exception.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.ewm.util.DateTimeFormats.STANDARD;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ApiError {
    private String status;
    private String reason;
    private String message;
    private List<String> errors;

    @JsonFormat(pattern = STANDARD)
    private LocalDateTime timestamp;
}
