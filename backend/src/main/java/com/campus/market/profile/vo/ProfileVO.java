package com.campus.market.profile.vo;

import com.campus.market.common.PageResult;
import com.campus.market.favorite.vo.FavoriteItemVO;
import com.campus.market.item.entity.Item;
import com.campus.market.item.vo.ItemDetailVO;
import com.campus.market.order.vo.OrderVO;
import com.campus.market.review.vo.ReviewVO;
import com.campus.market.user.dto.UserVO;
import com.campus.market.wallet.vo.WalletFlowVO;
import com.campus.market.wallet.vo.WalletVO;

/**
 * 个人中心聚合视图。
 */
public record ProfileVO(
        UserVO user,
        WalletVO wallet,
        PageResult<Item> myItems,
        PageResult<OrderVO> boughtOrders,
        PageResult<OrderVO> soldOrders,
        PageResult<FavoriteItemVO> favorites,
        PageResult<WalletFlowVO> walletFlows,
        PageResult<ReviewVO> receivedReviews
) {
}