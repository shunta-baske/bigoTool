package com.example.bingo.dto;

import lombok.Data;

@Data
public class PunchStatusRequest {
    private int index;
    private boolean status;
}
