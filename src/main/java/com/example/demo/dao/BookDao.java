package com.example.demo.dao;

import com.example.demo.model.Book;
import com.example.demo.util.ConnectionManager;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class BookDao {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionManager.class);

    private static final AtomicInteger ID_SYNC = new AtomicInteger();
    String insertSql = "insert into book (id, isbn, title, author, sheets, weight, cost) values (?, ?, ?, ?, ?, ?, ?)";
    String selectSql = "select * from book where deleted is null limit 10";
    String updateSql = "update book set isbn = ?, title = ?, author = ?, sheets = ?, weight =?, cost= ? where id = ?";
    String deleteSql = "update book set deleted = 1  where id = ?";

    public void createBook(Book book) {
        try (Connection connection = ConnectionManager.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(insertSql)) {

            final int bookId = ID_SYNC.incrementAndGet();
            preparedStatement.setInt(1, bookId);
            preparedStatement.setString(2,  book.getIsbn());
            preparedStatement.setString(3, book.getTitle());
            preparedStatement.setString(4,  book.getAuthor());
            preparedStatement.setString(5,  book.getSheets());
            preparedStatement.setString(6, book.getWeight());
            preparedStatement.setBigDecimal(7, book.getCost());
            preparedStatement.executeUpdate();
            logger.info("Book was creat");

        } catch (Exception exception) {
            logger.error("Can't create book" + exception);
        }
    }

    public List<Book> readAll() {
        List<Book> bookList = new ArrayList<>();
        try (Connection connection = ConnectionManager.getDataSource().getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(selectSql)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String isbn = resultSet.getString("isbn");
                String title = resultSet.getString("title");
                String author = resultSet.getString("author");
                String sheets = resultSet.getString("sheets");
                String weight = resultSet.getString("weight");
                BigDecimal cost = resultSet.getBigDecimal("cost");
                Book book = new Book(id, isbn, title, author, sheets, weight, cost);
                bookList.add(book);
            }
        } catch (Exception exception) {
            logger.error("Can't find books" + exception);
        }
        return bookList;
    }

    public boolean update(Book book, int id) {
        boolean flag = true;
        try (Connection connection = ConnectionManager.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(updateSql)) {
            preparedStatement.setString(1,  book.getIsbn());
            preparedStatement.setString(2, book.getTitle());
            preparedStatement.setString(3,  book.getAuthor());
            preparedStatement.setString(4,  book.getSheets());
            preparedStatement.setString(5, book.getWeight());
            preparedStatement.setBigDecimal(6, book.getCost());
            preparedStatement.setInt(7, id);
            preparedStatement.executeUpdate();
            logger.info("Book was update");
        } catch (Exception exception) {
            logger.error("Can't update book" + exception);
            flag = false;
        }
        return flag;
    }

    public boolean delete(int id) {
        boolean flag = true;
        try (Connection connection = ConnectionManager.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(deleteSql)) {
                preparedStatement.setInt(1, id);
                preparedStatement.executeUpdate();
                logger.info("Book was delete");
            }
         catch (Exception exception) {
            logger.error("Can't find book" + exception);
             flag = false;
        }
        return flag;
    }

}

