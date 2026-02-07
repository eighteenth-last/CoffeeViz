package com.coffeeviz.export.service.impl;

import com.coffeeviz.export.exception.ExportException;
import com.coffeeviz.export.service.ExportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * 导出服务实现
 * 使用 Mermaid CLI 进行图表导出
 * 
 * @author CoffeeViz Team
 * @since 1.0.0
 */
@Slf4j
@Service
public class ExportServiceImpl implements ExportService {
    
    private static final String MMDC_COMMAND = getMmdcCommand();
    
    /**
     * 获取 mmdc 命令路径
     * Windows 需要使用 mmdc.cmd，Linux/Mac 使用 mmdc
     */
    private static String getMmdcCommand() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            // Windows 系统，尝试找到 mmdc.cmd 的完整路径
            try {
                Process process = Runtime.getRuntime().exec("where.exe mmdc.cmd");
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
                );
                String path = reader.readLine();
                if (path != null && !path.isEmpty()) {
                    log.info("找到 Mermaid CLI 路径: {}", path);
                    return path;
                }
            } catch (Exception e) {
                log.warn("无法自动检测 mmdc 路径，使用默认值: {}", e.getMessage());
            }
            return "mmdc.cmd";
        } else {
            return "mmdc";
        }
    }
    
    @Override
    public byte[] exportSvg(String mermaidCode) throws ExportException {
        log.debug("开始导出 SVG，Mermaid 代码长度: {}", mermaidCode.length());
        
        try {
            // 创建临时文件
            Path tempMmdFile = Files.createTempFile("mermaid-", ".mmd");
            Path tempSvgFile = Files.createTempFile("mermaid-", ".svg");
            
            try {
                // 写入 Mermaid 代码
                Files.writeString(tempMmdFile, mermaidCode, StandardCharsets.UTF_8);
                
                // 调用 Mermaid CLI
                ProcessBuilder pb = new ProcessBuilder(
                    MMDC_COMMAND,
                    "-i", tempMmdFile.toString(),
                    "-o", tempSvgFile.toString(),
                    "-t", "default"
                );
                
                Process process = pb.start();
                int exitCode = process.waitFor();
                
                if (exitCode != 0) {
                    // 读取错误信息
                    BufferedReader errorReader = new BufferedReader(
                        new InputStreamReader(process.getErrorStream())
                    );
                    StringBuilder errorMsg = new StringBuilder();
                    String line;
                    while ((line = errorReader.readLine()) != null) {
                        errorMsg.append(line).append("\n");
                    }
                    
                    log.warn("Mermaid CLI 执行失败，退出码: {}, 错误: {}", exitCode, errorMsg);
                    
                    // 如果 CLI 不可用，返回简单的 SVG 占位符
                    return generatePlaceholderSvg(mermaidCode).getBytes(StandardCharsets.UTF_8);
                }
                
                // 读取生成的 SVG
                byte[] svgBytes = Files.readAllBytes(tempSvgFile);
                log.debug("SVG 导出成功，大小: {} bytes", svgBytes.length);
                return svgBytes;
                
            } finally {
                // 清理临时文件
                Files.deleteIfExists(tempMmdFile);
                Files.deleteIfExists(tempSvgFile);
            }
            
        } catch (Exception e) {
            log.error("导出 SVG 失败", e);
            // 返回占位符而不是抛出异常
            return generatePlaceholderSvg(mermaidCode).getBytes(StandardCharsets.UTF_8);
        }
    }
    
    @Override
    public byte[] exportPng(String mermaidCode, int width, int height) throws ExportException {
        log.debug("开始导出 PNG，尺寸: {}x{}", width, height);
        
        try {
            // 创建临时文件
            Path tempMmdFile = Files.createTempFile("mermaid-", ".mmd");
            Path tempPngFile = Files.createTempFile("mermaid-", ".png");
            
            try {
                // 写入 Mermaid 代码
                Files.writeString(tempMmdFile, mermaidCode, StandardCharsets.UTF_8);
                
                // 调用 Mermaid CLI，添加缩放参数提高清晰度
                ProcessBuilder pb = new ProcessBuilder(
                    MMDC_COMMAND,
                    "-i", tempMmdFile.toString(),
                    "-o", tempPngFile.toString(),
                    "-w", String.valueOf(width),
                    "-H", String.valueOf(height),
                    "-s", "2",  // 缩放因子 2x，提高清晰度
                    "-t", "default"
                );
                
                Process process = pb.start();
                int exitCode = process.waitFor();
                
                if (exitCode != 0) {
                    log.warn("Mermaid CLI 执行失败，退出码: {}", exitCode);
                    // 返回 1x1 透明 PNG
                    return generatePlaceholderPng();
                }
                
                // 读取生成的 PNG
                byte[] pngBytes = Files.readAllBytes(tempPngFile);
                log.debug("PNG 导出成功，大小: {} bytes", pngBytes.length);
                return pngBytes;
                
            } finally {
                // 清理临时文件
                Files.deleteIfExists(tempMmdFile);
                Files.deleteIfExists(tempPngFile);
            }
            
        } catch (Exception e) {
            log.error("导出 PNG 失败", e);
            // 返回占位符而不是抛出异常
            return generatePlaceholderPng();
        }
    }
    
    @Override
    public String exportMermaid(String mermaidCode) {
        return mermaidCode;
    }
    
    /**
     * 生成占位符 SVG（当 Mermaid CLI 不可用时）
     */
    private String generatePlaceholderSvg(String mermaidCode) {
        return String.format(
            "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"800\" height=\"600\">" +
            "<rect width=\"100%%\" height=\"100%%\" fill=\"#f9f9f9\"/>" +
            "<text x=\"50%%\" y=\"50%%\" text-anchor=\"middle\" font-family=\"Arial\" font-size=\"16\" fill=\"#666\">" +
            "Mermaid CLI 不可用，请安装 @mermaid-js/mermaid-cli" +
            "</text>" +
            "<text x=\"50%%\" y=\"55%%\" text-anchor=\"middle\" font-family=\"monospace\" font-size=\"12\" fill=\"#999\">" +
            "npm install -g @mermaid-js/mermaid-cli" +
            "</text>" +
            "</svg>"
        );
    }
    
    /**
     * 生成占位符 PNG（1x1 透明像素）
     */
    private byte[] generatePlaceholderPng() {
        // 1x1 透明 PNG 的字节数组
        return new byte[] {
            (byte)0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
            0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
            0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
            0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, (byte)0xC4,
            (byte)0x89, 0x00, 0x00, 0x00, 0x0A, 0x49, 0x44, 0x41, 0x54,
            0x78, (byte)0x9C, 0x63, 0x00, 0x01, 0x00, 0x00, 0x05,
            0x00, 0x01, 0x0D, 0x0A, 0x2D, (byte)0xB4, 0x00, 0x00,
            0x00, 0x00, 0x49, 0x45, 0x4E, 0x44, (byte)0xAE, 0x42,
            0x60, (byte)0x82
        };
    }
}
