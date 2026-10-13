package com.sky.service.impl;

import com.sky.service.ResponseService;
import com.sky.service.WorkspaceService;
import com.sky.vo.BusinessDataVO;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
public class ResponseServiceImpl implements ResponseService {

    private static final String TEMPLATE_PATH = "excel/运营数据报表模板.xlsx";
    private static final int REPORT_DAYS = 30;

    private final WorkspaceService workspaceService;
    private final Clock clock;

    @Autowired
    public ResponseServiceImpl(WorkspaceService workspaceService, Clock clock) {
        this.workspaceService = workspaceService;
        this.clock = clock;
    }

    @Override
    public void export(HttpServletResponse response) throws IOException {
        LocalDate endDate = LocalDate.now(clock).minusDays(1);
        LocalDate beginDate = endDate.minusDays(REPORT_DAYS - 1L);

        ClassPathResource template = new ClassPathResource(TEMPLATE_PATH);
        try (InputStream inputStream = template.getInputStream();
             XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
            XSSFSheet sheet = workbook.getSheetAt(0);
            fillOverview(sheet, beginDate, endDate);
            fillDetails(sheet, beginDate);

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            ContentDisposition disposition = ContentDisposition.attachment()
                    .filename("运营数据报表.xlsx", StandardCharsets.UTF_8)
                    .build();
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, disposition.toString());
            workbook.write(response.getOutputStream());
            response.getOutputStream().flush();
        }
    }

    private void fillOverview(XSSFSheet sheet, LocalDate beginDate, LocalDate endDate) {
        sheet.getRow(1).getCell(1).setCellValue("时间：" + beginDate + " 至 " + endDate);

        BusinessDataVO overview = workspaceService.getBusinessData(
                LocalDateTime.of(beginDate, LocalTime.MIN),
                LocalDateTime.of(endDate, LocalTime.MAX));
        sheet.getRow(3).getCell(2).setCellValue(overview.getTurnover());
        sheet.getRow(3).getCell(4).setCellValue(overview.getOrderCompletionRate());
        sheet.getRow(3).getCell(6).setCellValue(overview.getNewUsers());
        sheet.getRow(4).getCell(2).setCellValue(overview.getValidOrderCount());
        sheet.getRow(4).getCell(4).setCellValue(overview.getUnitPrice());
    }

    private void fillDetails(XSSFSheet sheet, LocalDate beginDate) {
        for (int index = 0; index < REPORT_DAYS; index++) {
            LocalDate date = beginDate.plusDays(index);
            BusinessDataVO dailyData = workspaceService.getBusinessData(
                    LocalDateTime.of(date, LocalTime.MIN),
                    LocalDateTime.of(date, LocalTime.MAX));

            int rowIndex = 7 + index;
            sheet.getRow(rowIndex).getCell(1).setCellValue(date.toString());
            sheet.getRow(rowIndex).getCell(2).setCellValue(dailyData.getTurnover());
            sheet.getRow(rowIndex).getCell(3).setCellValue(dailyData.getValidOrderCount());
            sheet.getRow(rowIndex).getCell(4).setCellValue(dailyData.getOrderCompletionRate());
            sheet.getRow(rowIndex).getCell(5).setCellValue(dailyData.getUnitPrice());
            sheet.getRow(rowIndex).getCell(6).setCellValue(dailyData.getNewUsers());
        }

    }
}
