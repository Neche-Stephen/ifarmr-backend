package org.ifarmr.payload.response;

public class AdminDashboardResponse {

    private long totalFarmers;
    private long activeFarmers;
    private long inactiveFarmers;

    public AdminDashboardResponse(long totalFarmers, long activeFarmers, long inactiveFarmers){ //long totalOrders, //long completedOrders) {
        this.totalFarmers = totalFarmers;
        this.activeFarmers = activeFarmers;
        this.inactiveFarmers = inactiveFarmers;

    }

    public long getTotalFarmers() {
        return totalFarmers;
    }

    public void setTotalFarmers(long totalFarmers) {
        this.totalFarmers = totalFarmers;
    }

    public long getActiveFarmers() {
        return activeFarmers;
    }

    public void setActiveFarmers(long activeFarmers) {
        this.activeFarmers = activeFarmers;
    }

    public long getInactiveFarmers() {
        return inactiveFarmers;
    }

    public void setInactiveFarmers(long inactiveFarmers) {
        this.inactiveFarmers = inactiveFarmers;
    }

}
