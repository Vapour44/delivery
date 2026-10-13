package com.sky.controller.admin;


import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.service.ResponseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping("/admin/report")
public class ReportController {
    @Autowired
    private ReportService reportService;
    @Autowired
    private ResponseService responseService;
    @GetMapping("turnoverStatistics")
    public Result turnoverReport(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
                                 @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        return Result.success(reportService.turnoverReport(begin, end));
    }

    @GetMapping("/userStatistics")
    public Result userReport(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
                             @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        return Result.success(reportService.userReport(begin, end));
    }

    @GetMapping("top10")
    public Result top10(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
    @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        return Result.success(reportService.top10(begin, end));
    }

    @GetMapping("/ordersStatistics")
    public Result orderReport(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
                              @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        return Result.success(reportService.orderReport(begin, end));
    }
    @GetMapping("/export")
    public void export(HttpServletResponse response) throws IOException {
        responseService.export(response);
    }

}
