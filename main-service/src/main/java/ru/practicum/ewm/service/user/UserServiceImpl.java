package ru.practicum.ewm.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.user.NewUserRequest;
import ru.practicum.ewm.dto.user.UserDto;
import ru.practicum.ewm.exception.custom.ConflictException;
import ru.practicum.ewm.exception.custom.NotFoundException;
import ru.practicum.ewm.mapper.user.UserMapper;
import ru.practicum.ewm.model.user.User;
import ru.practicum.ewm.repository.user.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository repository;
    private final UserMapper mapper;

    @Override
    @Transactional
    public UserDto create(NewUserRequest request) {
        validateEmailAvailability(request.getEmail());
        User user = repository.save(mapper.toEntity(request));
        log.info("Сохранен пользователь - {}", user);
        return mapper.toDto(user);
    }

    @Override
    public List<UserDto> get(List<Long> ids, int from, int size) {
        PageRequest page = PageRequest.of(from > 0 ? from / size : 0, size);
        List<User> users = (ids == null || ids.isEmpty())
                ? repository.findAll(page).getContent()
                : repository.findAllByIdIn(ids, page).getContent();
        log.info("Возвращен список пользователей в размере - {}", users.size());
        return mapper.toDto(users);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        User user = repository.findById(id)
                        .orElseThrow(() -> new NotFoundException("Пользователь с id=" + id + " не найден"));
        repository.delete(user);
        log.info("Удален пользователь - {}", user);
    }

    private void validateEmailAvailability(String email) {
        if (repository.existsByEmail(email)) {
            log.warn("Попытка регистрации пользователя на используемый email - {}", email);
            throw new ConflictException("Пользователь с указанным email уже зарегистрирован");
        }
    }
}
