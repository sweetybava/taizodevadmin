package com.taizo.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cfg_emp_timeline_events")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CfgEmpTimelineEvents {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_name")
    private String eventName;

    @Column(name= "event_description")
    private String eventDescription;

    @Column(name = "search_key")
    private String searchKey;

    @Column(name = "active")
    private boolean active;

}
