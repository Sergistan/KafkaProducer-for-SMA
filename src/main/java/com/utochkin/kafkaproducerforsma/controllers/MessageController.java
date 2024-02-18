package com.utochkin.kafkaproducerforsma.controllers;


import com.utochkin.kafkaproducerforsma.dto.MessageDto;
import com.utochkin.kafkaproducerforsma.dto.UpdateMessageDto;
import com.utochkin.kafkaproducerforsma.dto.request.MessageDeleteIdRequest;
import com.utochkin.kafkaproducerforsma.services.interfaces.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@Controller
public class MessageController {

    private final MessageService messageService;

    @RequestMapping("/start")
    public String start() {
        return "start";
    }

    @MessageMapping("/add")
    @SendTo("/chat")
    public MessageDto addMessage(@Payload MessageDto messageDTO) {
        return messageService.addMessage(messageDTO);
    }

    @MessageMapping("/delete")
    @SendTo("/chat")
    public MessageDeleteIdRequest deleteMessage(@Payload MessageDeleteIdRequest messageDeleteIdRequest) {
        return messageService.deleteById(messageDeleteIdRequest);
    }

    @MessageMapping("/update")
    @SendTo("/chat")
    public UpdateMessageDto updateMessage(@Payload UpdateMessageDto updateMessageDto) {
        return messageService.update(updateMessageDto);
    }

}
