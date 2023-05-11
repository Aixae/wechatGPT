package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class Knowledge {

    @TableId
    private Integer id;

    private String knowledge;

}
