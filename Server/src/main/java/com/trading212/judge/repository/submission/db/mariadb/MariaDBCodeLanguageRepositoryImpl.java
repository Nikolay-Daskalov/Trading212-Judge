package com.trading212.judge.repository.submission.db.mariadb;

import com.trading212.judge.model.entity.submission.CodeLanguageEntity;
import com.trading212.judge.model.entity.submission.enums.CodeLanguageEnum;
import com.trading212.judge.repository.submission.CodeLanguageRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MariaDBCodeLanguageRepositoryImpl implements CodeLanguageRepository {

    private final JdbcTemplate jdbcTemplate;

    public MariaDBCodeLanguageRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    @Override
    public Optional<Integer> findIdByName(CodeLanguageEnum codeLanguage) {
        return jdbcTemplate.query(Queries.GET_ID_BY_NAME, (rs) -> {
            if (!rs.next()) {
                return Optional.empty();
            }

            return Optional.of(rs.getInt(1));
        }, codeLanguage.name());
    }


    private static class Queries {
        private static final String GET_ID_BY_NAME = String.format("""
                SELECT `id`
                FROM `%s`
                WHERE `name` = ?
                """, CodeLanguageEntity.TABLE_NAME);
    }
}
