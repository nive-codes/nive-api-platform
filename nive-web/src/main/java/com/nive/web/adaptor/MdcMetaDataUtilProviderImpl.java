package com.nive.web.adaptor;

import com.nive.application.port.MdcMetaDataUtilProvider;
import com.nive.web.filter.mdc.support.MdcMetaDataUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author hosikchoi
 * @class MdcMetaDataUtilProviderImpl
 * @desc MdcMetaDatUtil을 application 모듈에서도 쓸 수 있기 위한 adaptor class
 * @since 2026-01-28
 */
@RequiredArgsConstructor
@Component
public class MdcMetaDataUtilProviderImpl implements MdcMetaDataUtilProvider {
    @Override
    public Map<String, Object> getMeta() {
        return MdcMetaDataUtil.traceInfo();
    }
}
