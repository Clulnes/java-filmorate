package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.dao.RatingDbStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class RatingService {
    private final RatingDbStorage ratingDbStorage;
    public Collection<Rating> findAll() { return ratingDbStorage.findAll(); }
    public Rating getById(int id) { return ratingDbStorage.getById(id); }
}
