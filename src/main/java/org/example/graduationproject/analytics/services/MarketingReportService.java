package org.example.graduationproject.analytics.services;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.graduationproject.analytics.models.CustomerCluster;
import org.example.graduationproject.analytics.models.MarketingTactic;
import org.example.graduationproject.analytics.models.MarketingObjective;
import org.example.graduationproject.analytics.repositories.MarketingTacticRepository;
import org.example.graduationproject.analytics.repositories.MarketingObjectiveRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MarketingReportService {
    
    @Autowired
    private CustomerSegmentationService segmentationService;
    
    @Autowired
    private MarketingTacticRepository marketingTacticRepository;
    
    @Autowired
    private MarketingObjectiveRepository marketingObjectiveRepository;
    
    // ========== PDF EXPORT ==========
    
    public byte[] exportMarketingReportToPdf(Integer clusterId) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        try {
            // Tạo font với fallback
            PdfFont font;
            PdfFont boldFont;
            try {
                font = PdfFontFactory.createFont(StandardFonts.TIMES_ROMAN);
                boldFont = PdfFontFactory.createFont(StandardFonts.TIMES_BOLD);
            } catch (Exception e) {
                font = PdfFontFactory.createFont(StandardFonts.COURIER);
                boldFont = PdfFontFactory.createFont(StandardFonts.COURIER_BOLD);
            }

            // Lấy dữ liệu marketing strategy trực tiếp
            Map<String, Object> strategy = getMarketingStrategyForCluster(clusterId);
            if (strategy == null) {
                throw new RuntimeException("Không tìm thấy marketing strategy cho cluster ID: " + clusterId);
            }

            // Tiêu đề chính
            Paragraph title = new Paragraph("BÁO CÁO CHIẾN LƯỢC MARKETING")
                    .setFont(boldFont)
                    .setFontSize(20)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(title);

            // Thông tin cluster
            document.add(createClusterInfoSection(strategy, font, boldFont));
            document.add(new Paragraph("").setMarginBottom(15));

            // Thống kê RFM
            document.add(createRFMStatsSection(strategy, font, boldFont));
            document.add(new Paragraph("").setMarginBottom(15));

            // Mục tiêu marketing
            document.add(createObjectivesSection(strategy, font, boldFont));
            document.add(new Paragraph("").setMarginBottom(15));

            // Chiến thuật marketing
            document.add(createTacticsSection(strategy, font, boldFont));
            document.add(new Paragraph("").setMarginBottom(15));

            // Tổng kết
            document.add(createSummarySection(strategy, font, boldFont));

        } finally {
            document.close();
        }

        return outputStream.toByteArray();
    }
    
    private Paragraph createClusterInfoSection(Map<String, Object> strategy, PdfFont font, PdfFont boldFont) {
        Div div = new Div();
        
        Paragraph header = new Paragraph("THÔNG TIN CỤM KHÁCH HÀNG")
                .setFont(boldFont)
                .setFontSize(14)
                .setMarginBottom(10);
        div.add(header);
        
        // Thông tin cơ bản
        div.add(createInfoRow("Tên cụm:", (String) strategy.get("clusterName"), font, boldFont));
        div.add(createInfoRow("Mô tả:", (String) strategy.get("clusterDescription"), font, boldFont));
        div.add(createInfoRow("Số lượng khách hàng:", String.valueOf(strategy.get("customerCount")), font, boldFont));
        div.add(createInfoRow("Ngày tạo báo cáo:", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), font, boldFont));
        
        return new Paragraph().add(div);
    }
    
    private Paragraph createRFMStatsSection(Map<String, Object> strategy, PdfFont font, PdfFont boldFont) {
        Div div = new Div();
        
        Paragraph header = new Paragraph("THỐNG KÊ RFM")
                .setFont(boldFont)
                .setFontSize(14)
                .setMarginBottom(10);
        div.add(header);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> characteristics = (Map<String, Object>) strategy.get("characteristics");
        if (characteristics != null) {
            div.add(createInfoRow("Recency trung bình:", characteristics.get("recencyAvg") + " ngày", font, boldFont));
            div.add(createInfoRow("Frequency trung bình:", characteristics.get("frequencyAvg") + " đơn", font, boldFont));
            div.add(createInfoRow("Monetary trung bình:", formatCurrency((BigDecimal) characteristics.get("monetaryAvg")), font, boldFont));
        }
        
        return new Paragraph().add(div);
    }
    
    private Paragraph createObjectivesSection(Map<String, Object> strategy, PdfFont font, PdfFont boldFont) {
        Div div = new Div();
        
        Paragraph header = new Paragraph("MỤC TIÊU MARKETING")
                .setFont(boldFont)
                .setFontSize(14)
                .setMarginBottom(10);
        div.add(header);
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> objectives = (List<Map<String, Object>>) strategy.get("marketingObjectives");
        if (objectives != null && !objectives.isEmpty()) {
            for (int i = 0; i < objectives.size(); i++) {
                Map<String, Object> objective = objectives.get(i);
                div.add(createObjectiveItem(objective, i + 1, font, boldFont));
            }
        } else {
            div.add(new Paragraph("Chưa có mục tiêu marketing nào được thiết lập.")
                    .setFont(font)
                    .setItalic());
        }
        
        return new Paragraph().add(div);
    }
    
    private Paragraph createTacticsSection(Map<String, Object> strategy, PdfFont font, PdfFont boldFont) {
        Div div = new Div();
        
        Paragraph header = new Paragraph("CHIẾN THUẬT MARKETING")
                .setFont(boldFont)
                .setFontSize(14)
                .setMarginBottom(10);
        div.add(header);
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> tactics = (List<Map<String, Object>>) strategy.get("marketingTactics");
        if (tactics != null && !tactics.isEmpty()) {
            for (int i = 0; i < tactics.size(); i++) {
                Map<String, Object> tactic = tactics.get(i);
                div.add(createTacticItem(tactic, i + 1, font, boldFont));
            }
        } else {
            div.add(new Paragraph("Chưa có chiến thuật marketing nào được thiết lập.")
                    .setFont(font)
                    .setItalic());
        }
        
        return new Paragraph().add(div);
    }
    
    private Paragraph createSummarySection(Map<String, Object> strategy, PdfFont font, PdfFont boldFont) {
        Div div = new Div();
        
        Paragraph header = new Paragraph("TỔNG KẾT")
                .setFont(boldFont)
                .setFontSize(14)
                .setMarginBottom(10);
        div.add(header);
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> objectives = (List<Map<String, Object>>) strategy.get("marketingObjectives");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> tactics = (List<Map<String, Object>>) strategy.get("marketingTactics");
        
        int totalObjectives = objectives != null ? objectives.size() : 0;
        int totalTactics = tactics != null ? tactics.size() : 0;
        int activeObjectives = objectives != null ? (int) objectives.stream().filter(obj -> Boolean.TRUE.equals(obj.get("isActive"))).count() : 0;
        int activeTactics = tactics != null ? (int) tactics.stream().filter(tac -> Boolean.TRUE.equals(tac.get("isActive"))).count() : 0;
        
        div.add(createInfoRow("Tổng số mục tiêu:", String.valueOf(totalObjectives), font, boldFont));
        div.add(createInfoRow("Mục tiêu đang hoạt động:", String.valueOf(activeObjectives), font, boldFont));
        div.add(createInfoRow("Tổng số chiến thuật:", String.valueOf(totalTactics), font, boldFont));
        div.add(createInfoRow("Chiến thuật đang hoạt động:", String.valueOf(activeTactics), font, boldFont));
        
        return new Paragraph().add(div);
    }
    
    private Paragraph createInfoRow(String label, String value, PdfFont font, PdfFont boldFont) {
        return new Paragraph()
                .add(new Text(label).setFont(boldFont))
                .add(new Text(" " + value).setFont(font))
                .setMarginBottom(5);
    }
    
    private Paragraph createObjectiveItem(Map<String, Object> objective, int index, PdfFont font, PdfFont boldFont) {
        Div div = new Div();
        
        Paragraph title = new Paragraph(index + ". " + objective.get("primaryObjective"))
                .setFont(boldFont)
                .setMarginBottom(5);
        div.add(title);
        
        if (objective.get("secondaryObjective") != null) {
            div.add(new Paragraph("   Mục tiêu phụ: " + objective.get("secondaryObjective"))
                    .setFont(font)
                    .setMarginBottom(3));
        }
        
        if (objective.get("kpi") != null) {
            div.add(new Paragraph("   KPI: " + objective.get("kpi"))
                    .setFont(font)
                    .setMarginBottom(3));
        }
        
        div.add(new Paragraph("   Ưu tiên: " + objective.get("priority") + " | Trạng thái: " + objective.get("status"))
                .setFont(font)
                .setMarginBottom(10));
        
        return new Paragraph().add(div);
    }
    
    private Paragraph createTacticItem(Map<String, Object> tactic, int index, PdfFont font, PdfFont boldFont) {
        Div div = new Div();
        
        Paragraph title = new Paragraph(index + ". " + tactic.get("title"))
                .setFont(boldFont)
                .setMarginBottom(5);
        div.add(title);
        
        if (tactic.get("description") != null) {
            div.add(new Paragraph("   Mô tả: " + tactic.get("description"))
                    .setFont(font)
                    .setMarginBottom(3));
        }
        
        div.add(new Paragraph("   Danh mục: " + tactic.get("category") + " | Ưu tiên: " + tactic.get("priority"))
                .setFont(font)
                .setMarginBottom(3));
        
        if (tactic.get("estimatedImpact") != null) {
            div.add(new Paragraph("   Tác động: " + tactic.get("estimatedImpact") + " | Chi phí: " + tactic.get("estimatedCost"))
                    .setFont(font)
                    .setMarginBottom(3));
        }
        
        div.add(new Paragraph("   Thời gian: " + tactic.get("timeToImplement") + " | Trạng thái: " + tactic.get("status"))
                .setFont(font)
                .setMarginBottom(10));
        
        return new Paragraph().add(div);
    }
    
    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "0 VNĐ";
        return String.format("%,.0f VNĐ", amount);
    }
    
    // ========== EXCEL EXPORT ==========
    
    public byte[] exportMarketingReportToExcel(Integer clusterId) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            // Lấy dữ liệu marketing strategy trực tiếp
            Map<String, Object> strategy = getMarketingStrategyForCluster(clusterId);
            if (strategy == null) {
                throw new RuntimeException("Không tìm thấy marketing strategy cho cluster ID: " + clusterId);
            }
            
            // Tạo style
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle boldStyle = createBoldStyle(workbook);
            
            // Sheet 1: Tổng quan
            Sheet overviewSheet = workbook.createSheet("Tổng quan");
            createOverviewSheet(overviewSheet, strategy, headerStyle, titleStyle, dataStyle, boldStyle);
            
            // Sheet 2: Mục tiêu Marketing
            Sheet objectivesSheet = workbook.createSheet("Mục tiêu Marketing");
            createObjectivesSheet(objectivesSheet, strategy, headerStyle, titleStyle, dataStyle, boldStyle);
            
            // Sheet 3: Chiến thuật Marketing
            Sheet tacticsSheet = workbook.createSheet("Chiến thuật Marketing");
            createTacticsSheet(tacticsSheet, strategy, headerStyle, titleStyle, dataStyle, boldStyle);
            
            // Tự động điều chỉnh độ rộng cột
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                for (int j = 0; j < sheet.getRow(0).getLastCellNum(); j++) {
                    sheet.autoSizeColumn(j);
                }
            }
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
    
    private void createOverviewSheet(Sheet sheet, Map<String, Object> strategy, CellStyle headerStyle, 
                                   CellStyle titleStyle, CellStyle dataStyle, CellStyle boldStyle) {
        int rowNum = 0;
        
        // Tiêu đề chính
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("BÁO CÁO CHIẾN LƯỢC MARKETING");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 3));
        
        rowNum++; // Dòng trống
        
        // Thông tin cluster
        Row clusterRow = sheet.createRow(rowNum++);
        clusterRow.createCell(0).setCellValue("THÔNG TIN CỤM KHÁCH HÀNG");
        clusterRow.getCell(0).setCellStyle(headerStyle);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowNum-1, rowNum-1, 0, 3));
        
        createInfoRow(sheet, rowNum++, "Tên cụm:", (String) strategy.get("clusterName"), boldStyle, dataStyle);
        createInfoRow(sheet, rowNum++, "Mô tả:", (String) strategy.get("clusterDescription"), boldStyle, dataStyle);
        createInfoRow(sheet, rowNum++, "Số lượng khách hàng:", String.valueOf(strategy.get("customerCount")), boldStyle, dataStyle);
        createInfoRow(sheet, rowNum++, "Ngày tạo báo cáo:", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), boldStyle, dataStyle);
        
        rowNum++; // Dòng trống
        
        // Thống kê RFM
        Row rfmRow = sheet.createRow(rowNum++);
        rfmRow.createCell(0).setCellValue("THỐNG KÊ RFM");
        rfmRow.getCell(0).setCellStyle(headerStyle);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowNum-1, rowNum-1, 0, 3));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> characteristics = (Map<String, Object>) strategy.get("characteristics");
        if (characteristics != null) {
            createInfoRow(sheet, rowNum++, "Recency trung bình:", characteristics.get("recencyAvg") + " ngày", boldStyle, dataStyle);
            createInfoRow(sheet, rowNum++, "Frequency trung bình:", characteristics.get("frequencyAvg") + " đơn", boldStyle, dataStyle);
            createInfoRow(sheet, rowNum++, "Monetary trung bình:", formatCurrency((BigDecimal) characteristics.get("monetaryAvg")), boldStyle, dataStyle);
        }
    }
    
    private void createObjectivesSheet(Sheet sheet, Map<String, Object> strategy, CellStyle headerStyle, 
                                     CellStyle titleStyle, CellStyle dataStyle, CellStyle boldStyle) {
        int rowNum = 0;
        
        // Tiêu đề
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("MỤC TIÊU MARKETING");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 6));
        
        rowNum++; // Dòng trống
        
        // Header
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"STT", "Mục tiêu chính", "Mục tiêu phụ", "KPI", "Ưu tiên", "Trạng thái", "Ghi chú"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Dữ liệu
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> objectives = (List<Map<String, Object>>) strategy.get("marketingObjectives");
        if (objectives != null && !objectives.isEmpty()) {
            for (int i = 0; i < objectives.size(); i++) {
                Map<String, Object> objective = objectives.get(i);
                Row dataRow = sheet.createRow(rowNum++);
                
                dataRow.createCell(0).setCellValue(i + 1);
                dataRow.createCell(1).setCellValue((String) objective.get("primaryObjective"));
                dataRow.createCell(2).setCellValue((String) objective.get("secondaryObjective"));
                dataRow.createCell(3).setCellValue((String) objective.get("kpi"));
                dataRow.createCell(4).setCellValue((String) objective.get("priority"));
                dataRow.createCell(5).setCellValue((String) objective.get("status"));
                dataRow.createCell(6).setCellValue((String) objective.get("notes"));
                
                for (int j = 0; j < 7; j++) {
                    dataRow.getCell(j).setCellStyle(dataStyle);
                }
            }
        }
    }
    
    private void createTacticsSheet(Sheet sheet, Map<String, Object> strategy, CellStyle headerStyle, 
                                  CellStyle titleStyle, CellStyle dataStyle, CellStyle boldStyle) {
        int rowNum = 0;
        
        // Tiêu đề
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("CHIẾN THUẬT MARKETING");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 8));
        
        rowNum++; // Dòng trống
        
        // Header
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"STT", "Tiêu đề", "Mô tả", "Danh mục", "Ưu tiên", "Tác động", "Chi phí", "Thời gian", "Trạng thái"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Dữ liệu
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> tactics = (List<Map<String, Object>>) strategy.get("marketingTactics");
        if (tactics != null && !tactics.isEmpty()) {
            for (int i = 0; i < tactics.size(); i++) {
                Map<String, Object> tactic = tactics.get(i);
                Row dataRow = sheet.createRow(rowNum++);
                
                dataRow.createCell(0).setCellValue(i + 1);
                dataRow.createCell(1).setCellValue((String) tactic.get("title"));
                dataRow.createCell(2).setCellValue((String) tactic.get("description"));
                dataRow.createCell(3).setCellValue((String) tactic.get("category"));
                dataRow.createCell(4).setCellValue((String) tactic.get("priority"));
                dataRow.createCell(5).setCellValue((String) tactic.get("estimatedImpact"));
                dataRow.createCell(6).setCellValue((String) tactic.get("estimatedCost"));
                dataRow.createCell(7).setCellValue((String) tactic.get("timeToImplement"));
                dataRow.createCell(8).setCellValue((String) tactic.get("status"));
                
                for (int j = 0; j < 9; j++) {
                    dataRow.getCell(j).setCellStyle(dataStyle);
                }
            }
        }
    }
    
    private void createInfoRow(Sheet sheet, int rowNum, String label, String value, CellStyle labelStyle, CellStyle valueStyle) {
        Row row = sheet.createRow(rowNum);
        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(labelStyle);
        
        Cell valueCell = row.createCell(1);
        valueCell.setCellValue(value);
        valueCell.setCellStyle(valueStyle);
    }
    
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }
    
    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }
    
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.TOP);
        style.setWrapText(true);
        return style;
    }
    
    private CellStyle createBoldStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        return style;
    }
    
    // ========== HELPER METHODS ==========
    
    // Lấy marketing strategy cho cluster (tương tự như trong MarketingService)
    private Map<String, Object> getMarketingStrategyForCluster(Integer clusterId) {
        List<CustomerCluster> clusters = segmentationService.getCustomerSegments();
        CustomerCluster cluster = clusters.stream()
            .filter(c -> c.getId().equals(clusterId))
            .findFirst()
            .orElse(null);
        
        if (cluster == null) {
            return null;
        }
        
        return createMarketingStrategy(cluster);
    }
    
    // Tạo marketing strategy cho cluster
    private Map<String, Object> createMarketingStrategy(CustomerCluster cluster) {
        Map<String, Object> strategy = new HashMap<>();
        
        strategy.put("clusterId", cluster.getId());
        strategy.put("clusterName", cluster.getClusterName());
        strategy.put("clusterDescription", cluster.getClusterDescription());
        strategy.put("customerCount", cluster.getCustomerCount());
        
        // Đặc trưng cluster
        Map<String, Object> characteristics = new HashMap<>();
        characteristics.put("recencyAvg", cluster.getRecencyAvg());
        characteristics.put("frequencyAvg", cluster.getFrequencyAvg());
        characteristics.put("monetaryAvg", cluster.getMonetaryAvg());
        strategy.put("characteristics", characteristics);
        
        // Chiến thuật marketing từ database
        List<Map<String, Object>> marketingTactics = getMarketingTacticsFromDatabase(cluster.getId());
        strategy.put("marketingTactics", marketingTactics);
        
        // Marketing Objectives từ database
        List<Map<String, Object>> marketingObjectives = getMarketingObjectivesFromDatabase(cluster.getId());
        strategy.put("marketingObjectives", marketingObjectives);
        
        return strategy;
    }
    
    // Lấy chiến thuật marketing từ database
    private List<Map<String, Object>> getMarketingTacticsFromDatabase(Integer clusterId) {
        List<MarketingTactic> tactics = marketingTacticRepository.findByClusterIdOrderByPriorityAscCreatedDateDesc(clusterId);
        
        return tactics.stream()
                .map(this::convertTacticToMap)
                .collect(Collectors.toList());
    }
    
    // Lấy mục tiêu marketing từ database
    private List<Map<String, Object>> getMarketingObjectivesFromDatabase(Integer clusterId) {
        List<MarketingObjective> objectives = marketingObjectiveRepository.findByClusterIdOrderByPriorityAscCreatedDateDesc(clusterId);
        
        return objectives.stream()
                .map(this::convertObjectiveToMap)
                .collect(Collectors.toList());
    }
    
    // Chuyển đổi MarketingTactic thành Map
    private Map<String, Object> convertTacticToMap(MarketingTactic tactic) {
        Map<String, Object> tacticMap = new HashMap<>();
        tacticMap.put("id", tactic.getId());
        tacticMap.put("name", tactic.getName());
        tacticMap.put("title", tactic.getTitle());
        tacticMap.put("description", tactic.getDescription());
        tacticMap.put("priority", tactic.getPriority());
        tacticMap.put("category", tactic.getCategory());
        tacticMap.put("estimatedImpact", tactic.getEstimatedImpact());
        tacticMap.put("estimatedCost", tactic.getEstimatedCost());
        tacticMap.put("timeToImplement", tactic.getTimeToImplement());
        tacticMap.put("budgetRequired", tactic.getBudgetRequired());
        tacticMap.put("expectedROI", tactic.getExpectedROI());
        tacticMap.put("status", tactic.getStatus());
        tacticMap.put("isActive", tactic.getIsActive());
        tacticMap.put("createdDate", tactic.getCreatedDate());
        tacticMap.put("updatedDate", tactic.getUpdatedDate());
        tacticMap.put("createdBy", tactic.getCreatedBy());
        tacticMap.put("notes", tactic.getNotes());
        return tacticMap;
    }
    
    // Chuyển đổi MarketingObjective thành Map
    private Map<String, Object> convertObjectiveToMap(MarketingObjective objective) {
        Map<String, Object> objectiveMap = new HashMap<>();
        objectiveMap.put("id", objective.getId());
        objectiveMap.put("clusterId", objective.getClusterId());
        objectiveMap.put("primaryObjective", objective.getPrimaryObjective());
        objectiveMap.put("secondaryObjective", objective.getSecondaryObjective());
        objectiveMap.put("kpi", objective.getKpi());
        objectiveMap.put("description", objective.getDescription());
        objectiveMap.put("targetValue", objective.getTargetValue());
        objectiveMap.put("measurementPeriod", objective.getMeasurementPeriod());
        objectiveMap.put("priority", objective.getPriority());
        objectiveMap.put("status", objective.getStatus());
        objectiveMap.put("isActive", objective.getIsActive());
        objectiveMap.put("createdDate", objective.getCreatedDate());
        objectiveMap.put("updatedDate", objective.getUpdatedDate());
        objectiveMap.put("createdBy", objective.getCreatedBy());
        objectiveMap.put("notes", objective.getNotes());
        return objectiveMap;
    }
}
