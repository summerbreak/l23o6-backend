package org.fffd.l23o6.util.strategy.payment;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import io.github.lyc8503.spring.starter.incantation.exception.BizException;
import org.fffd.l23o6.exception.BizError;

/**
 * 使用沙盒环境的支付宝支付
 */
public class AlipayStrategy extends PaymentStrategy {
    public static final AlipayStrategy INSTANCE = new AlipayStrategy();
    // 应用ID,您的APPID，收款账号既是您的APPID对应支付宝账号
    private static final String app_id = "9021000122696964";

    // 商户私钥，您的PKCS8格式RSA2私钥
    private static final String merchant_private_key = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCTx8+4OfbJgHlRiF0Lp5AAXRYOvNzzHxwkuHP3MF6+uXL2PGuTf3MhYILfRwRqNm1HfzPTUQBZ01LlhgDk8+vY0c64n4TKbSPcSt+BBvTn7iDs7ppLUO/QOwWhxFY8B/EM1Bx1V6Wy471s0OIMBSk0QyJXpOisMayK4Z4zs/6GQADdNaRyimls0n7OchuXmBMmlal1L+fhn+na6C1O0O6MLP4squgkFdopxgnAl+k9cp8uwgtUseaarLL+Jc3QM67pnmJeRfMvupHVe8KNYHvQWQ12xJuJfWKSfdj5C7zToAMQUQty/zwAQFEAsLkP4xaBmRD3gOnJxqHE8UysS9VnAgMBAAECggEAVSVLCVpcVr0vKUroUU66KXt4ugUXSP0jugSMzF+SN1Giaz1kvhcwvopc3UiNwqSZUhh4Q8jn6tlXaedLnJ7txH8eHEHMwNUhVEvPewgHE5qgXeMcA0ke41seY5Y8GVp6CGot+24Eio2tFoVDuKlAlFwZ7WWWqZ/1Sg+0lxWeELrvar8fP+Y9Eo9qIn1lgrm83S4WZdHyapJacK4uyomK551EP463LfeDwDwVwP8ue9aIZ1iteYFEq32AFB44udoZRJshGImA2aFeI2R/XZ7oxQ4H8BIppsRYfBTwosFJ5U+4pkRHWadUO/8oDFgSyJimSj0Xy2faQKMBzMEPXk/gAQKBgQDLN0h/28yLBoJFl/xaRLPJ4LS8vJx5FiWpBPou7cv22rS2VdC0CNw5R0vhQgSEobK1/FsLY3SJ/IQUkGbugwZwnTJBUxkEXR13u5nw4JaGifT0Of3CDpaGZeHCAJ3Ez2uAsx2URly+u/WIKk6CPUoutl3ekgmPbrA+R3sEqK03xwKBgQC6KmI6SlafGW3AQCvwLxmok0FVluRoT1mtSifsNGE+FdylzL5/MXij+le7K/4+dYhX2vbpsLSyOenXlXV01UsNfmY7mFZqPts0iWBIHWWvLGtjA1NCGPZ8R6MprzI41ufwpGGVWCRZA4KDVfLs1qRNhgBjzZqtY8i0TiXRx/a1YQKBgEUBOr8f9e7ngOj82RYV6i8M/JJyQXfki1k/dWzmFrJ2RZL8xOe9cnqIuT1+W2ZmOfcOACR32yTX+Em7Vxh6qX8oAd6oxv/yVoVH0Ng4iySYR/N+laqFpZtxkR19dBVWkZycNdkZo3io/pEEPLCA+2WtS2a+lDx+S556S02jsV4VAoGACZ2Qz0Pxr2wH0CtAWbqFD8DzWfFeQMkAb8Ppxfh6oVNNvz6RBE7Q1V9j0qYWDrHRZgBISo+29ZGqM1lj5LHzm3HQ4/kdVYWDmvYRhAgIrYCU11tfnIVfjdHBarDC3k6zUimbZLRVxEc7IX95+aOoftyR/pWrDJOf1ThIGcrDQmECgYBpjCCfHTdPBy7u1JyZO3SBF5B2XyrHqAJNsA7qqmsmnpoKzr0f1zbc1Ptfl21KECVRj9uq6SJzUR1yoCYkUTxmCJKeL4Jl5SWHHm3Mdg/3oHVu0CifLWDY6UgBTLSL07yhoYk3cpYnOrYeSsPqCSqqh7LDbuu4mNnfz/0zwOYW3Q==";

    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    private static final String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAiR1xERPu4C/03a8cWnA2b4PDPAJxJYbm+Sg7zNh327DfhOo11rMCx+pdZOutfStdiHIrz5T0U6QEiDsWAzgXEY1e2qP6XRxWvYa06lb2/6seGdT0Z2q5/txVsukQsF2/jOXE70MQMD0nd5OiMtrdBJQgztuaj3WGVgobaeOUV+AmVcozagpMeut+AEl5rjaDKclRasg/ahfE4SbRewFkZyUYTJn0N107JJd/X/JguAu0mBevFlw2bnhTZa6l/KZJDhmJ58IF8+Rn4Pfafk1pkODYvGcWBJe3yqwAuuv5TwFK+LEVn56PAcJzKP4x5MTaEMzu/qVwTdR9eYhtYHofDQIDAQAB";

    // 服务器异步通知页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    private static final String notify_url = "";

    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    private static final String return_url = "http://localhost:5173/";

    // 签名方式
    private static final String sign_type = "RSA2";

    private static final String format = "json";

    // 字符编码格式
    private static final String charset = "utf-8";

    private static final String TRADE_SUCCESS = "TRADE_SUCCESS";

    // 支付宝网关
    private static final String gatewayUrl = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";
    private static final String paymentPageHTMLBefore = "<!DOCTYPE html>\n" +
            "<html lang=\"en\">\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <title>支付宝支付</title>\n" +
            "</head>\n" +
            "<body>";
    private static final String paymentPageHTMLAfter = "</body>\n" +
            "</html>";

    private AlipayStrategy() {
    }

    @Override
    public String pay(double price, Long orderId) {
        AlipayClient alipayClient = new DefaultAlipayClient(gatewayUrl, app_id, merchant_private_key, format, charset,
                alipay_public_key, sign_type);
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setNotifyUrl(notify_url);
        request.setReturnUrl(return_url);
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", orderId + "");
        bizContent.put("total_amount", price);
        bizContent.put("subject", "车票");
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");

        request.setBizContent(bizContent.toString());
        try {
            AlipayTradePagePayResponse response = alipayClient.pageExecute(request);
            if (response.isSuccess()) {
                System.out.println("调用成功");
                return paymentPageHTMLBefore + response.getBody() + paymentPageHTMLAfter;
            } else {
                System.out.println("调用失败");
                throw new BizException(BizError.PAYMENT_FAILED);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "failed";
    }

    @Override
    public void refund(double price, Long orderId) {
        AlipayClient alipayClient = new DefaultAlipayClient(gatewayUrl, app_id, merchant_private_key, format, charset,
                alipay_public_key, sign_type);
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", orderId + "");
        bizContent.put("refund_amount", price);
        bizContent.put("out_request_no", System.currentTimeMillis() + "");

        request.setBizContent(bizContent.toString());
        try {
            AlipayTradeRefundResponse response = alipayClient.execute(request);
            if (response.isSuccess()) {
                System.out.println("调用成功");
            } else {
                System.out.println("调用失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isTradeSuccess(Long orderId) {
        AlipayClient alipayClient = new DefaultAlipayClient(gatewayUrl, app_id, merchant_private_key, format, charset,
                alipay_public_key, sign_type);
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", orderId);
        request.setBizContent(bizContent.toString());
        try {
            AlipayTradeQueryResponse response = alipayClient.execute(request);
            if (response.isSuccess()) {
                System.out.println("调用成功");
                if (response.getTradeStatus().equals(TRADE_SUCCESS)) {
                    return true;
                }
            } else {
                System.out.println("调用失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
