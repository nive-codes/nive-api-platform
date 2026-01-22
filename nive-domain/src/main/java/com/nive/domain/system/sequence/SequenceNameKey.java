package com.nive.domain.system.sequence;

/**
 * @author nive
 * @class SequenceNameKey
 * @desc 시퀀스 생성 enum
 * @since 2025-06-25
 */
public enum SequenceNameKey {
    PRODUCT("P-", "PRODUCT");

    private final String strategyPrefix;
    private final String tableKey;

    SequenceNameKey(String strategyPrefix, String tableKey) {
        this.strategyPrefix = strategyPrefix;
        this.tableKey = tableKey;
    }

    public String getStrategyPrefix() {
        return strategyPrefix;
    }

    public String getTableKey() {
        return tableKey;
    }
}
