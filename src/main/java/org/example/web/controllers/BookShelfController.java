package org.example.web.controllers;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.example.app.services.BookService;
import org.example.web.dto.Book;
import org.example.web.dto.BookFilter;
import org.example.web.dto.BookToRemove;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.*;
import java.util.List;

@Controller
@RequestMapping(value = "/books")
public class BookShelfController {

    private final Logger logger = Logger.getLogger(BookShelfController.class);
    private final BookService bookService;

    @Autowired
    public BookShelfController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/shelf")
    public String books(Model model) {
        logger.info(this.toString());
        model.addAttribute("book", new Book());
        model.addAttribute("bookToRemove", new BookToRemove());
        model.addAttribute("bookFilter", new BookFilter());
        model.addAttribute("bookList", bookService.getAllBooks());
        return "book_shelf";
    }

    @PostMapping("/save")
    public String saveBook(@Valid Book book, BindingResult bindingResult, Model model) {
        if (bindingResult.hasFieldErrors("title") || bindingResult.hasFieldErrors("author") || bindingResult.hasFieldErrors("size")) {
            model.addAttribute("book", book);
            model.addAttribute("bookToRemove", new BookToRemove());
            model.addAttribute("bookList", bookService.getAllBooks());
            return "book_shelf";
        } else {
            bookService.saveBook(book);
            logger.info("current repository size: " + bookService.getAllBooks().size());
            return "redirect:/books/shelf";
        }
    }

    @PostMapping("/remove")
    public String removeBook(BookToRemove bookToRemove, Model model) {
        model.addAttribute("book", new Book());
        if (bookService.removeBook(bookToRemove) == 0) {
            model.addAttribute("bookToRemove", new BookToRemove());
            model.addAttribute("bookNotFound", "book not found");
            model.addAttribute("bookList", bookService.getAllBooks());
            return "book_shelf";
        }
        return "redirect:/books/shelf";
    }

    @PostMapping(value = "/filter")
    public String filterBookList(Model model, Book book) {
        List<Book> filter = bookService.filter(book);
        if (filter.isEmpty()) {
            model.addAttribute("bookFilterNotFound", "found 0 books with this filter");
            model.addAttribute("bookList", bookService.getAllBooks());
        } else {
            model.addAttribute("bookList", filter);
        }
        logger.info(String.format("returns %s books", filter.size()));
        model.addAttribute("bookFilter", new BookFilter());
        model.addAttribute("bookToRemove", new BookToRemove());
        model.addAttribute("book", new Book());
        return "book_shelf";
    }


    @PostMapping("/uploadFile")
    public String uploadFile(@RequestParam("file") MultipartFile file) throws Exception {
        String name = file.getOriginalFilename();
        byte[] bytes = file.getBytes();
        String rootPath = System.getProperty("catalina.home");
        File dir = new File(rootPath + File.separator + "external_uploads");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File serverFile = new File(dir.getAbsolutePath() + File.separator + name);
        try (BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(serverFile))) {
            stream.write(bytes);
        }

        logger.info("new file saved at: " + serverFile.getAbsolutePath());

        return "redirect:/books/shelf";
    }

    @RequestMapping(path = "/download/{file_name}", method = RequestMethod.GET)
    public ResponseEntity<Resource> download(@PathVariable("file_name") String fileName) throws IOException {
        File file = new File(fileName);

        FileUtils.copyInputStreamToFile(getClass().getClassLoader().getResourceAsStream("files" + File.separator + fileName), file);
        String filePath = file.getAbsolutePath();
        InputStreamResource resource = new InputStreamResource(new FileInputStream(filePath));

        return ResponseEntity.ok()
                .contentLength(file.length())
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
