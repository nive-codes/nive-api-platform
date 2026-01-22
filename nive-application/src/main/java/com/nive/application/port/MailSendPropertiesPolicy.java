package com.nive.application.port;

/**
 * @author hosikchoi
 * @class MailSendPropertiesPolicy
 * @desc nive-web의 properties를 참조하기 위한 interface
 * @since 2026-01-06
 */
public interface MailSendPropertiesPolicy {
    boolean isSendEnabled();
}
