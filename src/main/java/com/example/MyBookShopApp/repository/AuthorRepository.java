package com.example.MyBookShopApp.repository;

import com.example.MyBookShopApp.entity.author.Author;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorRepository extends JpaRepository<Author, Long> {

}
