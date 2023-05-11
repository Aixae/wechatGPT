package com.example.demo.utils;

import lombok.Data;

@Data
public class Choices {

    ChatGptMessage message;
    String finish_reason;
    Integer index;
}

