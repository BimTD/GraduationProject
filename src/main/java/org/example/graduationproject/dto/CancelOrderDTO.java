package org.example.graduationproject.dto;

public class CancelOrderDTO {
    private Integer orderId;

    public CancelOrderDTO() {}

    public CancelOrderDTO(Integer orderId) {
        this.orderId = orderId;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }
}


