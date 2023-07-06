package org.fffd.l23o6.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.fffd.l23o6.dao.OrderDao;
import org.fffd.l23o6.dao.RouteDao;
import org.fffd.l23o6.dao.TrainDao;
import org.fffd.l23o6.dao.UserDao;
import org.fffd.l23o6.pojo.entity.UserEntity;
import org.fffd.l23o6.pojo.enum_.OrderStatus;
import org.fffd.l23o6.exception.BizError;
import org.fffd.l23o6.pojo.entity.OrderEntity;
import org.fffd.l23o6.pojo.entity.RouteEntity;
import org.fffd.l23o6.pojo.entity.TrainEntity;
import org.fffd.l23o6.pojo.vo.order.OrderVO;
import org.fffd.l23o6.service.OrderService;
import org.fffd.l23o6.util.strategy.payment.AlipayStrategy;
import org.fffd.l23o6.util.strategy.payment.CreditStrategy;
import org.fffd.l23o6.util.strategy.payment.PaymentStrategy;
import org.fffd.l23o6.util.strategy.train.GSeriesSeatStrategy;
import org.fffd.l23o6.util.strategy.train.KSeriesSeatStrategy;
import org.springframework.stereotype.Service;

import io.github.lyc8503.spring.starter.incantation.exception.CommonErrorType;
import io.github.lyc8503.spring.starter.incantation.exception.BizException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderDao orderDao;
    private final UserDao userDao;
    private final TrainDao trainDao;
    private final RouteDao routeDao;


    public Long createOrder(String username, Long trainId, Long fromStationId, Long toStationId, String seatType,
            String paymentType) {
        Long userId = userDao.findByUsername(username).getId();
        TrainEntity train = trainDao.findById(trainId).get();
        RouteEntity route = routeDao.findById(train.getRouteId()).get();
        int startStationIndex = route.getStationIds().indexOf(fromStationId);
        int endStationIndex = route.getStationIds().indexOf(toStationId);
        String seat = null;
        double[][] priceTable = train.getSeatPrices();
        int typeIndex;
        switch (train.getTrainType()) {
            case HIGH_SPEED:
                seat = GSeriesSeatStrategy.INSTANCE.allocSeat(startStationIndex, endStationIndex,
                        GSeriesSeatStrategy.GSeriesSeatType.fromString(seatType), train.getSeats());
                typeIndex = Objects.requireNonNull(GSeriesSeatStrategy.GSeriesSeatType.fromString(seatType)).ordinal();
                break;
            case NORMAL_SPEED:
                seat = KSeriesSeatStrategy.INSTANCE.allocSeat(startStationIndex, endStationIndex,
                        KSeriesSeatStrategy.KSeriesSeatType.fromString(seatType), train.getSeats());
                typeIndex = Objects.requireNonNull(KSeriesSeatStrategy.KSeriesSeatType.fromString(seatType)).ordinal();
                break;
            default:
                throw new BizException(CommonErrorType.ILLEGAL_ARGUMENTS);
        }
        if (seat == null) {
            throw new BizException(BizError.OUT_OF_SEAT);
        }
        double price = 0;
        for (int i = startStationIndex; i < endStationIndex; i++) {
            price += priceTable[i][typeIndex];
        }
        OrderEntity order = OrderEntity.builder().trainId(trainId).userId(userId).seat(seat).paymentType(paymentType)
                .status(OrderStatus.PENDING_PAYMENT).arrivalStationId(toStationId).departureStationId(fromStationId)
                .price(price).build();
        train.setUpdatedAt(null);// force it to update
        trainDao.save(train);
        orderDao.save(order);
        return order.getId();
    }

    public void checkOrders() {
        // check if an order by Alipay is paid successfully
        List<OrderEntity> alipayOrders = orderDao.findAll().stream()
                .filter((OrderEntity order) -> order.getPaymentType().equals("alipay") &&
                        order.getStatus().equals(OrderStatus.PENDING_PAYMENT))
                .toList();
        for (OrderEntity order: alipayOrders) {
            if (AlipayStrategy.INSTANCE.isTradeSuccess(order.getId())) {
                order.setStatus(OrderStatus.PAID);
            }
        }
        orderDao.saveAll(alipayOrders);
        // check if a paid order is completed via date
        List<OrderEntity> paidOrders = orderDao.findAll().stream()
                .filter((OrderEntity order) -> order.getStatus().equals(OrderStatus.PAID)).toList();
        for (OrderEntity order: paidOrders) {
            TrainEntity train = trainDao.findById(order.getTrainId()).get();
            if (train.getArrivalTimes().get(0).before(new Date())) {
                order.setStatus(OrderStatus.COMPLETED);
            }
        }
        orderDao.saveAll(paidOrders);
    }

    public List<OrderVO> listOrders(String username) {
        Long userId = userDao.findByUsername(username).getId();
        List<OrderEntity> orders = orderDao.findByUserId(userId);
        orders.sort((o1,o2)-> o2.getId().compareTo(o1.getId()));
        return orders.stream().map(order -> {
            TrainEntity train = trainDao.findById(order.getTrainId()).get();
            RouteEntity route = routeDao.findById(train.getRouteId()).get();
            int startIndex = route.getStationIds().indexOf(order.getDepartureStationId());
            int endIndex = route.getStationIds().indexOf(order.getArrivalStationId());
            return OrderVO.builder().id(order.getId()).trainId(order.getTrainId())
                    .seat(order.getSeat()).status(order.getStatus().getText())
                    .createdAt(order.getCreatedAt())
                    .startStationId(order.getDepartureStationId())
                    .endStationId(order.getArrivalStationId())
                    .departureTime(train.getDepartureTimes().get(startIndex))
                    .arrivalTime(train.getArrivalTimes().get(endIndex))
                    .price(order.getPrice())
                    .paymentType(order.getPaymentType())
                    .build();
        }).collect(Collectors.toList());
    }

    public OrderVO getOrder(Long id) {
        OrderEntity order = orderDao.findById(id).get();
        TrainEntity train = trainDao.findById(order.getTrainId()).get();
        RouteEntity route = routeDao.findById(train.getRouteId()).get();
        int startIndex = route.getStationIds().indexOf(order.getDepartureStationId());
        int endIndex = route.getStationIds().indexOf(order.getArrivalStationId());
        return OrderVO.builder().id(order.getId()).trainId(order.getTrainId())
                .seat(order.getSeat()).status(order.getStatus().getText())
                .createdAt(order.getCreatedAt())
                .startStationId(order.getDepartureStationId())
                .endStationId(order.getArrivalStationId())
                .departureTime(train.getDepartureTimes().get(startIndex))
                .arrivalTime(train.getArrivalTimes().get(endIndex))
                .price(order.getPrice())
                .paymentType(order.getPaymentType())
                .build();
    }

    public void cancelOrder(Long id) {
        OrderEntity order = orderDao.findById(id).get();
        UserEntity user = userDao.findById(order.getUserId()).get();

        if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new BizException(BizError.ILLEAGAL_ORDER_STATUS);
        }

        // TODO: refund user's money and credits if needed
        if (order.getStatus() == OrderStatus.PAID) {
            PaymentStrategy paymentStrategy = CreditStrategy.INSTANCE;
            double price = order.getPrice();
            long creditChange = Math.round(price * 100);
            if (order.getPaymentType().equals("credit")) {
                creditChange = -creditChange;
            } else {
                paymentStrategy = AlipayStrategy.INSTANCE;
                creditChange = Math.round(price * 5);
            }
            paymentStrategy.refund(price, order.getId());
            user.setCredit(user.getCredit() - creditChange);
        }

        order.setStatus(OrderStatus.CANCELLED);
        userDao.save(user);
        orderDao.save(order);
    }

    public String payOrder(Long id) {
        if (id == 114514L) {
            return AlipayStrategy.INSTANCE.pay(123, 114514L);
        }
        OrderEntity order = orderDao.findById(id).get();
        UserEntity user = userDao.findById(order.getUserId()).get();

        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new BizException(BizError.ILLEAGAL_ORDER_STATUS);
        }

        // TODO: use payment strategy to pay!
        // TODO: update user's credits, so that user can get discount next time
        PaymentStrategy paymentStrategy = CreditStrategy.INSTANCE;
        double price = order.getPrice();
        long creditChange = Math.round(price * 100);
        if (order.getPaymentType().equals("credit")) {
            if (user.getCredit() < creditChange) {
                throw new BizException(BizError.CREDIT_NOT_ENOUGH);
            }
            creditChange = -creditChange;
        } else if (order.getPaymentType().equals("alipay")) {
            paymentStrategy = AlipayStrategy.INSTANCE;
            creditChange = Math.round(price * 5);
        } else {
            throw new BizException(CommonErrorType.ILLEGAL_ARGUMENTS);
        }
        String response = paymentStrategy.pay(price, order.getId());
        user.setCredit(user.getCredit() + creditChange);

        order.setStatus(OrderStatus.PAID);
        userDao.save(user);
        orderDao.save(order);
        return response;
    }

}
