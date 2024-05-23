package com.taizo.DTO;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceFilterDTO {
	
	private Integer empId;
    private Long adminId;
    private String emailId;
    private String invoiceNo;
    private long invoiceAmount;
    private String invoiceDate;
    private String dueDate;
    private Boolean paid;
    private Date startDate;
    private Date endDate;
    private int page;
    private int size;
    
    public boolean isPaid() {
        return paid;
    }

}
