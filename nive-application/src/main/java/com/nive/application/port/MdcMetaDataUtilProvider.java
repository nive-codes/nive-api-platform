package com.nive.application.port;

import java.util.Map;

/**
 * @author hosikchoi
 * @class MdcMetaDataUtilProvider
 * @desc MdcMetaData를 통해 traceId 및 시간을 가지고 오는 port interface
 * @since 2026-01-28
 */
public interface MdcMetaDataUtilProvider {
    public Map<String, Object> getMeta();
}
