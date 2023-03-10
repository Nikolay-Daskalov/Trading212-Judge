package com.trading212.judge.model.binding;

import com.trading212.judge.web.validation.IsJSON;
import com.trading212.judge.web.validation.NoSpecialCharacters;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public record TaskBindingModel(
        @NotNull
        @NoSpecialCharacters
        String name,
        @NotNull
        Integer documentId,
        @IsJSON
        MultipartFile answers) {
}
