package com.taizo.model;


import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "candidate_call_registry")
@ToString
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CandidateCallsModel implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = -385578164948938222L;

	@Id
    @Column(name="sno")
    private int sno;

    @Column(name="cid")
    private int cId;

    @Column(name="empid")
    private int empId;

    @Column(name = "jid")
    private int jid;
    
    @Transient
    private int count;

    public Date getCallTime() {
        return callTime;
    }

    public void setCallTime(Date callTime) {
        this.callTime = callTime;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "call_time", insertable=false)
    private Date callTime;

    public int getcId() {
        return cId;
    }

    public void setcId(int cId) {
        this.cId = cId;
    }

    public int getEmpId() {
        return empId;
    }

    public void setEmpId(int empId) {
        this.empId = empId;
    }


    @PrePersist
    protected void onCreate() {
        if (callTime == null) { callTime = new Date(); }
    }

	public int getSno() {
		return sno;
	}

	public void setSno(int sno) {
		this.sno = sno;
	}


	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	
}
