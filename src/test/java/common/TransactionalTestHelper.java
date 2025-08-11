package common;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import modules.catalog.core.domain.Book;
import modules.catalog.core.usecases.repositories.BookRepository;
import modules.readinglist.core.domain.ReadingList;
import modules.readinglist.core.usecases.repositories.ReadingListRepository;
import modules.review.core.domain.Review;
import modules.review.core.usecases.repositories.ReviewRepository;
import modules.user.core.domain.User;
import modules.user.core.usecases.repositories.UserRepository;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class TransactionalTestHelper {

    @Inject
    UserRepository userRepository;

    @Inject
    BookRepository bookRepository;

    @Inject
    ReadingListRepository readingListRepository;

    @Inject
    ReviewRepository reviewRepository;

    @Transactional
    public void addBookToReadingList(UUID readingListId, UUID bookId) {
        readingListRepository.addBookToReadingList(readingListId, bookId);
    }

    @Transactional
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    @Transactional
    public Book saveBook(Book book) {
        return bookRepository.save(book);
    }

    @Transactional
    public ReadingList saveReadingList(ReadingList readingList) {
        return readingListRepository.create(readingList);
    }

    @Transactional
    public void deleteReadingList(UUID id) {
        readingListRepository.deleteById(id);
    }

    @Transactional
    public void deleteBook(UUID id) {
        bookRepository.deleteById(id);
    }

    @Transactional
    public void deleteUser(UUID id) {
        userRepository.deleteById(id);
    }

    public List<ReadingList> findReadingListsByUserId(UUID userId) {
        return readingListRepository.findByUserId(userId);
    }

    @Transactional
    public Review saveReview(Review review) {
        return reviewRepository.create(review);
    }

    @Transactional
    public void deleteReview(UUID id) {
        try {
            reviewRepository.deleteById(id);
        } catch (Exception e) {
        }
    }
}