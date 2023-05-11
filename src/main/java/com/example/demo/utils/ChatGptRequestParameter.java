package com.example.demo.utils;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class ChatGptRequestParameter {

    String model = "gpt-3.5-turbo";
//    String model = "gpt-4";

    List<ChatGptMessage> messages = new ArrayList<>();

    public void addMessages(ChatGptMessage message) {
        this.messages.add(message);
    }

}
