package org.modular.playground.readinglist.utils;

import java.util.List;
import java.util.UUID;

import org.modular.playground.readinglist.core.domain.ReadingList;
import org.modular.playground.readinglist.core.usecases.repositories.ReadingListRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ReadingListRepositoryUtils {
    
    @Inject
    ReadingListRepository readingListRepository;

    @Transactional
    public void addBookToReadingList(UUID readingListId, UUID bookId) {
        readingListRepository.addBookToReadingList(readingListId, bookId);
    }

    @Transactional
    public ReadingList saveReadingList(ReadingList readingList) {
        return readingListRepository.create(readingList);
    }

    @Transactional
    public void deleteReadingList(UUID id) {
        readingListRepository.deleteById(id);
    }

    public List<ReadingList> findReadingListsByUserId(UUID userId) {
        return readingListRepository.findByUserId(userId);
    }
}
