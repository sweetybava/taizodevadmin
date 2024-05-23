package com.taizo.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;

@Getter
@Setter
@ToString
public class CloudwatchLogEventModel {
    private String type;
    private String message;
    private String Description;
    private HashMap<String, String> logData;
}
