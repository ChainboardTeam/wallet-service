package io.chainboard.entity;

import com.alibaba.fastjson.JSONObject;

import java.util.List;

public class BlocksDO {

    private JSONObject blockHeader;

    private List<BlockDO> blockDOList;

    public JSONObject getBlockHeader() {
        return blockHeader;
    }

    public void setBlockHeader(JSONObject blockHeader) {
        this.blockHeader = blockHeader;
    }

    public List<BlockDO> getBlockDOList() {
        return blockDOList;
    }

    public void setBlockDOList(List<BlockDO> blockDOList) {
        this.blockDOList = blockDOList;
    }
}
