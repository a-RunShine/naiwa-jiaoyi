package com.campus.market.profile;

import com.campus.market.common.PageResult;
import com.campus.market.favorite.FavoriteService;
import com.campus.market.favorite.vo.FavoriteItemVO;
import com.campus.market.item.ItemService;
import com.campus.market.item.entity.Item;
import com.campus.market.order.OrderService;
import com.campus.market.order.dto.OrderQueryRequest;
import com.campus.market.order.vo.OrderVO;
import com.campus.market.profile.vo.ProfileVO;
import com.campus.market.review.ReviewService;
import com.campus.market.review.vo.ReviewVO;
import com.campus.market.user.UserService;
import com.campus.market.user.dto.UserVO;
import com.campus.market.wallet.WalletService;
import com.campus.market.wallet.vo.WalletFlowVO;
import com.campus.market.wallet.vo.WalletVO;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {

    private final UserService userService;
    private final ItemService itemService;
    private final OrderService orderService;
    private final WalletService walletService;
    private final FavoriteService favoriteService;
    private final ReviewService reviewService;

    public ProfileService(UserService userService, ItemService itemService,
                          OrderService orderService, WalletService walletService,
                          FavoriteService favoriteService, ReviewService reviewService) {
        this.userService = userService;
        this.itemService = itemService;
        this.orderService = orderService;
        this.walletService = walletService;
        this.favoriteService = favoriteService;
        this.reviewService = reviewService;
    }

    public ProfileVO me(Long uid) {
        UserVO user = userService.me(uid);
        WalletVO wallet = walletService.me(uid);
        PageResult<Item> myItems = itemService.listMine(uid, 1, 20);
        PageResult<OrderVO> bought = orderService.list(uid, orderQuery("buy"));
        PageResult<OrderVO> sold = orderService.list(uid, orderQuery("sell"));
        PageResult<FavoriteItemVO> favs = favoriteService.list(uid, 1, 20);
        PageResult<WalletFlowVO> flows = walletService.flows(uid, 1, 5);
        PageResult<ReviewVO> reviews = reviewService.receivedByUser(uid, 1, 20);
        return new ProfileVO(user, wallet, myItems, bought, sold, favs, flows, reviews);
    }

    private static OrderQueryRequest orderQuery(String role) {
        OrderQueryRequest q = new OrderQueryRequest();
        q.setRole(role);
        q.setPage(1);
        q.setSize(20);
        return q;
    }
}