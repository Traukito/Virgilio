package com.cs.virgilio.model;

import lombok.Data;

import java.util.List;

@Data
public class FracttalResponseDto<T> {
    private boolean success;
    private String message;
    private List<T> data;
    private int total;
}

