package org.fffd.l23o6.util.strategy.payment;

import io.github.lyc8503.spring.starter.incantation.exception.BizException;
import org.fffd.l23o6.exception.BizError;
import org.springframework.data.util.Pair;

public abstract class PaymentStrategy {

    // TODO: implement this by adding necessary methods and implement specified strategy
    public abstract Pair<String, Long> pay(double price, Long orderId, Long credit);
    public abstract Pair<String, Long> refund(double price, Long orderId, Long credit);

    public static Long priceToCredit(double price) {
        return Math.round(price * 10);
    }

    /**
     * 表驱动计算折扣
     * @param credit 用户积分
     * @return 折扣金额
     */
    public static double discountByCredit(Long credit) {
        if (credit < 1000) {
            throw new BizException(BizError.CREDIT_NOT_ENOUGH);
        }
        long[] creditArr = new long[]{0, 1000, 3000, 10000, 50000, Long.MAX_VALUE};
        double[] discountArr = new double[]{0.001, 0.0015, 0.002, 0.0025, 0.003};
        double discount = 0;
        for (int i = 1; i < creditArr.length - 1; i++) {
            if (credit >= creditArr[i]) {
                discount += (creditArr[i] - creditArr[i - 1]) * discountArr[i - 1];
            }
            if (credit < creditArr[i + 1]) {
                discount += (credit - creditArr[i]) * discountArr[i];
                break;
            }
        }
        return discount;
    }
}
