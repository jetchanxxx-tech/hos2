package com.huifu.starchain.common.exception;


public class BizError {

    private final int code;
    private final String message;

    public BizError(int code, String message) { this.code = code; this.message = message; }

    public int getCode() { return code; }
    public String getMessage() { return message; }

    private BizError(int code, String message) {
        this.code = code;
        this.message = message;
    }

    // ---- Common ----
    public static final BizError BAD_REQUEST          = new BizError(40000, "请求参数有误");
    public static final BizError UNAUTHORIZED         = new BizError(40100, "未登录或令牌已过期");
    public static final BizError FORBIDDEN            = new BizError(40300, "无操作权限");
    public static final BizError NOT_FOUND            = new BizError(40400, "资源不存在");
    public static final BizError CONFLICT             = new BizError(40900, "资源冲突");
    public static final BizError INTERNAL_ERROR       = new BizError(50000, "服务器内部错误");
    public static final BizError SERVICE_UNAVAILABLE  = new BizError(50300, "服务暂不可用");

    // ---- Business ----
    public static final BizError USER_NOT_FOUND       = new BizError(40101, "用户不存在");
    public static final BizError PASSWORD_WRONG       = new BizError(40102, "密码错误");
    public static final BizError ACCOUNT_SUSPENDED    = new BizError(40103, "账号已被停用");
    public static final BizError PHONE_ALREADY_EXISTS = new BizError(40901, "手机号已被注册");
    public static final BizError FAMILY_NOT_FOUND     = new BizError(40401, "家庭不存在");
    public static final BizError FAMILY_FULL          = new BizError(40902, "家庭成员已达上限");
    public static final BizError PACKAGE_NOT_FOUND    = new BizError(40402, "服务包不存在");
    public static final BizError PACKAGE_OFF_SHELF    = new BizError(40903, "服务包已下架");
    public static final BizError ORDER_NOT_FOUND      = new BizError(40403, "订单不存在");
    public static final BizError ORDER_EXPIRED        = new BizError(40904, "订单已过期");
    public static final BizError BENEFIT_EXHAUSTED    = new BizError(40905, "权益次数已用完");
    public static final BizError TASK_NOT_FOUND       = new BizError(40404, "随访任务不存在");
    public static final BizError TASK_ALREADY_DONE    = new BizError(40906, "随访任务已完成");
    public static final BizError DATA_AUTH_REVOKED    = new BizError(40301, "用户已撤销数据授权");
    public static final BizError GATEWAY_SYNC_FAILED  = new BizError(50301, "医院数据同步失败");
    public static final BizError EMERGENCY_ALERT      = new BizError(40907, "已触发紧急预警");
}
