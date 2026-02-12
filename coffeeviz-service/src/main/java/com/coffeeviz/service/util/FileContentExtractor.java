package com.coffeeviz.service.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

/**
 * 文件内容提取工具
 * <p>
 * 支持从 .txt / .md / .docx / .pdf 文件中提取纯文本内容。
 * </p>
 *
 * @author CoffeeViz Team
 * @since 1.4.0
 */
@Slf4j
public class FileContentExtractor {

    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of(
            "txt", "md", "markdown", "docx", "pdf"
    );

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    /**
     * 判断文件扩展名是否支持
     */
    public static boolean isSupported(String filename) {
        if (filename == null) return false;
        String ext = getExtension(filename).toLowerCase();
        return SUPPORTED_EXTENSIONS.contains(ext);
    }

    /**
     * 从上传文件中提取文本内容
     *
     * @param file 上传的文件
     * @return 提取的文本内容
     * @throws Exception 解析失败
     */
    public static String extract(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件为空");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("文件大小超过限制（最大 10MB）");
        }

        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new IllegalArgumentException("无法获取文件名");
        }

        String ext = getExtension(filename).toLowerCase();
        log.info("提取文件内容: filename={}, ext={}, size={}KB", filename, ext, file.getSize() / 1024);

        return switch (ext) {
            case "txt", "md", "markdown" -> extractFromText(file);
            case "docx" -> extractFromDocx(file);
            case "pdf" -> extractFromPdf(file);
            default -> throw new IllegalArgumentException("不支持的文件格式: " + ext
                    + "，支持: " + String.join(", ", SUPPORTED_EXTENSIONS));
        };
    }

    /**
     * 提取纯文本 / Markdown 文件
     */
    private static String extractFromText(MultipartFile file) throws Exception {
        try (InputStream is = file.getInputStream()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /**
     * 提取 .docx 文件内容
     */
    private static String extractFromDocx(MultipartFile file) throws Exception {
        try (InputStream is = file.getInputStream();
             XWPFDocument doc = new XWPFDocument(is)) {

            List<XWPFParagraph> paragraphs = doc.getParagraphs();
            StringBuilder sb = new StringBuilder();

            for (XWPFParagraph para : paragraphs) {
                String text = para.getText();
                if (text == null || text.isBlank()) {
                    sb.append("\n");
                    continue;
                }

                // 根据段落样式推断标题层级，转为 Markdown 格式
                String style = para.getStyleID();
                if (style != null) {
                    if (style.matches("(?i)heading1|标题1")) {
                        sb.append("# ").append(text).append("\n");
                    } else if (style.matches("(?i)heading2|标题2")) {
                        sb.append("## ").append(text).append("\n");
                    } else if (style.matches("(?i)heading3|标题3")) {
                        sb.append("### ").append(text).append("\n");
                    } else if (style.matches("(?i)heading4|标题4")) {
                        sb.append("#### ").append(text).append("\n");
                    } else {
                        sb.append(text).append("\n");
                    }
                } else {
                    sb.append(text).append("\n");
                }
            }

            return sb.toString();
        }
    }

    /**
     * 提取 .pdf 文件内容
     */
    private static String extractFromPdf(MultipartFile file) throws Exception {
        try (InputStream is = file.getInputStream();
             PDDocument doc = Loader.loadPDF(is.readAllBytes())) {

            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(doc);
        }
    }

    private static String getExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex >= 0 ? filename.substring(dotIndex + 1) : "";
    }
}
