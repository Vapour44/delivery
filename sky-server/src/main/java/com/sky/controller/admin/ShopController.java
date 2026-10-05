package com.sky.controller.admin;


import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Slf4j
public class ShopController {
    public static final String SHOP_STATUS = "shopStatus";
    @Autowired
    private RedisTemplate redisTemplate;
    @PutMapping("/{status}")
    public Result updateShop(@PathVariable Integer status) {
        log.info("更新店铺状态为：{}", status==1 ? "营业中" : "打烊中");
        redisTemplate.opsForValue().set(SHOP_STATUS, status);
        return Result.success();
    }

    @GetMapping("/status")
    public Result<Integer> getShopStatus() {
        Integer shopStatus = (Integer) redisTemplate.opsForValue().get(SHOP_STATUS);
        return Result.success(shopStatus);
    }
}
