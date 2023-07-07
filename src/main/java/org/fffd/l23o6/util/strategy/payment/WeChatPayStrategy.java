package org.fffd.l23o6.util.strategy.payment;

import io.github.lyc8503.spring.starter.incantation.exception.BizException;
import org.fffd.l23o6.exception.BizError;
import org.springframework.data.util.Pair;

public class WeChatPayStrategy extends PaymentStrategy {
    public static final WeChatPayStrategy INSTANCE = new WeChatPayStrategy();

    private double money = 10000;

    private WeChatPayStrategy() {
    }

    @Override
    public Pair<String, Long> pay(double price, Long orderId, Long credit) {
        if (money < price) {
            throw new BizException(BizError.MONEY_NOT_ENOUGH, String.format("only %.2f left", money));
        }
        money -= price;
        credit += priceToCredit(price);
        return Pair.of("success", credit);
    }

    @Override
    public Pair<String, Long> refund(double price, Long orderId, Long credit) {
        money += price;
        return Pair.of("success", credit);
    }
}
