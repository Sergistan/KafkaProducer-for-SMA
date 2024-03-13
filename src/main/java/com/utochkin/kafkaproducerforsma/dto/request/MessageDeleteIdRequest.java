package com.utochkin.kafkaproducerforsma.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MessageDeleteIdRequest implements Serializable {
   private Long idMessage;
   private Long idChat;
   @JsonProperty(access = JsonProperty.Access.READ_ONLY)
   private boolean isDeleted;
}
