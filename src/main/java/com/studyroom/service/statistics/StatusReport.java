package com.studyroom.service.statistics;

import java.util.Map;

/**
 * 统计报告接口 —— 体现 OOP 多态
 */
public interface StatusReport {
    Map<String, Object> generateReport();
    String getReportType();
}