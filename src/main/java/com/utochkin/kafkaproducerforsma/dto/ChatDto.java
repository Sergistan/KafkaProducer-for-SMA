package com.utochkin.kafkaproducerforsma.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.utochkin.kafkaproducerforsma.models.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "ChatDto request/response")
public class ChatDto implements Serializable {

    @Schema(description = "Id чата", example = "1", type = "integer", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @Schema(description = "Дата и время создания чата", example = "2024-01-12 13:56", type = "string", pattern = "yyyy-MM-dd HH:mm", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime createdAt;

    @Schema(description = "Последенее сообщение в чате", example = "Последенее сообщение в чате", type = "string",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED, accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String lastMessage;

    @Schema(description = "Id первого пользователя в чате", example = "1", type = "integer")
    @NotNull(message = "FirstUserId must be not null.")
    private Long firstUserId;

    @Schema(description = "Id второго пользователя в чате", example = "2", type = "integer")
    @NotNull(message = "SecondUserId must be not null.")
    private Long secondUserId;

    @JsonIgnore
    private Set<User> users;

}
