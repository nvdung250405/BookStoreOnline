package com.bookstore.dto;

public class AuditLogStatsDTO {
    private long totalLogs;
    private long todayLogs;
    private long activeUsersCount;
    private long dataChangesCount;

    public AuditLogStatsDTO() {}

    public AuditLogStatsDTO(long totalLogs, long todayLogs, long activeUsersCount, long dataChangesCount) {
        this.totalLogs = totalLogs;
        this.todayLogs = todayLogs;
        this.activeUsersCount = activeUsersCount;
        this.dataChangesCount = dataChangesCount;
    }

    // Getters and Setters
    public long getTotalLogs() { return totalLogs; }
    public void setTotalLogs(long totalLogs) { this.totalLogs = totalLogs; }
    public long getTodayLogs() { return todayLogs; }
    public void setTodayLogs(long todayLogs) { this.todayLogs = todayLogs; }
    public long getActiveUsersCount() { return activeUsersCount; }
    public void setActiveUsersCount(long activeUsersCount) { this.activeUsersCount = activeUsersCount; }
    public long getDataChangesCount() { return dataChangesCount; }
    public void setDataChangesCount(long dataChangesCount) { this.dataChangesCount = dataChangesCount; }
}
