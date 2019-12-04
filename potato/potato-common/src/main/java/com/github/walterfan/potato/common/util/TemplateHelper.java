package com.github.walterfan.potato.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Locale;
import java.util.Map;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.springframework.stereotype.Component;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
/**
 * @Author: Walter Fan
 **/
@Component
public class TemplateHelper {
    private Configuration templateConfig;

    public TemplateHelper() {
        templateConfig = new Configuration();

        templateConfig.setDefaultEncoding("UTF-8");
        templateConfig.setLocale(Locale.US);
        templateConfig.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    }

    public String renderTemplate(Map<String, Object> map, String tplFile) throws IOException, TemplateException {
        String tplContent = loadTemplate(tplFile);
        Template template = new Template("ivr_flow", new StringReader(tplContent), templateConfig);

        StringWriter writer = new StringWriter();

        template.process(map, writer);

        return writer.toString();
    }

    public static String loadTemplate(String fileInClassPath) throws IOException {

        try(InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileInClassPath)) {
            if(null == in) {
                return "";
            }
            return IOUtils.toString(in);
        }
    }
}
