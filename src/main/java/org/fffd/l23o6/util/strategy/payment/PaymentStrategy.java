package org.fffd.l23o6.util.strategy.payment;

import org.fffd.l23o6.pojo.entity.UserEntity;

public abstract class PaymentStrategy {

    // TODO: implement this by adding necessary methods and implement specified strategy
    public abstract String pay(double price, Long orderId);
    public abstract String refund(double price, Long orderId);
}
