package io.chainboard.rpcs.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.chainboard.entity.AssetDO;
import io.chainboard.entity.BlockDO;
import io.chainboard.entity.BlocksDO;
import io.chainboard.entity.Constant;
import io.chainboard.rpcs.BaseRpc;
import io.chainboard.util.HttpRequestHandler;
import io.chainboard.util.TRONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.utils.Numeric;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TRONRpc implements BaseRpc{

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public BlocksDO getBlockByBlockNumber(Long blockNumber, Map<String, AssetDO> assetMap, String url){
        url = url + "/wallet/getblockbynum";
        JSONObject params = new JSONObject();
        params.put("num", blockNumber);
        JSONObject block = JSONObject.parseObject(makeRequest(url, params));
        JSONArray transactions = block.getJSONArray("transactions");

        BlocksDO blocksDO = new BlocksDO();
        blocksDO.setBlockHeader(block.getJSONObject("block_header"));
        List<BlockDO> blockDOList = new ArrayList<>();
        if (null != transactions) {
            for (int i = 0; i < transactions.size(); i++) {
                JSONObject transaction = transactions.getJSONObject(i);
                addBlockDO(blockNumber, transaction, blockDOList, assetMap);
            }
        }
        blocksDO.setBlockDOList(blockDOList);
        return blocksDO;
    }

    private void addBlockDO(Long blockNumber, JSONObject transaction, List<BlockDO> blockDOList, Map<String, AssetDO> assetMap){
        BlockDO blockDO = new BlockDO();
        blockDO.setBlockChain(Constant.BlockChain.TRX);
        String contractRet = transaction.getJSONArray("ret").getJSONObject(0).getString("contractRet");
        if ("SUCCESS".equals(contractRet) || null == contractRet) {
            JSONObject rawData = transaction.getJSONObject("raw_data");
            if (rawData == null)
                return;
            JSONArray contract = rawData.getJSONArray("contract");
            if (contract == null || contract.size() == 0)
                return;

            String type = contract.getJSONObject(0).getString("type");
            JSONObject parameter = contract.getJSONObject(0).getJSONObject("parameter");
            if (parameter == null)
                return;

            JSONObject value = parameter.getJSONObject("value");
            if (value == null)
                return;

            String fromAddress = value.getString("owner_address");
            blockDO.setFrom(fromAddress);
            String toAddress = null; //value.getString("to_address") != null ? value.getString("to_address") : value.getString("contract_address");
            String valueString = null;
            //String txHash = transaction.getString("txID");

            if ("TransferContract".equals(type)){
                toAddress = value.getString("to_address");
                valueString = value.getString("amount");
                blockDO.setAsset("TRX");
            } else if ("TransferAssetContract".equals(type)) {
                String assetId = value.getString("asset_name");
                assetId = TRONUtil.decodeHex(assetId);
                AssetDO assetDO = assetMap.get(assetId);
                if (assetDO == null || StringUtils.isEmpty(assetDO.getAsset()))
                    return;
                blockDO.setAsset(assetDO.getAsset());
                blockDO.setContractAddressAssetId(assetId);
                toAddress = value.getString("to_address");
                valueString = value.getString("amount");
            } else if ("TriggerSmartContract".equals(type)) {
                if (!"SUCCESS".equals(contractRet))
                    return;
                String contractAddress = TRONUtil.hexStringToBase58(value.getString("contract_address"));
                AssetDO assetDO = assetMap.get(contractAddress);
                if (assetDO == null || StringUtils.isEmpty(assetDO.getAsset()))
                    return;
                blockDO.setAsset(assetDO.getAsset());
                blockDO.setContractAddressAssetId(contractAddress);
                String data = value.getString("data");
                if (StringUtils.isEmpty(data))
                    return;
                if (!data.startsWith("a9059cbb"))
                    return;
                toAddress = TRONUtil.getStandardHexTronAddress(data.substring(8,72));//Numeric.toBigInt(data.substring(8,72)).toString(16);
                valueString = Numeric.toBigInt(data.substring(72, 136)).toString();
            } else
                return;

            blockDO.setFrom(TRONUtil.hexStringToBase58(fromAddress));
            blockDO.setTo(StringUtils.isEmpty(toAddress) ? "null" : TRONUtil.hexStringToBase58(toAddress));
            blockDO.setBlockNumber(blockNumber);

            valueString = StringUtils.isEmpty(valueString) ? "0" : valueString;
            blockDO.setAmount(new BigDecimal(valueString));
            blockDO.setTxHash(transaction.getString("txID"));
            blockDOList.add(blockDO);
        }
    }

    @Override
    public BigDecimal getBalance(String address, String contractAddressAssetId, String url){
        if (StringUtils.isEmpty(contractAddressAssetId))
            return getBalance(address, url);
        if (contractAddressAssetId.startsWith("T") || contractAddressAssetId.startsWith("41"))
            return getTRC20Balance(address, contractAddressAssetId, url);
        else {
            return getTrc10Balance(address, contractAddressAssetId, url);
        }
    }


    private JSONObject account(String address, String url){
        address = TRONUtil.base58ToHexString(address);
        url = url + "/wallet/getaccount";
        JSONObject params = new JSONObject();
        params.put("address", address);
        return JSONObject.parseObject(makeRequest(url, params));
    }

    private BigDecimal getBalance(String address, String url) {
        JSONObject account = account(address, url);
        String balance = account.getString("balance");
        if (balance == null)
            return BigDecimal.ZERO;
        return BaseRpc.fromWei(new BigDecimal(balance), 6);
    }

    private BigDecimal getTrc10Balance(String address, String assetId, String url){
        JSONObject account = account(address, url);
        JSONArray assetV2 = account.getJSONArray("assetV2");
        if (assetV2 == null || assetV2.size() == 0)
            return BigDecimal.ZERO;
        BigDecimal trc10Balance = BigDecimal.ZERO;
        for (int i=0;i<assetV2.size();i++){
            JSONObject trc10 = assetV2.getJSONObject(i);
            if (assetId.equals(trc10.getString("key"))){
                return trc10.getBigDecimal("value");
            }
        }
        return trc10Balance;
    }

    private BigDecimal getTRC20Balance(String address, String contractAddress, String url) {
        String tronResult = "";
            if (address.startsWith("T")) {
                address = TRONUtil.base58ToHexString(address);
            }

            if (contractAddress.startsWith("T")) {
                contractAddress = TRONUtil.base58ToHexString(contractAddress);
            }

            String functionSelector = "balanceOf(address)";
            String parameter = encodeFunction(address).substring(8);
            JSONObject data = new JSONObject();
            data.put("contract_address", contractAddress);
            data.put("function_selector", functionSelector);
            data.put("parameter", parameter);
            data.put("owner_address", address);
            url = url + "/wallet/triggersmartcontract";
            tronResult = makeRequest(url, data);
            JSONObject smartTransaction = JSONObject.parseObject(tronResult);
            if (!smartTransaction.getJSONObject("result").getBoolean("result")) {
                throw new RuntimeException("调用合约失败");
            }
            JSONArray constantResult = smartTransaction.getJSONArray("constant_result");
            BigInteger balance = Numeric.toBigInt(constantResult.getString(0));
            return new BigDecimal(balance);
    }

    private String encodeFunction(String address){
        address = address.replaceFirst("0x", "");
        address = address.replaceFirst("41", "0x");
        String methodName = "balanceOf";
        List<Type> inputParameters = new ArrayList<>();
        List<TypeReference<?>> outputParameters = new ArrayList<>();
        Address tAddress = new Address(address);
        //Uint256 tokenValue = new Uint256(amount.multiply(BigDecimal.TEN.pow(decimal)).toBigInteger());
        inputParameters.add(tAddress);
        //inputParameters.add(tokenValue);
        TypeReference<Bool> typeReference = new TypeReference<Bool>() {};
        outputParameters.add(typeReference);
        Function function = new Function(methodName, inputParameters, outputParameters);
        return FunctionEncoder.encode(function).replace("0x","").replaceAll("^(0+)", "");
    }

    @Override
    public String getTransaction(String txid, String fullUrl) {
        try {
            String url = fullUrl + "/wallet/gettransactioninfobyid";
            JSONObject params = new JSONObject();
            params.put("value", txid);
            JSONObject transactionInfo = JSONObject.parseObject(makeRequest(url, params));
            if (transactionInfo.isEmpty())
                transactionInfo.put("blockNumber", -9999);
            return transactionInfo.toJSONString();
        } catch (Exception e) {
            JSONObject transactionInfo = new JSONObject();
            transactionInfo.put("blockNumber", -9999);
            return transactionInfo.toJSONString();
        }
    }

    @Override
    public Long bestBlockNumber(String fullUrl) {
        String url = fullUrl + "/wallet/getnowblock";
        JSONObject params = new JSONObject();
        JSONObject nowBlock = JSONObject.parseObject(makeRequest(url, params));
        return nowBlock.getJSONObject("block_header").getJSONObject("raw_data").getLong("number");
    }

    public Long getBlockNumberByTxid(String txid, String url) {
        JSONObject transactionInfo = JSONObject.parseObject(getTransactionInfo(txid, url));
        return transactionInfo.getLong("blockNumber");
    }

    private String getTransactionInfo(String txid, String fullUrl) {
        String url = fullUrl + "/wallet/gettransactioninfobyid";
        JSONObject params = new JSONObject();
        params.put("value", txid);
        return makeRequest(url, params);
    }

    private String getRawTransaction(String txid, String fullUrl) {
        String url = fullUrl + "/wallet/gettransactionbyid";
        JSONObject params = new JSONObject();
        params.put("value", txid);
        return makeRequest(url, params);
    }

    private String makeRequest(String url, JSONObject data) {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add("Content-Type", "application/json");
        requestHeaders.add("Accept", "application/json");
        return HttpRequestHandler.postWithHeader(url, data, requestHeaders);
    }

    public Constant.TransactionStatusEnum checkTransactionStatus(String txid, String url) {
        try {
            String transactionString = getRawTransaction(txid, url);
            JSONObject transaction= JSONObject.parseObject(transactionString);
            String contractRet = transaction.getJSONArray("ret").getJSONObject(0).getString("contractRet");
            if ("SUCCESS".equals(contractRet) || null == contractRet) {
                JSONObject rawData = transaction.getJSONObject("raw_data");
                if (rawData == null)
                    return Constant.TransactionStatusEnum.FAIL;
                JSONArray contract = rawData.getJSONArray("contract");
                if (contract == null || contract.size() == 0)
                    return Constant.TransactionStatusEnum.FAIL;

                String type = contract.getJSONObject(0).getString("type");
                switch (type) {
                    case "TransferContract":
                    case "TransferAssetContract":
                        return Constant.TransactionStatusEnum.SUCCESS;
                    case "TriggerSmartContract":
                        if ("SUCCESS".equals(contractRet))
                            return Constant.TransactionStatusEnum.SUCCESS;
                        else
                            return Constant.TransactionStatusEnum.FAIL;
                    default:
                        return Constant.TransactionStatusEnum.FAIL;
                }
            } else {
                return Constant.TransactionStatusEnum.FAIL;
            }
        } catch (Exception e) {
            logger.warn("checkTransactionStatus exception: {}, txid:{}", e.toString(), txid);
            return Constant.TransactionStatusEnum.UN_KNOW;
        }
    }
}
