package com.example.demo.sevice;

import com.example.demo.dao.BookDaoWithJdbcTemplate;
import com.example.demo.external.OpenLibraryExchangeClient;
import com.example.demo.model.Book;
import com.example.demo.external.dto.AuthorFromOpenLibDto;
import com.example.demo.model.dto.BookDto;
import com.example.demo.external.dto.BookFromOpenLibraryDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookDaoWithJdbcTemplate bookDaoWithJdbcTemplate;

    @Mock
    private OpenLibraryExchangeClient openLibraryExchangeClient;

    @InjectMocks
    private BookService bookService;

    private final BookDto dtoForTest = new BookDto("isbn", "title", "author", "sheets", "weight", new BigDecimal("0.0"));
    private final Book bookForTest = new Book("isbn", "title", "author", "sheets", "weight", new BigDecimal("0.0"));
    private final BookFromOpenLibraryDto libraryDtoForTest = new BookFromOpenLibraryDto(
            new AuthorFromOpenLibDto("key", "name"),
            "title",
            "key");

    @Test
    void create() {
       bookService.create(dtoForTest);
       verify(bookDaoWithJdbcTemplate).createBook(bookForTest);
    }

    @Test
    void readAll() {
        when(bookDaoWithJdbcTemplate.readAll()).thenReturn(List.of(bookForTest));
        List<BookDto> dtoList = bookService.readAll();
        Assertions.assertEquals(dtoList, List.of(bookForTest).stream().map(bookService::bookModelConvertToBookDTO)
                .collect(Collectors.toList()));
    }

    @Test
    void update() {
        when(bookDaoWithJdbcTemplate.update(bookForTest, 1)).thenReturn(true);
        boolean update = bookService.update(bookService.bookModelConvertToBookDTO(bookForTest), 1);
        Assertions.assertTrue(update);
    }

    @Test
    void delete() {
        when(bookDaoWithJdbcTemplate.delete(anyInt())).thenReturn(true);
        boolean delete = bookService.delete(anyInt());
        Assertions.assertTrue(delete);
    }

    @Test
    void readFromOpenLibrary() {
        when(openLibraryExchangeClient.getBookFromOpenLibraryByAuthor(anyString())).thenReturn(List.of(libraryDtoForTest));
        List<BookDto> dtoList = bookService.readFromOpenLibrary(anyString());
        Assertions.assertEquals(dtoList, List.of(libraryDtoForTest).stream()
                .map(bookService::openLibraryDtoConvertToBookDTO).collect(Collectors.toList()));
    }

    @Test
    void readBookByAuthorFromDbAndOL() {
        when(bookDaoWithJdbcTemplate.getBookByAuthor(anyString())).thenReturn(List.of(bookForTest));
        when(openLibraryExchangeClient.getBookFromOpenLibraryByAuthor(anyString())).thenReturn(List.of(libraryDtoForTest));
        List<BookDto> dtoList = bookService.readBookByAuthorFromDbAndOL(anyString());
        List<BookDto> dtoList2 = bookService.readFromOpenLibrary(anyString());
        Assertions.assertEquals(dtoList, List.of(bookForTest).stream().map(bookService::bookModelConvertToBookDTO)
                .collect(Collectors.toCollection(() -> dtoList2)));
    }

    @Test
    void bookDTOConvertToBookModel() {
        Book book = bookService.bookDTOConvertToBookModel(dtoForTest);
        assertThat(book.getIsbn(), equalTo("isbn"));
        assertThat(book.getTitle(), equalTo("title"));
        assertThat(book.getAuthor(), equalTo("author"));
        assertThat(book.getSheets(), equalTo("sheets"));
        assertThat(book.getWeight(), equalTo("weight"));
        assertThat(book.getCost(), comparesEqualTo(new BigDecimal("0.0")));
    }

    @Test
    void openLibraryDtoConvertToBookDTO() {
        String info = "info missing in openLibrary";
        BookDto bookDto = bookService.openLibraryDtoConvertToBookDTO(libraryDtoForTest);
        assertThat(bookDto.getIsbn(), equalTo(info));
        assertThat(bookDto.getTitle(), equalTo("title"));
        assertThat(bookDto.getAuthor(), equalTo("name"));
        assertThat(bookDto.getSheets(), equalTo(info));
        assertThat(bookDto.getWeight(), equalTo(info));
        assertThat(bookDto.getCost(), comparesEqualTo(new BigDecimal("0.0")));
    }

    @Test
    void bookModelConvertToBookDTO() {
        BookDto bookDto = bookService.bookModelConvertToBookDTO(bookForTest);
        assertThat(bookDto.getIsbn(), equalTo("isbn"));
        assertThat(bookDto.getTitle(), equalTo("title"));
        assertThat(bookDto.getAuthor(), equalTo("author"));
        assertThat(bookDto.getSheets(), equalTo("sheets"));
        assertThat(bookDto.getWeight(), equalTo("weight"));
        assertThat(bookDto.getCost(), comparesEqualTo(new BigDecimal("0.0")));
    }
}