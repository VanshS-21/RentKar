package com.rentkar.dto;

public class RequestStatistics {
    
    private int pendingCount;
    private int approvedCount;
    private int rejectedCount;
    private int returnedCount;
    private int completedCount;
    private int totalSent;
    private int totalReceived;
    
    public RequestStatistics() {}
    
    public RequestStatistics(int pendingCount, int approvedCount, int rejectedCount,
                           int returnedCount, int completedCount, int totalSent, int totalReceived) {
        this.pendingCount = pendingCount;
        this.approvedCount = approvedCount;
        this.rejectedCount = rejectedCount;
        this.returnedCount = returnedCount;
        this.completedCount = completedCount;
        this.totalSent = totalSent;
        this.totalReceived = totalReceived;
    }
    
    public int getPendingCount() {
        return pendingCount;
    }
    
    public void setPendingCount(int pendingCount) {
        this.pendingCount = pendingCount;
    }
    
    public int getApprovedCount() {
        return approvedCount;
    }
    
    public void setApprovedCount(int approvedCount) {
        this.approvedCount = approvedCount;
    }
    
    public int getRejectedCount() {
        return rejectedCount;
    }
    
    public void setRejectedCount(int rejectedCount) {
        this.rejectedCount = rejectedCount;
    }
    
    public int getReturnedCount() {
        return returnedCount;
    }
    
    public void setReturnedCount(int returnedCount) {
        this.returnedCount = returnedCount;
    }
    
    public int getCompletedCount() {
        return completedCount;
    }
    
    public void setCompletedCount(int completedCount) {
        this.completedCount = completedCount;
    }
    
    public int getTotalSent() {
        return totalSent;
    }
    
    public void setTotalSent(int totalSent) {
        this.totalSent = totalSent;
    }
    
    public int getTotalReceived() {
        return totalReceived;
    }
    
    public void setTotalReceived(int totalReceived) {
        this.totalReceived = totalReceived;
    }
}
