package com.trading212.judge.repository.task;

import com.trading212.judge.model.dto.task.TaskPageable;
import com.trading212.judge.model.entity.task.TaskEntity;

import java.util.Optional;
import java.util.Set;

public interface TaskRepository {

    TaskPageable findAllByDocumentPageable(Integer docId, Integer pageNumber);

    Optional<TaskEntity> findById(Integer id);

    boolean isExist(String name);

    boolean isExist(Integer id);

    Integer save(String name, String answersURL, Integer docId);

    boolean deleteAllByDocument(Integer id);

    Set<Integer> findAllByDocumentId(Integer id);

    Set<String> findAllURLByDocumentId(Integer id);
}
