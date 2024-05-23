package com.taizo.DTO;

import java.util.List;
import java.util.Map;

import com.taizo.model.AdminRolesPrevilegeMappingModel;
import com.taizo.model.CfgAdminPrevilegeModel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminDTO {
	
	private long id;
	private String userName;
    private String profilePic;
    private String emailId;
    private String password;
    private String mobileNo;
    private Long roleId;  
    private String module;
    private List<PrivilegeDTO> privileges;

}
