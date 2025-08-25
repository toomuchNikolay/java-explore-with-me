package ru.practicum.ewm.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.validation.annotation.ValidEventDate;

import java.time.LocalDateTime;

@Component
public class ValidEventDateValidator implements ConstraintValidator<ValidEventDate, LocalDateTime> {
    private int hours;

    @Override
    public void initialize(ValidEventDate constraintAnnotation) {
        this.hours = constraintAnnotation.hours();
    }

    @Override
    public boolean isValid(LocalDateTime time, ConstraintValidatorContext constraintValidatorContext) {
        if (time == null) {
            return true;
        }

        LocalDateTime allowedTime = LocalDateTime.now().plusHours(hours);
        return !time.isBefore(allowedTime);
    }
}
