package com.campus.market.item;

import com.campus.market.common.BusinessException;
import com.campus.market.common.ErrorCode;
import com.campus.market.common.UserContext;
import com.campus.market.item.dto.CreateItemRequest;
import com.campus.market.item.entity.Item;
import com.campus.market.item.enums.Category;
import com.campus.market.item.enums.ItemCondition;
import com.campus.market.item.enums.ItemStatus;
import com.campus.market.item.enums.TradeMethod;
import com.campus.market.item.mapper.ItemMapper;
import com.campus.market.user.entity.User;
import com.campus.market.user.mapper.UserMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ItemServiceTest {

    @Mock
    private ItemMapper itemMapper;
    @Mock
    private UserMapper userMapper;
    private ItemService itemService;

    private final ObjectMapper om = new ObjectMapper();

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        itemService = new ItemService(itemMapper, userMapper, om);
    }

    @AfterEach
    void clearContext() {
        UserContext.clear();
    }

    @Test
    void createSetsOnSaleAndStoresImagesJson() {
        UserContext.set(10L);
        CreateItemRequest req = new CreateItemRequest(
                "标题足够长的字符串abcde",
                "描述足够长的字符串abcdefghijklmnopqrst",
                Category.DIGITAL,
                ItemCondition.NEARLY_NEW,
                new BigDecimal("99.99"),
                TradeMethod.BOTH,
                List.of("http://x/1.jpg", "http://x/2.jpg"));

        // mock insert 回填 id 与 images;selectById 回填完整 item
        lenient().when(itemMapper.insert(any(Item.class))).thenAnswer(inv -> {
            Item arg = inv.getArgument(0);
            arg.setId(99L);
            return 1;
        });
        lenient().when(itemMapper.selectById(99L)).thenAnswer(inv -> {
            Item it = new Item();
            it.setId(99L);
            it.setSellerId(10L);
            it.setTitle(req.title());
            it.setStatus(ItemStatus.ON_SALE);
            it.setImages("[\"http://x/1.jpg\",\"http://x/2.jpg\"]");
            return it;
        });
        lenient().when(userMapper.selectById(10L)).thenReturn(null);

        var vo = itemService.create(10L, req);
        assertThat(vo.status()).isEqualTo(ItemStatus.ON_SALE);
        assertThat(vo.images()).hasSize(2);
        verify(itemMapper).insert(any(Item.class));
    }

    @Test
    void offShelfForbiddenForNonSeller() {
        UserContext.set(99L);
        Item it = new Item();
        it.setId(1L);
        it.setSellerId(10L);
        it.setStatus(ItemStatus.ON_SALE);
        when(itemMapper.selectById(1L)).thenReturn(it);

        assertThatThrownBy(() -> itemService.offShelf(99L, 1L))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo(ErrorCode.FORBIDDEN.getCode());
        verify(itemMapper, never()).updateById(any(Item.class));
    }

    @Test
    void offShelfRejectedWhenNotOnSale() {
        UserContext.set(10L);
        Item it = new Item();
        it.setId(1L);
        it.setSellerId(10L);
        it.setStatus(ItemStatus.RESERVED);
        when(itemMapper.selectById(1L)).thenReturn(it);

        assertThatThrownBy(() -> itemService.offShelf(10L, 1L))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo(ErrorCode.ORDER_STATUS_INVALID.getCode());
    }

    @Test
    void deleteRejectedWhenSold() {
        UserContext.set(10L);
        Item it = new Item();
        it.setId(1L);
        it.setSellerId(10L);
        it.setStatus(ItemStatus.SOLD);
        when(itemMapper.selectById(1L)).thenReturn(it);

        assertThatThrownBy(() -> itemService.delete(10L, 1L))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo(ErrorCode.ORDER_STATUS_INVALID.getCode());
        verify(itemMapper, never()).deleteById(anyLong());
    }
}