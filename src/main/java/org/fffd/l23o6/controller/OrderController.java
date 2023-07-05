package org.fffd.l23o6.controller;

import io.github.lyc8503.spring.starter.incantation.exception.BizException;
import io.github.lyc8503.spring.starter.incantation.exception.CommonErrorType;
import io.github.lyc8503.spring.starter.incantation.pojo.CommonResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.io.PrintWriter;
import java.util.List;

import org.fffd.l23o6.pojo.vo.order.CreateOrderRequest;
import org.fffd.l23o6.pojo.vo.order.OrderIdVO;
import org.fffd.l23o6.pojo.vo.order.OrderVO;
import org.fffd.l23o6.pojo.vo.order.PatchOrderRequest;
import org.fffd.l23o6.service.OrderService;
import org.springframework.web.bind.annotation.*;

import cn.dev33.satoken.stp.StpUtil;

@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RestController
@RequestMapping("/v1/")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    private String str;

    @PostMapping("order")
    public CommonResponse<OrderIdVO> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        StpUtil.checkLogin();
        return CommonResponse.success(new OrderIdVO(orderService.createOrder(StpUtil.getLoginIdAsString(), request.getTrainId(), request.getStartStationId(), request.getEndStationId(), request.getSeatType(), request.getPaymentType())));
    }

    @GetMapping("order")
    public CommonResponse<List<OrderVO>> listOrders(){
        StpUtil.checkLogin();
        return CommonResponse.success(orderService.listOrders(StpUtil.getLoginIdAsString()));
    }

    @GetMapping("order/{orderId}")
    public CommonResponse<OrderVO> getOrder(@PathVariable("orderId") Long orderId) {
        return CommonResponse.success(orderService.getOrder(orderId));
    }

    @PatchMapping("order/{orderId}")
    public CommonResponse<?> patchOrder(@PathVariable("orderId") Long orderId,
                                        @Valid @RequestBody PatchOrderRequest request) {
        System.out.println("into here******\n");
        String resp = null;
        switch (request.getStatus()) {
            case PAID:
                resp = orderService.payOrder(orderId);
                break;
            case CANCELLED:
                orderService.cancelOrder(orderId);
                break;
            default:
                throw new BizException(CommonErrorType.ILLEGAL_ARGUMENTS, "Invalid order status.");
        }
        if (resp == null) {
            resp = "refund success";
        }
        str = resp;
        System.err.println(resp);
        return CommonResponse.success(resp);
    }

    @RequestMapping("alipay")
    public void alipay(HttpServletResponse response) {
        response.setContentType("text/html;charset=utf-8");
        try {
            PrintWriter writer = response.getWriter();
            writer.write(str);
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* only for test
    @RequestMapping("alipay_test")
    public void alipayTest(HttpServletResponse response) {
        response.setContentType("text/html;charset=utf-8");
        try {
            PrintWriter writer = response.getWriter();
            String str = orderService.payOrder(114514L);
//            System.out.println(str);
            writer.write(str);
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    */
}