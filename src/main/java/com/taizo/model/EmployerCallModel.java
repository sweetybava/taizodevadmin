package com.taizo.model;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "employer_call_registry")
@ToString
public class EmployerCallModel implements Serializable {

    @Id
    @Column(name="sno")
    private int sno;

    @Column(name="cid")
    private int cId;

    @Column(name="empid")
    private int empId;

    @Column(name="jid")
    private int jId;
    
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

    public int getJid() {
        return jId;
    }

    public void setJid(int jid) {
        this.jId = jid;
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

	public int getjId() {
		return jId;
	}

	public void setjId(int jId) {
		this.jId = jId;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}


}
