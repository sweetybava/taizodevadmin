package com.taizo.controller.candidate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.utils.PdfMerger;
import com.taizo.model.CanLanguageModel;
import com.taizo.model.CandidateModel;
import com.taizo.model.EmployerModel;
import com.taizo.model.LanguagesModel;
import com.taizo.model.UserModel;
import com.taizo.repository.CanLanguagesRepository;
import com.taizo.repository.CandidateRepository;
import com.taizo.repository.EmployerRepository;
import com.taizo.repository.UserRepository;
import com.taizo.utils.FreeMarkerUtils;
import com.taizo.utils.ImageConverter;

import freemarker.template.TemplateException;

@CrossOrigin
@Controller
public class PDFController {

	@Autowired
	private FreeMarkerUtils freeMarkerUtils;

	@Autowired
	CandidateRepository candidateRepository;
	
	@Autowired
	EmployerRepository employerRepository;
	
	@Autowired
	UserRepository userRepository;

	@PersistenceContext
	EntityManager em;
	
	@Autowired
	CanLanguagesRepository canLanguagesRepository;
	
	@RequestMapping(path = "/whatsappimage")
	public String getWhatsappImage(HttpServletRequest request, HttpServletResponse response)
			throws IOException, TemplateException {

		String htmlFilePath = "C:\\Users\\hi\\Documents\\workspace-spring-tool-suite-4-4.7.0.RELEASE\\taizo-java\\src\\main\\resources\\templates\\email\\receipt.html";

		String imageFilePath = "C:\\Users\\hi\\Documents\\workspace-spring-tool-suite-4-4.7.0.RELEASE\\taizo-java\\src\\main\\resources\\static\\images\\receipt.png";

		ImageConverter converter = new ImageConverter();
		converter.convertHtmlToImage(htmlFilePath, imageFilePath);
		return null;

	}

	@RequestMapping("/in")
	public String getHomepage() {
		return "invoice";
	}
	
	@RequestMapping(path = "/downloadInvoice")
	public ResponseEntity<?> getInvoice(@RequestParam("can_id") final int empId, HttpServletRequest request,
			HttpServletResponse response) throws IOException, TemplateException {

		Optional<EmployerModel> d1 = employerRepository.findById(empId);
		EmployerModel d = d1.get();
		
		int amount = 1799;
		double GST = (amount*9.0/100.0);
		double tax = amount-GST*2;
		
		DateFormat formatter = new SimpleDateFormat("MMMMM dd, yyyy");
		String date = formatter.format(new Date());

		HashMap<String, String> emailDataHM = new HashMap<>();
		emailDataHM.put("CompanyName", d.getCompanyName() != null ? d.getCompanyName() : "");
		emailDataHM.put("InvoiceNo", "TZ/22-23/00082");
		emailDataHM.put("InvoiceDate", date);
		emailDataHM.put("Address", d.getAddress() != null ? d.getAddress() : "");
		emailDataHM.put("gstNum", d.getRegProofNumber() != null ? d.getRegProofNumber() : "");
		emailDataHM.put("Email", d.getEmailId() != null ? d.getEmailId() : "");
		emailDataHM.put("MobileNum", String.valueOf(d.getMobileNumber()));
		DecimalFormat df = new DecimalFormat("0.00");
		df.setDecimalSeparatorAlwaysShown(true);
		emailDataHM.put("tax", df.format(tax));
		DecimalFormat dff = new DecimalFormat("0.00");
		df.setDecimalSeparatorAlwaysShown(true);
		emailDataHM.put("GST", dff.format(GST));
		DecimalFormat df1 = new DecimalFormat("0.00");
		df.setDecimalSeparatorAlwaysShown(true);
		emailDataHM.put("amount", df1.format(amount));

		String message = freeMarkerUtils.getHtml1("invoice.html", emailDataHM);
		
		ByteArrayOutputStream target = new ByteArrayOutputStream();
   
		ConverterProperties converterProperties = new ConverterProperties();
		converterProperties.setBaseUri("http://localhost:8000");

		HtmlConverter.convertToPdf(message, target, converterProperties);

		byte[] bytes = target.toByteArray();

		return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).body(bytes);

	}

	@RequestMapping(path = "/downloadCV")
	public ResponseEntity<?> getPDF(@RequestParam("can_id") final int canId, HttpServletRequest request,
			HttpServletResponse response) throws IOException, TemplateException {

		/* Do Business Logic */

		Optional<CandidateModel> d1 = candidateRepository.findById(canId);
		Optional<UserModel> user = userRepository.findById(d1.get().getUserId());
		CandidateModel d = d1.get();
		
		List<CanLanguageModel> details = canLanguagesRepository.findByCandidateId(d.getId());
		String commaseparatedLanlist = null;
		if(!details.isEmpty()) {
		List<LanguagesModel> persons = null;
		Set<Integer> list = new HashSet();

		int j = 0;
	  String lan = null;
		List<String> lang = new ArrayList<String>();


		for (CanLanguageModel s : details) {
			j = s.getLanguageId();
			list.add(j);
		}

		persons = em.createQuery("SELECT j FROM LanguagesModel j WHERE j.id IN :ids").setParameter("ids", list)
				.getResultList();
		
		if(persons!=null && !persons.isEmpty()) {
		for (LanguagesModel s : persons) {

			lan = s.getKeyword();
			lang.add(lan);
		}
		}
		commaseparatedLanlist = lang.toString();
        
		commaseparatedLanlist
            = commaseparatedLanlist.replace("[", "")
                  .replace("]", "");
		}

		String edu = d.getQualification();
		if(d.getSpecification()!=null && !d.getSpecification().isEmpty()) {
			edu = d.getSpecification();
		}
		
		HashMap<String, String> emailDataHM = new HashMap<>();
		emailDataHM.put("name", d.getFirstName() != null ? d.getFirstName() : "");
		emailDataHM.put("key", d.getKeySkill() != null ? d.getKeySkill() : "");
		emailDataHM.put("gender", d.getGender() != null ? d.getGender() : "");
		emailDataHM.put("age", String.valueOf(d.getAge()));
		emailDataHM.put("education", edu != null ? edu : "");
		emailDataHM.put("photo", user.get().getProfilePic() != null ? user.get().getProfilePic() : "");
		emailDataHM.put("languagesKnown", commaseparatedLanlist != null ? commaseparatedLanlist : "");
		emailDataHM.put("exp", String.valueOf(d.getExperience()));
		emailDataHM.put("months", String.valueOf(d.getExpMonths()));
		emailDataHM.put("mobNum", String.valueOf(d.getMobileNumber()));
		emailDataHM.put("whatsappNum", String.valueOf(d.getWhatsappNumber()));
		String loc = d.getCurrentCity()+", "+d.getCurrentState()+", "+d.getPerCountry();
		emailDataHM.put("address", loc);

		if (d.isExperienced()) {
			String exp = d.getJobCategory()+" with "+d.getExperience()+" year(s) experience in "+d.getIndustry();
			emailDataHM.put("profileDescription", exp);
			emailDataHM.put("role", d.getJobCategory());
			emailDataHM.put("RoleHide", "");
		} else {
			emailDataHM.put("profileDescription",
					"Hardworking individual looking for an entry-level role in the manufacturing industry");
			emailDataHM.put("role", d.getJobCategory());
			emailDataHM.put("RoleHide", "none");
		}
		if (d.getEmailId() == null) {
			emailDataHM.put("email", "");
			emailDataHM.put("EmailHide", "none");
		} else {
			emailDataHM.put("EmailHide", "");
			emailDataHM.put("email", d.getEmailId());
		}
		if (d.getCertificationSpecialization() == null) {
			emailDataHM.put("CertificationHide", "none");
			emailDataHM.put("certificates", "");
		} else {
			emailDataHM.put("CertificationHide", "");
			emailDataHM.put("certificates", d.getCertificationSpecialization());
		}

		/* Create HTML using Thymeleaf template Engine */
		String message = freeMarkerUtils.getHtml1("ResumeTemplate.html", emailDataHM);
		

		/* Setup Source and target I/O streams */

		ByteArrayOutputStream target = new ByteArrayOutputStream();

		/* Setup converter properties. */
		ConverterProperties converterProperties = new ConverterProperties();
		converterProperties.setBaseUri("http://localhost:8000");

		/* Call convert method */
		HtmlConverter.convertToPdf(message, target, converterProperties);
		// HtmlConverter.convertToPdf(new
		// File("./src/main/resources/templates/email/Test.html"),new
		// File("simple-output.pdf"),converterProperties);

		/* extract output as bytes */
		byte[] bytes = target.toByteArray();

		/* Send the response as downloadable PDF */

		return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).body(bytes);

	}
	@RequestMapping(path = "/SLApdf")
	public ResponseEntity<?> getInvoice1(@RequestParam("emp_id") final int empId, HttpServletRequest request,
			HttpServletResponse response) throws IOException, TemplateException, FileNotFoundException, java.io.IOException {

		String BASEURI = "./src/main/resources/";
		String ENDURI = "templates/email/";
		String ImageENDURI = "static/images/";

		String[] SRC = { String.format("%sSLA1.html", BASEURI + ENDURI),
				String.format("%sSLA2.html", BASEURI + ENDURI), };

		ConverterProperties properties = new ConverterProperties();
		properties.setBaseUri(BASEURI + ImageENDURI);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        PdfMerger merger = new PdfMerger(pdf);

        for (String html : SRC) {
        PdfDocument temp = new PdfDocument(new PdfWriter(baos));
        HtmlConverter.convertToPdf(new FileInputStream(html), temp, properties);
        temp = new PdfDocument(new PdfReader(new ByteArrayInputStream(baos.toByteArray())));
        merger.merge(temp, 1, temp.getNumberOfPages());
        temp.close();
        }
        pdf.close();
		byte[] bytes = baos.toByteArray();
		return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).body(bytes);

	} 
}
