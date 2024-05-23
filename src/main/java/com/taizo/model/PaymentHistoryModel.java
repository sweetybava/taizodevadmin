package com.taizo.model;

import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PaymentHistoryModel {
	
    private int amount;	 
    private int applyLimit;   
    private Date date;
    private String status;

    
    

}
