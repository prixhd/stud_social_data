package com.example.studdata.config;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

@Component
public class NetworkConfig implements ApplicationListener<ApplicationReadyEvent> {
    private static final Logger log = LoggerFactory.getLogger(NetworkConfig.class);

    @Value("${server.port:8081}")
    private String serverPort;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            String localIP = getLocalIP();
            log.info("🌐 Приложение запущено и доступно по адресам:");
            log.info("🏠 Локальный доступ: http://localhost:{}", serverPort);
            log.info("📱 Доступ из локальной сети: http://{}:{}", localIP, serverPort);
            log.info("📋 Логин: admin, Пароль: admin");
            log.info("=" .repeat(60));
        } catch (Exception e) {
            log.error("Ошибка при получении IP адреса: {}", e.getMessage());
        }
    }

    private String getLocalIP() throws Exception {
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();

            if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                continue;
            }

            Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress address = addresses.nextElement();

                if (address.isSiteLocalAddress() && !address.isLoopbackAddress()) {
                    return address.getHostAddress();
                }
            }
        }

        return InetAddress.getLocalHost().getHostAddress();
    }
}