package com.voting.constant;

/**
 * 信息提示常量类
 */
public class MessageConstant {

    public static final String PASSWORD_ERROR = "密码错误";
    public static final String ALREADY_EXISTS = "账号已存在";
    public static final String ACCOUNT_NOT_FOUND = "账号不存在";
    public static final String ACCOUNT_LOCKED = "账号被锁定";
    public static final String UNKNOWN_ERROR = "未知错误";
    public static final String USER_NOT_LOGIN = "用户未登录";
    public static final String CATEGORY_BE_RELATED_BY_SETMEAL = "当前分类关联了套餐,不能删除";
    public static final String CATEGORY_BE_RELATED_BY_DISH = "当前分类关联了菜品,不能删除";
    public static final String SHOPPING_CART_IS_NULL = "购物车数据为空，不能下单";
    public static final String ADDRESS_BOOK_IS_NULL = "用户地址为空，不能下单";
    public static final String LOGIN_FAILED = "登录失败";
    public static final String UPLOAD_FAILED = "文件上传失败";
    public static final String SETMEAL_ENABLE_FAILED = "套餐内包含未启售菜品，无法启售";
    public static final String PASSWORD_EDIT_FAILED = "密码修改失败";
    public static final String DISH_ON_SALE = "起售中的菜品不能删除";
    public static final String SETMEAL_ON_SALE = "起售中的套餐不能删除";
    public static final String DISH_BE_RELATED_BY_SETMEAL = "当前菜品关联了套餐,不能删除";
    public static final String ORDER_STATUS_ERROR = "订单状态错误";
    public static final String ORDER_NOT_FOUND = "订单不存在";
    
    // 合伙人相关
    public static final String PARTNER_USER_ALREADY_BOUND = "该用户已关联为合伙人";
    public static final String PARTNER_NOT_FOUND = "合伙人不存在";
    public static final String PARTNER_HAS_VOTE_RECORDS = "合伙人已参与投票，无法删除";
    public static final String USER_NOT_FOUND = "用户不存在";
    
    // 议案相关
    public static final String PROPOSAL_NOT_FOUND = "议案不存在";
    public static final String PROPOSAL_STATUS_NOT_ALLOW_UPDATE = "当前议案状态不允许修改";
    public static final String PROPOSAL_STATUS_NOT_ALLOW_DELETE = "只有草稿状态的议案可以删除";
    public static final String PROPOSAL_STATUS_NOT_ALLOW_PUBLISH = "只有待发布状态的议案可以发布";
    public static final String PROPOSAL_HAS_VOTE_TASK = "议案已关联投票任务，无法删除";

}
