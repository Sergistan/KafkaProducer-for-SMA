package com.utochkin.kafkaproducerforsma.services.interfaces;


import com.utochkin.kafkaproducerforsma.dto.MessageDto;
import com.utochkin.kafkaproducerforsma.dto.UpdateMessageDto;
import com.utochkin.kafkaproducerforsma.dto.request.MessageDeleteIdRequest;

public interface MessageService {

    MessageDto addMessage(MessageDto messageDTO);

    UpdateMessageDto update(UpdateMessageDto updateMessageDto);

    MessageDeleteIdRequest deleteById(MessageDeleteIdRequest messageDeleteIdRequest);
}
