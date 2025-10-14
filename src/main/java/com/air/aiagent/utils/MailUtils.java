package com.air.aiagent.utils;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import java.util.Map;
import java.util.Random;

@Component
public class MailUtils {

    @Value("${spring.mail.username}")
    private String mainEmail;

    private final JavaMailSender mailSender;

    /**
     * 需要thymeleaf依赖
     */
    private final TemplateEngine templateEngine;


    public MailUtils(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    /**
     * 发送验证码邮件（快捷方法）
     * @param userEmail 收件人邮箱
     * @param code 验证码
     */
    public void sendVerificationCode(String userEmail, String code) throws MessagingException {
        Map<String, Object> variables = Map.of(
                "code", code,
                "expireMinutes", 5
        );
        sendTemplateMail(userEmail, "您的验证码", "email/verification", variables);
    }


    /**
     * 生成6位数字验证码
     * @return 6位数字字符串验证码
     */
    public String generateVerificationCode() {
        StringBuilder code = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }


    /**
     * 使用Thymeleaf模板发送邮件
     * @param userEmail 收件人
     * @param subject 主题
     * @param template 模板路径（如 "email/verification"）
     * @param variables 模板变量
     */
    public void sendTemplateMail(String userEmail, String subject, String template, Map<String, Object> variables)
            throws MessagingException {
        Context context = new Context();
        context.setVariables(variables);
        String html = templateEngine.process(template, context);
        sendHtmlMail(userEmail, subject, html);
    }


    /**
     * 发送HTML邮件
     * @param userEmail 收件人
     * @param subject 主题
     * @param htmlContent HTML内容
     * @throws MessagingException
     */
    public void sendHtmlMail(String userEmail, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom(mainEmail);
        helper.setTo(userEmail);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        mailSender.send(message);
    }
}