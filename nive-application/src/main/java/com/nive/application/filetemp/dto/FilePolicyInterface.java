package com.nive.application.filetemp.dto;

/**
 * @author nive
 * @class FilePolicyInterface
 * @desc [클래스 설명]
 * @since 2025-06-02
 */
public interface FilePolicyInterface {
    boolean isAllowed(String extension);
}
