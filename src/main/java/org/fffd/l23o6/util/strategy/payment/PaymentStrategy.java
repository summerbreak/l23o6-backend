package org.fffd.l23o6.util.strategy.payment;

import io.github.lyc8503.spring.starter.incantation.exception.BizException;
import org.fffd.l23o6.exception.BizError;

public abstract class PaymentStrategy {

    // TODO: implement this by adding necessary methods and implement specified strategy

    /**
     * 订单支付操作
     * @param price 订单金额
     * @param orderId 订单id
     * @return 支付成功/失败消息
     */
    public abstract String pay(double price, Long orderId);

    /**
     * 订单退款操作
     * @param price 订单金额
     * @param orderId 订单id
     */
    public abstract void refund(double price, Long orderId);

    /**
     * 积分换算公式:1元=10积分
     * @param price 订单金额
     * @return 对应的积分数
     */
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
