package org.fffd.l23o6.util.strategy.payment;

import io.github.lyc8503.spring.starter.incantation.exception.BizException;
import org.fffd.l23o6.exception.BizError;

/**
 * 桩程序实现微信支付
 */
public class WeChatPayStrategy extends PaymentStrategy {
    public static final WeChatPayStrategy INSTANCE = new WeChatPayStrategy();

    private double money = 10000;

    private WeChatPayStrategy() {
    }

    @Override
    public String pay(double price, Long orderId) {
        if (money < price) {
            throw new BizException(BizError.MONEY_NOT_ENOUGH, String.format("only %.2f left", money));
        }
        money -= price;
        return "success";
    }

    @Override
    public void refund(double price, Long orderId) {
        money += price;
    }
}
