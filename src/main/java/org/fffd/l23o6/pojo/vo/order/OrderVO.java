package org.fffd.l23o6.pojo.vo.order;

import java.util.Date;

import lombok.Builder;
import lombok.Data;
import org.fffd.l23o6.pojo.enum_.PaymentType;

@Builder
@Data
public class OrderVO {
    private Long id;
    private Long trainId;
    private Long startStationId;
    private Long endStationId;
    private Date departureTime;
    private Date arrivalTime;
    private String status;
    private Date createdAt;
    private String seat;
    private Double price;
    private PaymentType paymentType;
}
