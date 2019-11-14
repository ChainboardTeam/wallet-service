package io.chainboard.rpcs;

import com.alibaba.fastjson.JSONObject;
import io.chainboard.entity.AssetDO;
import io.chainboard.entity.BlocksDO;
import io.chainboard.entity.Constant;

import java.math.BigDecimal;
import java.util.Map;

public interface BaseRpc {

    /**
     * 获取该块高上的所有数据, assetMap为代币集合（key为合约地址或assetId）
     * @param blockNumber 区块高度
     * @param url 接口
     * @return 区块数据
     */
    BlocksDO getBlockByBlockNumber(Long blockNumber, Map<String, AssetDO> assetMap, String url);

    /**
     * 余额
     * @param address 地址
     * @param contractAddressAssetId 合约地址
     * @param url 接口
     * @return 余额
     */
    BigDecimal getBalance(String address, String contractAddressAssetId, String url);

    /**
     * txHash获取交易明细
     * @param txHash txHash
     * @param url 接口
     * @return 钱包获取交易
     */
    String getTransaction(String txHash, String url);

    /**
     * 获取节点当前最新区块数
     * @param url 接口
     * @return 块高
     */
    Long bestBlockNumber(String url);

    /**
     * 检查交易是否成功
     * @param txHash txHash
     * @param url url
     * @return 1，0，-1
     */
    Constant.TransactionStatusEnum checkTransactionStatus(String txHash, String url);

    /**
     * 精度转换
     * @param value
     * @param decimal
     * @return
     */
    static BigDecimal fromWei(BigDecimal value, int decimal) {
        if (decimal == 0)
            return value;
        return value.divide(BigDecimal.valueOf(Math.pow(10.0, decimal)), decimal, BigDecimal.ROUND_DOWN);
    }

    /**
     * 精度转换
     * @param value
     * @param decimal
     * @return
     */
    static BigDecimal toWei(BigDecimal value, int decimal) {
        if (decimal == 0)
            return value;
        return value.multiply(BigDecimal.valueOf(Math.pow(10.0, decimal)));
    }

}
