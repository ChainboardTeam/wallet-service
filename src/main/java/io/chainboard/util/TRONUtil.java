package io.chainboard.util;

import com.alibaba.fastjson.JSONObject;
import org.bitcoinj.core.Base58;
import org.bitcoinj.core.Sha256Hash;
import org.bouncycastle.util.encoders.Hex;
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
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.utils.Numeric;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TRONUtil {

    private static Logger logger = LoggerFactory.getLogger(TRONUtil.class);

    public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    public static String getStandardHexTronAddress(String hexString){
        if (hexString == null)
            throw new RuntimeException("tron地址格式不正确");
        hexString = Numeric.toBigInt(hexString).toString(16);
        if(hexString.length()>42)
            throw new RuntimeException("tron地址格式不正确");
        if (hexString.startsWith("41") && hexString.length() == 42) {
            //if (hexString.length() != 42)
            //    throw new RuntimeException("tron地址格式不正确");
            return hexString;
        } else {
            if (hexString.length() == 42){
                return "41" + hexString.substring(2);
            } else if (hexString.length() > 40)
                throw new RuntimeException("tron地址格式不正确");
            StringBuilder x0 = new StringBuilder();
            for (int i=0;i<40-hexString.length();i++){
                x0.append("0");
            }
            return "41" + x0.toString() + hexString;
        }
    }

    public static String decodeHex(String data) {
        try {
            return new String(org.apache.commons.codec.binary.Hex.decodeHex(data.toCharArray()));
        } catch (Exception e) {
            throw new RuntimeException("Hex.decodeHex exception: " + e.toString());
        }
    }

    public static String hexStringToBase58(String inputString) {
        byte[] input = fromHexString(inputString);
        byte[] hash0 = Sha256Hash.hash(input);
        byte[] hash1 = Sha256Hash.hash(hash0);
        byte[] inputCheck = new byte[input.length + 4];
        System.arraycopy(input, 0, inputCheck, 0, input.length);
        System.arraycopy(hash1, 0, inputCheck, input.length, 4);
        return  Base58.encode(inputCheck);
    }

    private static byte[] fromHexString(String data) {
        if (data == null) {
            return EMPTY_BYTE_ARRAY;
        }
        if (data.startsWith("0x")) {
            data = data.substring(2);
        }
        if (data.length() % 2 == 1) {
            data = "0" + data;
        }
        return Hex.decode(data);
    }

    public static String SHA256_HMAC(String message, String secret) {
        String hash = "";
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            byte[] bytes = sha256_HMAC.doFinal(message.getBytes());
            hash = byteArrayToHexString(bytes);
        } catch (Exception e) {
            logger.error("HMAC_SHA256加密失败" + e.getMessage());
        }
        return hash;
    }

    private static String byteArrayToHexString(byte[] b) {
        StringBuilder hs = new StringBuilder();
        String temp;
        for (int n = 0; b != null && n < b.length; n++) {
            temp = Integer.toHexString(b[n] & 0XFF);
            if (temp.length() == 1)
                hs.append('0');
            hs.append(temp);
        }
        return hs.toString().toLowerCase();
    }

    public static String encodeApproveFunction(String spender, BigDecimal amount, int decimal) {
        spender = spender.replaceFirst("0x", "");
        spender = spender.replaceFirst("41", "0x");
        String methodName = "approve";
        List<Type> inputParameters = new ArrayList();
        List<TypeReference<?>> outputParameters = new ArrayList();
        Address tAddress = new Address(spender);
        Uint256 tokenValue = new Uint256(amount.multiply(BigDecimal.TEN.pow(decimal)).toBigInteger());
        inputParameters.add(tAddress);
        inputParameters.add(tokenValue);
        TypeReference<Bool> typeReference = new TypeReference<Bool>() {
        };
        outputParameters.add(typeReference);
        Function function = new Function(methodName, inputParameters, outputParameters);
        return FunctionEncoder.encode(function).replace("0x", "").replaceAll("^(0+)", "");
    }

    public static String brocastTransaction(String fullUrl, String txId, JSONObject transactionSign){
        String url = fullUrl + "/wallet/broadcasttransaction";
        String broadCastTransaction = makeRequest(url, transactionSign);

        if ("true".equals(JSONObject.parseObject(broadCastTransaction).getString("result")))
            return txId;
        else {
            logger.warn("广播交易失败,broadCastTransaction:{}，transactionSign：{}", broadCastTransaction, transactionSign);
            return null;
        }
    }

    public static String base58ToHexString(String address) {
        byte[] decodeCheck = Base58.decode(address);
        if (decodeCheck.length <= 4) {
            return null;
        }
        byte[] decodeData = new byte[decodeCheck.length - 4];
        System.arraycopy(decodeCheck, 0, decodeData, 0, decodeData.length);
        byte[] hash0 = Sha256Hash.hash(decodeData);
        byte[] hash1 = Sha256Hash.hash(hash0);
        if (hash1[0] == decodeCheck[decodeData.length] &&
                hash1[1] == decodeCheck[decodeData.length + 1] &&
                hash1[2] == decodeCheck[decodeData.length + 2] &&
                hash1[3] == decodeCheck[decodeData.length + 3]) {
            return Hex.toHexString(decodeData);
        }
        return null;
    }


    public static String makeRequest(String url, JSONObject data) {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add("Content-Type", "application/json");
        requestHeaders.add("Accept", "application/json");
        return HttpRequestHandler.postWithHeader(url, data, requestHeaders);
    }

    public static void main(String[] args) {
        System.err.println(hexStringToBase58("41549eaeda1f3c11cc5b290ce3d206d3a788f118ff"));
    }

}
