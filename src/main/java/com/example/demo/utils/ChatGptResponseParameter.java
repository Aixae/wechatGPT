package com.example.demo.utils;

import lombok.Data;
import java.util.List;

@Data
public class ChatGptResponseParameter {

    String id;
    String object;
    String created;
    String model;
    Usage usage;
    List<Choices> choices;
}

