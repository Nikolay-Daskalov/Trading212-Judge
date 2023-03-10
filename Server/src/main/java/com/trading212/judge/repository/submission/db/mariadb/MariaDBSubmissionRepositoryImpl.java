package com.trading212.judge.repository.submission.db.mariadb;

import com.trading212.judge.model.entity.submission.SubmissionEntity;
import com.trading212.judge.repository.submission.SubmissionRepository;
import com.trading212.judge.service.enums.CodeResultEnum;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class MariaDBSubmissionRepositoryImpl implements SubmissionRepository {

    private final JdbcTemplate jdbcTemplate;

    public MariaDBSubmissionRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Integer save(Integer codeLanguageId, CodeResultEnum codeResult, Integer userId, Integer taskId, String executionTime) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(Queries.SAVE, Statement.RETURN_GENERATED_KEYS);

            ps.setInt(1, taskId);
            ps.setInt(2, userId);
            ps.setString(3, codeResult.name());
            ps.setInt(4, codeLanguageId);
            ps.setString(5, executionTime);

            return ps;
        }, keyHolder);

        return keyHolder.getKey().intValue();
    }

    @Override
    public Optional<SubmissionEntity> findById(Integer id) {
        SubmissionEntity submissionEntity = jdbcTemplate.query(Queries.FIND_BY_ID, rs -> {
            if (!rs.next()) {
                return null;
            }

            return new SubmissionEntity.Builder()
                    .setId(rs.getInt(1))
                    .setTaskId(rs.getInt(2))
                    .setUserId(rs.getInt(3))
                    .setResult(CodeResultEnum.valueOf(rs.getString(4)))
                    .setCodeLanguageId(rs.getInt(5))
                    .setExecutionTime(rs.getString(6))
                    .setCreatedAt(rs.getTimestamp(7).toLocalDateTime().toInstant(ZoneOffset.UTC))
                    .build();
        }, id);

        return Optional.ofNullable(submissionEntity);
    }

    @Override
    public boolean deleteAllByTasksIds(Set<Integer> tasksIds) {
        if (tasksIds.isEmpty()) {
            return true;
        }

        String sqlUnknownParameter = "?";

        String allParameters = tasksIds.stream()
                .map(taskId -> sqlUnknownParameter)
                .collect(Collectors.joining(", "));

        String queryClauseToAdd = "(" + allParameters + ")";

        jdbcTemplate.update(Queries.DELETE_ALL_BY_TASKS_ID + queryClauseToAdd, tasksIds.toArray(Integer[]::new));
        return true;
    }

    private static class Queries {
        private static final String SAVE = String.format("""
                INSERT INTO `%s` (`task_id`, `user_id`, `result`, `code_language_id`, `execution_time`)
                VALUE
                (?, ?, ?, ?, ?)
                """, SubmissionEntity.TABLE_NAME);

        private static final String FIND_BY_ID = String.format("""
                SELECT `id`, `task_id`, `user_id`, `result`, `code_language_id`, `execution_time`, `created_at`
                FROM `%s`
                WHERE `id` = ?
                """, SubmissionEntity.TABLE_NAME);

        private static final String DELETE_ALL_BY_TASKS_ID = String.format("""
                DELETE FROM `%s`
                WHERE `task_id` IN\s
                """, SubmissionEntity.TABLE_NAME);
    }
}
