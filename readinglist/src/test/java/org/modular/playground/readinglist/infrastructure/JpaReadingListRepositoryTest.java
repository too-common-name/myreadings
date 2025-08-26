package org.modular.playground.readinglist.infrastructure;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.modular.playground.catalog.core.domain.Book;
import org.modular.playground.catalog.core.usecases.repositories.BookRepository;
import org.modular.playground.catalog.utils.CatalogTestUtils;
import org.modular.playground.common.JpaRepositoryTestProfile;
import org.modular.playground.readinglist.core.usecases.repositories.ReadingListRepository;
import org.modular.playground.user.core.domain.User;
import org.modular.playground.user.core.usecases.repositories.UserRepository;
import org.modular.playground.user.utils.UserTestUtils;

import java.util.function.Supplier;

@QuarkusTest
@TestProfile(JpaRepositoryTestProfile.class)
@TestTransaction
public class JpaReadingListRepositoryTest extends AbstractReadingListRepositoryTest {

    @Inject
    ReadingListRepository jpaReadingListRepository;
    
    @Inject
    UserRepository userRepository;

    @Inject
    BookRepository bookRepository;

    @Override
    protected ReadingListRepository getRepository() {
        return jpaReadingListRepository;
    }

    @Override
    protected User createAndSaveUser() {
        return userRepository.create(UserTestUtils.createValidUser());
    }

    @Override
    protected Book createAndSaveBook() {
       return bookRepository.create(CatalogTestUtils.createValidBook());
    }

    @Override
    protected void runTransactionalStep(Runnable step) {
        QuarkusTransaction.requiringNew().run(step::run);
    }
    
    @Override
    protected <T> T runTransactionalStep(Supplier<T> step) {
        return QuarkusTransaction.requiringNew().call(step::get);
    }
}