package org.example.nextstepbackend.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SelectedCardLabelRequest(

        @NotNull(message = "CardId is required")
        @Positive(message = "CardId must be > 0")
        Integer cardId,

        @NotNull(message = "LabelId is required")
        @Positive(message = "LabelId must be > 0")
        Integer labelId

) {}