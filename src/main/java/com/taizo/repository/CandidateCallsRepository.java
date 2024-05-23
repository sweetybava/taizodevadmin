package com.taizo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.taizo.model.Admin;
import com.taizo.model.CanLanguageModel;
import com.taizo.model.CandidateCallModel;
import com.taizo.model.CandidateCallsModel;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface CandidateCallsRepository extends JpaRepository<CandidateCallsModel, Long> {

    @Query(value = "SELECT * ,count(*) as count FROM candidate_call_registry u where u.empid=?1 group by u.cid,u.empid,u.jid order by u.call_time desc LIMIT 30",nativeQuery = true)
    List<CandidateCallsModel> getEmployerInCallHistory(Integer empId);
    
    @Query(value = "SELECT * ,count(*) as count FROM candidate_call_registry u where u.cid=?1 group by u.cid,u.empid,u.jid order by u.call_time desc LIMIT 20",nativeQuery = true)
    List<CandidateCallsModel> getCandidateInCallHistory(Integer canId);

    @Query(value = "SELECT * FROM candidate_call_registry u where u.empid=?1 and u.jid=?2",nativeQuery = true)
	List<CandidateCallsModel> getCallHistoryByJobId(int employerId, int id);
    
    @Query(value = "SELECT * FROM candidate_call_registry u WHERE u.jid = ?1 ORDER BY u.call_time DESC",
            nativeQuery = true)
     List<CandidateCallsModel> findAllByJidOrderByCallTimeDesc(int jid);
    
    Page<CandidateCallsModel> findAllByOrderByCallTimeDesc(Pageable pageable);

    Page<CandidateCallsModel> findAllByJidOrderByCallTimeDesc(int jid, Pageable pageable);

}
