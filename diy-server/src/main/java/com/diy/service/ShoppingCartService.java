package com.diy.service;

import com.diy.dto.ShoppingCartDTO;
import com.diy.entity.ShoppingCart;

import java.util.List;

public interface ShoppingCartService {
    void add(ShoppingCartDTO shoppingCartDTO);

    List<ShoppingCart> showShoppingCart();

    void delete();

    void subShoppingCart(ShoppingCartDTO shoppingCartDTO);
}
