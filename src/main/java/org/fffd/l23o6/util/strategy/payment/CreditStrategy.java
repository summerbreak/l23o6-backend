package org.fffd.l23o6.util.strategy.payment;

import io.github.lyc8503.spring.starter.incantation.exception.BizException;
import org.fffd.l23o6.exception.BizError;
import org.fffd.l23o6.pojo.entity.UserEntity;

public class CreditStrategy extends PaymentStrategy {
    public static final CreditStrategy INSTANCE = new CreditStrategy();

    private CreditStrategy() {}
    @Override
    public String pay(double price, Long orderId) {
        return "success";
    }

    @Override
    public String refund(double price, Long orderId) {
        return "success";
    }
}
