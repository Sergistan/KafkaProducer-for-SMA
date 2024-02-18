package com.utochkin.kafkaproducerforsma.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateMessageDto implements Serializable {

    @NotNull(message = "Message id must be not null.")
    private Long id;

    private Long chatId;

    @NotNull(message = "Updated message must be not null.")
    private String updatedText;

    @NotNull(message = "Sender name must be not null.")
    private String senderName;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private boolean isUpdated;
}
