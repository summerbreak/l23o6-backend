package org.fffd.l23o6.util.strategy.payment;

import org.springframework.data.util.Pair;

public class WeChatPayStrategy extends PaymentStrategy {
    public static final WeChatPayStrategy INSTANCE = new WeChatPayStrategy();

    private WeChatPayStrategy() {
    }

    @Override
    public Pair<String, Long> pay(double price, Long orderId, Long credit) {
        return null;
    }

    @Override
    public Pair<String, Long> refund(double price, Long orderId, Long credit) {
        return null;
    }
}
