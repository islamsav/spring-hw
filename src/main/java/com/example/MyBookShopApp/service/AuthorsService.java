package com.example.MyBookShopApp.service;

import com.example.MyBookShopApp.entity.author.Author;
import com.example.MyBookShopApp.repository.AuthorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AuthorsService {

    private final AuthorRepository authorRepository;

    @Autowired
    public AuthorsService(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    public Map<String, List<Author>> getAuthorsMap() {
        return authorRepository.findAll()
                .stream()
                .collect(Collectors.groupingBy((Author a) -> a.getName().substring(0, 1)));
    }
}
