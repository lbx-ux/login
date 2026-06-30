package com.fjq.login.service.impl;


import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendMail(String toEmail,String code){
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("您的登录验证码");

            // 1. 创建 Thymeleaf 的上下文对象，用于存放变量
            Context context = new Context();
            // 将生成的验证码放入上下文，键名 "code" 必须与 HTML 中的 th:text="${code}" 一致
            context.setVariable("code", code);

            // 2. 渲染模板。第一个参数 "email-code" 对应 templates 目录下的 email-code.html
            String htmlContent = templateEngine.process("email-code", context);

            // 3. 将渲染好的 HTML 字符串设置为邮件内容
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("验证码邮件已成功发送至: {}", toEmail);

        } catch (MessagingException e) {
            log.error("邮件发送失败给: {}", toEmail, e);
            throw new RuntimeException("邮件发送失败", e);
        }
    }
}
