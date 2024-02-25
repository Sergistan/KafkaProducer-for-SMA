package com.utochkin.kafkaproducerforsma.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "MessageDto request/response")
public class MessageDto implements Serializable {

    @Schema(description = "Id сообщения", example = "1", type = "integer", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @Schema(description = "Id чата", example = "1", type = "integer")
    @NotNull(message = "ChatId must be not null.")
    private Long chatId;

    @Schema(description = "Текст сообщения", example = "Привет)", type = "string")
    @NotNull(message = "Text must be not null.")
    private String text;

    @Schema(description = "Имя отправителя сообщения", example = "Sergey", type = "string")
    @NotNull(message = "Sender name must be not null.")
    private String senderName;

    @Schema(description = "Дата и время создания сообщения", example = "2024-01-12 13:56", type = "string", pattern = "yyyy-MM-dd HH:mm", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime sentAt;
}
