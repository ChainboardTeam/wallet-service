package io.chainboard.entity;

public class Constant {

    public enum AssetType{

        BASE("BASE")
        ,TRC10("TRC10")
        ,TRC20("TRC20")
        ,ERC20("ERC20")
        ;

        private String type;

        AssetType(String type) {this.type = type;}
    }

    public enum BlockChain{
        BTC("BTC")
        ,ETH("ETH")
        ,TRX("TRX")
        ;
        private String blockChain;
        BlockChain(String blockChain){this.blockChain = blockChain;}
    }

    public enum TransactionStatusEnum {

        SUCCESS(1), //成功
        UN_KNOW(0), //失败
        FAIL(-1); //位置

        private int status;

        TransactionStatusEnum(int status){
            this.status = status;
        }
    }
}
