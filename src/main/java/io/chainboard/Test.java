package io.chainboard;

import com.alibaba.fastjson.JSONObject;
import io.chainboard.entity.AssetDO;
import io.chainboard.rpcs.BaseRpc;
import io.chainboard.rpcs.impl.TRONRpc;

public class Test {

    public static void main(String[] args) throws Exception{
        String url = "https://api.trongrid.io";
        BaseRpc rpc = new TRONRpc();
        String address = "TMuA6YqfCeX8EhbfYEg5y7S4DqzSJireY9";
        String contractAddress = "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t";
        System.err.println("trx余额: " + rpc.getBalance(address, null, url));
        System.err.println("btt余额：" + rpc.getBalance(address, "1002000", url));
        System.err.println("usdt余额：" + rpc.getBalance(address, contractAddress, url));

//        System.err.println(JSONObject.toJSONString(rpc.getBlockByBlockNumber(14492423L, AssetDO.initAssetsMap, url)));
    }
}
