package com.coffeeviz.service;

import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.coffeeviz.dto.SendEmailRequest;
import com.coffeeviz.entity.*;
import com.coffeeviz.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 邮件服务 - 阿里云邮件推送 (DirectMail) API 调用
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final SysConfigGroupMapper configGroupMapper;
    private final EmailTemplateMapper templateMapper;
    private final EmailLogMapper logMapper;
    private final UserMapper userMapper;

    /**
     * 获取邮件配置
     */
    public JSONObject getEmailConfig() {
        SysConfigGroup group = configGroupMapper.selectOne(
                new LambdaQueryWrapper<SysConfigGroup>().eq(SysConfigGroup::getGroupCode, "email"));
        if (group == null || group.getConfigValue() == null) return new JSONObject();
        JSONObject json = JSON.parseObject(group.getConfigValue());
        json.put("enabled", group.getStatus() != null && group.getStatus() == 1);
        return json;
    }

    /**
     * 发送邮件（管理员手动触发）
     */
    @Async
    public void sendEmail(SendEmailRequest request) {
        JSONObject config = getEmailConfig();
        if (!Boolean.TRUE.equals(config.getBoolean("enabled"))) {
            log.warn("邮件服务未启用");
            return;
        }

        // 解析模板
        EmailTemplate template = null;
        if (request.getTemplateCode() != null) {
            template = templateMapper.selectOne(
                    new LambdaQueryWrapper<EmailTemplate>()
                            .eq(EmailTemplate::getTemplateCode, request.getTemplateCode()));
        }

        String subject = request.getSubject();
        String content = request.getContent();
        if (template != null) {
            if (subject == null || subject.isBlank()) subject = template.getSubject();
            if (content == null || content.isBlank()) content = template.getContent();
        }

        // 收集收件人
        List<String> recipients = resolveRecipients(request.getTarget(), request.getEmails());
        if (recipients.isEmpty()) {
            log.warn("没有找到收件人");
            return;
        }

        Long senderId = null;
        try { senderId = StpUtil.getLoginIdAsLong(); } catch (Exception ignored) {}

        // 逐个发送
        for (String email : recipients) {
            String finalSubject = replaceVars(subject, email);
            String finalContent = replaceVars(content, email);

            EmailLog emailLog = new EmailLog();
            emailLog.setTemplateCode(request.getTemplateCode());
            emailLog.setToEmail(email);
            emailLog.setSubject(finalSubject);
            emailLog.setContent(finalContent);
            emailLog.setTarget(request.getTarget());
            emailLog.setSenderId(senderId);

            boolean testMode = Boolean.TRUE.equals(config.getBoolean("testMode"));
            if (testMode) {
                // 测试模式：发送到测试邮箱
                String testEmail = config.getString("testEmail");
                if (testEmail != null && !testEmail.isBlank()) {
                    email = testEmail;
                }
            }

            try {
                callAliyunDirectMail(config, email, finalSubject, finalContent);
                emailLog.setStatus("sent");
                log.info("邮件发送成功: to={}", email);
            } catch (Exception e) {
                emailLog.setStatus("failed");
                emailLog.setErrorMsg(e.getMessage());
                log.error("邮件发送失败: to={}", email, e);
            }
            logMapper.insert(emailLog);
        }
    }

    /**
     * 发送单封邮件（供事件监听器等内部调用）
     */
    public void sendSingleEmail(String toEmail, String subject, String htmlContent, String templateCode) {
        log.info("[自动邮件] 开始发送: to={}, template={}, subject={}", toEmail, templateCode, subject);
        JSONObject config = getEmailConfig();
        if (!Boolean.TRUE.equals(config.getBoolean("enabled"))) {
            log.warn("[自动邮件] 邮件服务未启用，跳过发送");
            return;
        }

        boolean testMode = Boolean.TRUE.equals(config.getBoolean("testMode"));
        String actualTo = toEmail;
        if (testMode) {
            String testEmail = config.getString("testEmail");
            if (testEmail != null && !testEmail.isBlank()) {
                actualTo = testEmail;
                log.info("[自动邮件] 测试模式，实际发送到: {}", actualTo);
            }
        }

        EmailLog emailLog = new EmailLog();
        emailLog.setTemplateCode(templateCode);
        emailLog.setToEmail(toEmail);
        emailLog.setSubject(subject);
        emailLog.setContent(htmlContent);
        emailLog.setTarget("auto");

        try {
            callAliyunDirectMail(config, actualTo, subject, htmlContent);
            emailLog.setStatus("sent");
            log.info("邮件发送成功: to={}, template={}", actualTo, templateCode);
        } catch (Exception e) {
            emailLog.setStatus("failed");
            emailLog.setErrorMsg(e.getMessage());
            log.error("邮件发送失败: to={}", actualTo, e);
        }
        logMapper.insert(emailLog);
    }

    /**
     * 发送测试邮件
     */
    public String sendTestEmail(String toEmail) {
        JSONObject config = getEmailConfig();
        if (!Boolean.TRUE.equals(config.getBoolean("enabled"))) {
            return "邮件服务未启用，请先启用并保存配置";
        }
        try {
            callAliyunDirectMail(config, toEmail,
                    "CoffeeViz 邮件测试",
                    "<div style='max-width:600px;margin:0 auto;font-family:sans-serif;color:#333'>" +
                    "<h2 style='color:#10b981'>邮件配置测试成功 ✅</h2>" +
                    "<p>如果你收到这封邮件，说明阿里云邮件推送配置正确。</p>" +
                    "<p style='color:#999;font-size:12px;margin-top:32px'>— CoffeeViz 团队</p></div>");
            return "测试邮件已发送至 " + toEmail;
        } catch (Exception e) {
            log.error("测试邮件发送失败", e);
            return "发送失败: " + e.getMessage();
        }
    }

    /**
     * 调用阿里云 DirectMail SingleSendMail API
     */
    private void callAliyunDirectMail(JSONObject config, String toEmail, String subject, String htmlBody) throws Exception {
        String accessKeyId = config.getString("apiKey");
        String accessKeySecret = config.getString("secretKey");
        String senderEmail = config.getString("senderEmail");
        String senderName = config.getString("senderName");
        if (senderName == null) senderName = "CoffeeViz";

        // 构建公共参数
        Map<String, String> params = new TreeMap<>();
        params.put("Action", "SingleSendMail");
        params.put("AccountName", senderEmail);
        params.put("ReplyToAddress", "false");
        params.put("AddressType", "1");
        params.put("ToAddress", toEmail);
        params.put("FromAlias", senderName);
        params.put("Subject", subject);
        params.put("HtmlBody", htmlBody);
        params.put("Format", "JSON");
        params.put("Version", "2015-11-23");
        params.put("AccessKeyId", accessKeyId);
        params.put("SignatureMethod", "HMAC-SHA1");
        params.put("SignatureVersion", "1.0");
        params.put("SignatureNonce", UUID.randomUUID().toString());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        params.put("Timestamp", sdf.format(new Date()));

        // 计算签名
        String signature = sign(params, accessKeySecret);
        params.put("Signature", signature);

        // 构建请求体
        String body = params.entrySet().stream()
                .map(e -> encode(e.getKey()) + "=" + encode(e.getValue()))
                .collect(Collectors.joining("&"));

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://dm.aliyuncs.com/"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("阿里云邮件API返回错误: " + response.body());
        }
        JSONObject result = JSON.parseObject(response.body());
        if (result.containsKey("Code") && !"OK".equals(result.getString("Code"))) {
            throw new RuntimeException(result.getString("Message"));
        }
    }

    private String sign(Map<String, String> params, String secret) throws Exception {
        String sortedQuery = params.entrySet().stream()
                .map(e -> encode(e.getKey()) + "=" + encode(e.getValue()))
                .collect(Collectors.joining("&"));
        String stringToSign = "POST&" + encode("/") + "&" + encode(sortedQuery);

        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec((secret + "&").getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
        byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(signData);
    }

    private String encode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8)
                    .replace("+", "%20").replace("*", "%2A").replace("%7E", "~");
        } catch (Exception e) {
            return value;
        }
    }

    /**
     * 解析收件人列表
     */
    private List<String> resolveRecipients(String target, String emails) {
        if ("specific".equals(target) && emails != null) {
            return Arrays.stream(emails.split("[,;\\s]+"))
                    .filter(e -> e.contains("@"))
                    .distinct()
                    .collect(Collectors.toList());
        }

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .isNotNull(User::getEmail)
                .ne(User::getEmail, "")
                .eq(User::getStatus, 1);

        if ("pro".equals(target)) {
            // 查询有活跃 PRO 订阅的用户（简化：查所有活跃用户，实际可 join 订阅表）
            // 这里先返回所有用户，后续可优化
        }

        List<User> users = userMapper.selectList(wrapper);
        return users.stream()
                .map(User::getEmail)
                .filter(e -> e != null && e.contains("@"))
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 简单变量替换（{{username}} 等）
     */
    private String replaceVars(String text, String email) {
        if (text == null) return "";
        // 根据邮箱查用户名
        String username = email;
        try {
            User user = userMapper.selectOne(
                    new LambdaQueryWrapper<User>().eq(User::getEmail, email).last("LIMIT 1"));
            if (user != null) {
                username = user.getDisplayName() != null ? user.getDisplayName() : user.getUsername();
            }
        } catch (Exception ignored) {}

        return text.replace("{{username}}", username)
                   .replace("{{email}}", email);
    }
}
