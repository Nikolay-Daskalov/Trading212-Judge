package com.trading212.judge.Server.model.entity.task;

import com.trading212.judge.Server.model.entity.base.BaseEntity;
import com.trading212.judge.Server.model.entity.task.enums.TaskCategoryEnum;

import java.time.LocalDateTime;
import java.util.Objects;

public class LanguageCategoryEntity extends BaseEntity {

    private final TaskCategoryEnum taskCategory;

    public LanguageCategoryEntity(Integer id, LocalDateTime createdAt, TaskCategoryEnum taskCategory) {
        super(id, createdAt);
        this.taskCategory = taskCategory;
    }

    public TaskCategoryEnum getTaskCategory() {
        return taskCategory;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LanguageCategoryEntity that = (LanguageCategoryEntity) o;
        return id.equals(that.id) && taskCategory == that.taskCategory;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, taskCategory);
    }
}
