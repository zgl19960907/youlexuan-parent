package com.youlexuan.pay.service;

import com.youlexuan.pojo.TbPayLog;

public interface PayLogService {
    /**
     * 根据用户查询payLog
     *   作用1:生成二维码是从支付日志中得到支付的ID以及支付的金额
     * @param userId
     * @return
     */
    public TbPayLog searchPayLogFromRedis(String userId);
}
