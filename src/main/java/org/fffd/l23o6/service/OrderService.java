package org.fffd.l23o6.service;

import java.util.List;

import org.fffd.l23o6.pojo.enum_.PaymentType;
import org.fffd.l23o6.pojo.vo.order.OrderVO;

public interface OrderService {
    Long createOrder(String username, Long trainId, Long fromStationId, Long toStationId, String seatType,
                     PaymentType paymentType);
    void checkOrders();
    List<OrderVO> listOrders(String username);
    OrderVO getOrder(Long id);
    void cancelOrder(Long id);
    String payOrder(Long id);

}
