package com.trading212.judge.service.task;

import com.trading212.judge.api.CloudStorageAPI;
import com.trading212.judge.model.dto.task.DocumentDTO;
import com.trading212.judge.model.dto.task.DocumentPageable;
import com.trading212.judge.model.entity.task.DocumentEntity;
import com.trading212.judge.repository.task.DocumentRepository;
import com.trading212.judge.service.enums.DocumentDifficulty;
import com.trading212.judge.service.submission.SubmissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.File;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class DocumentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentService.class);

    private final DocumentRepository documentRepository;
    private final TransactionTemplate transactionTemplate;
    private final TaskService taskService;
    private final SubmissionService submissionService;
    private final CloudStorageAPI cloudStorageAPI;

    public DocumentService(DocumentRepository documentRepository, TransactionTemplate transactionTemplate, TaskService taskService, SubmissionService submissionService, CloudStorageAPI cloudStorageAPI) {
        this.documentRepository = documentRepository;
        this.transactionTemplate = transactionTemplate;
        this.taskService = taskService;
        this.submissionService = submissionService;
        this.cloudStorageAPI = cloudStorageAPI;
    }

    public Optional<DocumentDTO> findByID(Integer id) {
        Optional<DocumentEntity> documentEntity = documentRepository.findById(id);

        if (documentEntity.isEmpty()) {
            return Optional.empty();
        }

        String docURL = cloudStorageAPI.getObjectURL(documentEntity.get().getUrl());
        return mapToDTO(documentEntity, docURL);
    }

    public boolean isExist(Integer id) {
        return documentRepository.isExist(id);
    }

    public boolean isExist(String name) {
        return documentRepository.isExist(name);
    }

    public DocumentDTO create(String name, boolean isTest, DocumentDifficulty difficulty, File file) {
        String docObjectKey = cloudStorageAPI.createDocument(name, file);

        Integer docId = documentRepository.save(name, docObjectKey, difficulty, isTest);
        Optional<DocumentEntity> documentEntity = documentRepository.findById(docId);
        String docURL = cloudStorageAPI.getObjectURL(documentEntity.get().getUrl());

        return mapToDTO(documentEntity, docURL).get();
    }

    private Optional<DocumentDTO> mapToDTO(Optional<DocumentEntity> documentEntity, String docURL) {
        if (documentEntity.isEmpty()) {
            return Optional.empty();
        }

        DocumentEntity entity = documentEntity.get();

        DocumentDTO documentDTO = new DocumentDTO(
                entity.getId(),
                entity.getName(),
                docURL,
                entity.getDifficulty(),
                entity.isTest(),
                entity.getCreatedAt()
        );

        return Optional.of(documentDTO);
    }

    public DocumentPageable getAllPageable(Integer pageNumber) {
        if (pageNumber < 0) {
            return null;
        }

        return documentRepository.getAllPageable(pageNumber);
    }

    public Boolean delete(Integer id) {
        boolean exist = isExist(id);

        if (!exist) {
            return null;
        }

        Set<Integer> tasksIds = taskService.findAllByDocumentId(id);

        try {
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    submissionService.deleteAllByTasksId(tasksIds);

                    taskService.deleteAllByDocumentId(id);

                    String docUrl = documentRepository.findURLById(id);

                    CompletableFuture<Boolean> isDocURLDeleted = CompletableFuture.supplyAsync(() -> cloudStorageAPI.deleteObjects(docUrl));

                    documentRepository.delete(id);

                    try {
                        isDocURLDeleted.get();
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                }
            });

            return true;
        } catch (TransactionException e) {
            LOGGER.error("Transaction failed for deleting document and it`s tasks!");
            return false;
        }
    }
}
