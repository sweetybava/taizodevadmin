package com.taizo.utils;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

@Component
public class FreeMarkerUtils {

	private static final Logger logger = LoggerFactory.getLogger(FreeMarkerUtils.class);

    /**
     * Freemarker configuration
     */
	@Autowired
    private Configuration configuration;

    /**
     * Get html text to send in the mail.
     * @param templateName - Template name.
     * @param data - Data.
     * @return
     * @throws IOException
     * @throws TemplateException
     */
    public String getHtml(String templateName, Map<String, Object> data)
        throws IOException, TemplateException {

        logger.debug("Get html for template : {}", templateName);

        Template template = configuration.getTemplate(templateName);
        String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, data);

        logger.debug("Html generated : {}", html);
        return html;
    }
    public String getHtml1(String templateName, Map<String, String> data)
            throws IOException, TemplateException {

            logger.debug("Get html for template : {}", templateName);

            Template template = configuration.getTemplate(templateName);
            String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, data);

            logger.debug("Html generated : {}", html);
            return html;
        }
	
}
