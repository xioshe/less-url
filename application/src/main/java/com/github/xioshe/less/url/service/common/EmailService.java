package com.github.xioshe.less.url.service.common;

import com.github.xioshe.less.url.config.AppProperties;
import com.github.xioshe.less.url.entity.EmailTemplate;
import com.github.xioshe.less.url.repository.EmailTemplateRepository;
import com.google.common.base.Strings;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.github.xioshe.less.url.service.auth.VerificationType.REGISTER;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailTemplateRepository templateRepository;
    private final AppProperties app;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Async
    public void sendEmail(String to, String templateName, Map<String, Object> variables) {
        log.debug("Sending email to: {}", to);
        sendEmailWithAttachment(to, templateName, variables, null);
    }

    @Async
    public void sendEmailWithAttachment(String to, String templateName, Map<String, Object> variables, List<AttachmentVO> attachments) {
        log.debug("Sending email with attachment to: {}", to);
        try {
            EmailTemplate template = getTemplate(templateName);
            String content = replaceVariables(template.getContent(), variables);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(Strings.nullToEmpty(template.getSubject()));
            helper.setText(content, true);

            if (attachments != null) {
                for (AttachmentVO attachment : attachments) {
                    helper.addAttachment(attachment.getName(), attachment.getFile());
                }
            }

            if (notMock()) {
                mailSender.send(message);
            }
            log.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to: {}", to, e);
        }
    }

    private EmailTemplate getTemplate(String templateName) {
        EmailTemplate template = templateRepository.findByName(templateName);
        if (template == null) {
            throw new RuntimeException("Template not found: " + templateName);
        }
        return template;
    }

    private String replaceVariables(String content, Map<String, Object> variables) {
        content = Strings.nullToEmpty(content);
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            content = content.replace("{{ " + entry.getKey() + " }}", entry.getValue().toString());
        }
        return content;
    }

    private boolean notMock() {
        return !app.isMockEmail();
    }

    public void sendRegisterVerificationEmail(String email, String code, int expirationMinutes) {
        Map<String, Object> variables = Map.of("code", code, "expireTime", expirationMinutes);
        sendEmail(email, REGISTER.getTemplateName(), variables);
    }
}
