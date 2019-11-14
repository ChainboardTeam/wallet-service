package io.chainboard.entity;

import javax.print.attribute.standard.NumberUp;
import java.util.HashMap;
import java.util.Map;

public class AssetDO {

    private Constant.AssetType type;

    private String asset;

    private Constant.BlockChain blockChain;

    private String contractAddressAssetId;

    private int decimals;

    public AssetDO(){}

    public AssetDO(Constant.AssetType type, String asset, Constant.BlockChain blockChain, String contractAddressAssetId, int decimals) {
        this.type = type;
        this.asset = asset;
        this.blockChain = blockChain;
        this.contractAddressAssetId = contractAddressAssetId;
        this.decimals = decimals;
    }

    public Constant.AssetType getType() {
        return type;
    }

    public void setType(Constant.AssetType type) {
        this.type = type;
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

    public String getContractAddressAssetId() {
        return contractAddressAssetId;
    }

    public void setContractAddressAssetId(String contractAddressAssetId) {
        this.contractAddressAssetId = contractAddressAssetId;
    }

    public int getDecimals() {
        return decimals;
    }

    public void setDecimals(int decimals) {
        this.decimals = decimals;
    }

    //key为合约地址或trc10的assetId，如果是该条链的主币则为链名
    public static Map<String, AssetDO> initAssetsMap;

    static {
        initAssetsMap = new HashMap<String, AssetDO>();

        //base trx
        AssetDO TRX = new AssetDO(Constant.AssetType.BASE, "TRX", Constant.BlockChain.TRX, null, 6);
        initAssetsMap.put(Constant.BlockChain.TRX.toString(), TRX);

        //trc10 btt
        String bttAssetId = "1002000";
        AssetDO BTT = new AssetDO(Constant.AssetType.TRC10, "BTT", Constant.BlockChain.TRX, bttAssetId, 6);
        initAssetsMap.put(bttAssetId, BTT);

        //trc20 TRON-usdt
        String tronUSDTContractAddress = "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t";
        AssetDO TRONUSDT = new AssetDO(Constant.AssetType.TRC20, "USDT", Constant.BlockChain.TRX, tronUSDTContractAddress, 6);
        initAssetsMap.put(tronUSDTContractAddress, TRONUSDT);
    }
}
