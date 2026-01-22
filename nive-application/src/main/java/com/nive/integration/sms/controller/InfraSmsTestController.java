package com.nive.integration.sms.controller;

import com.nive.integration.sms.service.InfraSmsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author nive
 * @class InfraMailTestController
 * @desc [클래스 설명]
 * @since 2025-05-22
 */
@RestController
@RequestMapping("/api/test/v1/sms")
@Profile({"dev", "test"})
@RequiredArgsConstructor
public class InfraSmsTestController {
    private final InfraSmsService infraSmsService;

    @GetMapping
    public void test(){
        infraSmsService.sendSmsTest();
    }
}
