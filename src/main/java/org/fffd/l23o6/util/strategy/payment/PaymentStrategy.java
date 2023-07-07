package org.fffd.l23o6.util.strategy.payment;

import org.springframework.data.util.Pair;

public abstract class PaymentStrategy {

    // TODO: implement this by adding necessary methods and implement specified strategy
    public abstract Pair<String, Long> pay(double price, Long orderId, Long credit);
    public abstract Pair<String, Long> refund(double price, Long orderId, Long credit);

    public Long priceToCredit(double price) {
        return Math.round(price * 10);
    }

    public double discountByCredit(Long credit) {
        return 0;
    }
}
