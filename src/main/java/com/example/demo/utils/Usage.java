package com.example.demo.utils;

import lombok.Data;

@Data
public class Usage {

    String prompt_tokens;
    String completion_tokens;
    String total_tokens;
}
