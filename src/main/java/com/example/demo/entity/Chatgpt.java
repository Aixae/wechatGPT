package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class Chatgpt {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String content;

    private String chatgpt;

    private String externalUserid;

    private Long duration;

    private String token;

}
