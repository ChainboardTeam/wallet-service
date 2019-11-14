package io.chainboard.entity;

import java.math.BigDecimal;

public class BlockDO {

    private Long blockNumber;

    private String from;

    private String to;

    private BigDecimal amount;

    private String asset;

    private String txHash;

    private Constant.BlockChain blockChain;

    private String contractAddressAssetId;

    public Long getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(Long blockNumber) {
        this.blockNumber = blockNumber;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public String getContractAddressAssetId() {
        return contractAddressAssetId;
    }

    public void setContractAddressAssetId(String contractAddressAssetId) {
        this.contractAddressAssetId = contractAddressAssetId;
    }

    public String getAsset() {
        return asset;
    }

    public void setAsset(String asset) {
        this.asset = asset;
    }

    public Constant.BlockChain getBlockChain() {
        return blockChain;
    }

    public void setBlockChain(Constant.BlockChain blockChain) {
        this.blockChain = blockChain;
    }
}
