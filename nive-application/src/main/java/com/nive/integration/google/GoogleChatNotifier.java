package com.nive.integration.google;


/**
 * GoogleChatNotifier 추상화 인터페이스.
 *
 * - @Async 적용 시 Spring이 JDK Dynamic Proxy를 사용하여
 *   실제 Bean 타입이 구현 클래스(InfraGoogleChatNotifier)가 아닌 Proxy 객체가 됨.
 * - 이로 인해 구체 클래스 타입(InfraGoogleChatNotifier)으로는 주입이 불가능하고,
 *   인터페이스 기반으로 주입해야 안정적으로 동작.
 *
 * 따라서, 비동기 프록시(@Async)로 인한 Bean 타입 불일치 문제를 해결하기 위해
 * 별도의 인터페이스를 두고 구현체를 주입받도록 설계.
 */
public interface GoogleChatNotifier {
    void sendApiEvent(String apiEvent);
}
