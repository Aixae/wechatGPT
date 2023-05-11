package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("`cursor`")
public class Cursor {

    @TableId
    private Integer id;

    @TableField("`cursor`")
    private String cursor;

    private String token;
}
