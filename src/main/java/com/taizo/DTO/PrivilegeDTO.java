package com.taizo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PrivilegeDTO {
	
    private Long privilegeId;
    private boolean create;
    private boolean read;
    private boolean update;
    private boolean delete;
}