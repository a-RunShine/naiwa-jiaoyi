package com.campus.market.wallet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.market.wallet.entity.Wallet;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface WalletMapper extends BaseMapper<Wallet> {

    /**
     * 行锁查钱包(冻结/解冻/结算时用,DB CHECK 兜底并发)。
     */
    @Select("SELECT * FROM wallet WHERE user_id = #{uid} FOR UPDATE")
    Wallet selectByUserIdForUpdate(@Param("uid") Long uid);

    /**
     * 同时改 balance 与 frozen,SQL 单条 UPDATE 保证原子。
     * DB CHECK {@code balance >= 0 AND frozen <= balance} 触发时抛
     * {@link org.springframework.dao.DataIntegrityViolationException},
     * 由 GlobalExceptionHandler 转为 5001/3001。
     */
    @Update("UPDATE wallet SET balance = balance + #{delta}, frozen = frozen + #{frozenDelta} " +
            "WHERE user_id = #{uid}")
    int updateBalance(@Param("uid") Long uid,
                      @Param("delta") java.math.BigDecimal delta,
                      @Param("frozenDelta") java.math.BigDecimal frozenDelta);
}