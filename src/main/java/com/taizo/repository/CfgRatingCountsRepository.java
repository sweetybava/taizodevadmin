package com.taizo.repository;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.taizo.model.CfgLanguagesModel;
import com.taizo.model.CfgRatingCountsModel;
import com.taizo.model.EmployerJobPersonalizationModel;

@Repository
public interface CfgRatingCountsRepository extends JpaRepository<CfgRatingCountsModel, Long> {

	/*
	 * static final String CASE_WHEN = "CASE" +
	 * "    WHEN ?3 = 'en' THEN r.reason_en" + "    WHEN ?3 = 'ta' THEN r.reason_ta"
	 * + "    WHEN ?3 = 'hi' THEN r.reason_hi" +
	 * "    WHEN ?3 = 'day_04' THEN r.reason_ml" +
	 * "    WHEN ?3 = 'day_05' THEN r.reason_ur" + "    ELSE 0" + "END";
	 */
	
    static final String CASE_WHEN_REASON = "CASE"
            + "    WHEN ?3 = 'en' THEN r.reason_en"
            + "    WHEN ?3 = 'ta' THEN r.reason_ta"
            + "    WHEN ?3 = 'hi' THEN r.reason_hi"
            + "    WHEN ?3 = 'as' THEN r.reason_as"
            + "    WHEN ?3 = 'bho' THEN r.reason_bho"
            + "    WHEN ?3 = 'bn' THEN r.reason_bn"
            + "    WHEN ?3 = 'gu' THEN r.reason_gu"
            + "    WHEN ?3 = 'kn' THEN r.reason_kn"
            + "    WHEN ?3 = 'ml' THEN r.reason_ml"
            + "    WHEN ?3 = 'mr' THEN r.reason_mr"
            + "    WHEN ?3 = 'or' THEN r.reason_or"
            + "    WHEN ?3 = 'pa' THEN r.reason_pa"
            + "    WHEN ?3 = 'te' THEN r.reason_te"
            + "    WHEN ?3 = 'ur' THEN r.reason_ur"
            + "    ELSE r.reason_en\n"
            + "END\n";
    
    static final String CASE_WHEN_QUES = "CASE"
            + "    WHEN ?3 = 'en' THEN c.question_en"
            + "    WHEN ?3 = 'ta' THEN c.question_ta"
            + "    WHEN ?3 = 'hi' THEN c.question_hi"
            + "    WHEN ?3 = 'as' THEN c.question_as"
            + "    WHEN ?3 = 'bho' THEN c.question_bho"
            + "    WHEN ?3 = 'bn' THEN c.question_bn"
            + "    WHEN ?3 = 'gu' THEN c.question_gu"
            + "    WHEN ?3 = 'kn' THEN c.question_kn"
            + "    WHEN ?3 = 'ml' THEN c.question_ml"
            + "    WHEN ?3 = 'mr' THEN c.question_mr"
            + "    WHEN ?3 = 'or' THEN c.question_or"
            + "    WHEN ?3 = 'pa' THEN c.question_pa"
            + "    WHEN ?3 = 'te' THEN c.question_te"
            + "    WHEN ?3 = 'ur' THEN c.question_ur"
            + "    ELSE c.question_en\n"
            + "END\n";

	List<CfgRatingCountsModel> findByModule(String module);

	@Query(value = "SELECT e FROM CfgRatingCountsModel e where e.module = ?1 and e.ratingCount = ?2 ")
	CfgRatingCountsModel findByModuleandCount(String module, int ratingCount);

	@Query(value = "SELECT r.rating_id,c.question_en,r.reason_en, " + CASE_WHEN_QUES + " AS question, " + CASE_WHEN_REASON + " AS reason FROM cfg_rating_counts c left join cfg_rating_reasons r on c.id=r.rating_id where c.module = ?1 and c.rating_count = ?2 ;", nativeQuery = true)
	List<Map<String, Object>> findByModuleandCountTran(String module, int ratingCount, String key);

}
