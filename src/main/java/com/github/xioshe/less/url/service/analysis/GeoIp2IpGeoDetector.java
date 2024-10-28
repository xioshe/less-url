package com.github.xioshe.less.url.service.analysis;

import com.github.xioshe.less.url.service.analysis.vo.IPLocationVO;
import com.google.common.net.InetAddresses;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
@RequiredArgsConstructor
public class GeoIp2IpGeoDetector implements IpGeoDetector {

    private static final String CHINA = "中国";
    private static final String ZH_CN = "zh-CN";

    private final DatabaseReader databaseReader;

    @Override
    public IPLocationVO detect(String ip) {
        log.debug("analysis ip location: {}", ip);
        CityResponse response;
        try {
            var inetAddress = InetAddress.getByName(ip);
            if (!isPublicAddress(inetAddress)) {
                log.debug("IP {} is not a public address", ip);
                return IPLocationVO.builder()
                        .ip(ip)
                        .country("Local Network")
                        .build();
            }
            response = databaseReader.city(inetAddress);
        } catch (UnknownHostException e) {
            log.warn("unknown host: {}", ip);
            return IPLocationVO.builder().ip(ip).build();
        } catch (IOException | GeoIp2Exception e) {
            log.warn("analysis ip location failed: {}", ip, e);
            return IPLocationVO.builder().ip(ip).build();
        }

        var continent = response.getContinent().getNames().get(ZH_CN);
        var country = response.getCountry().getNames().get(ZH_CN);
        var timezone = response.getLocation().getTimeZone();
        // 国内城市显示中文名，国外城市显示英文名
        var region = country.equals(CHINA)
                ? response.getMostSpecificSubdivision().getNames().get(ZH_CN)
                : response.getMostSpecificSubdivision().getName();
        var city = country.equals(CHINA)
                ? response.getCity().getNames().get(ZH_CN)
                : response.getCity().getName();

        return IPLocationVO.builder()
                .ip(ip)
                .city(city)
                .region(region)
                .country(country)
                .continent(continent)
                .timezone(timezone)
                .build();
    }

    public boolean isPublicAddress(InetAddress address) {
        if (address == null
            || address.isLoopbackAddress()
            || address.isLinkLocalAddress()
            || address.isSiteLocalAddress()) {
            return false;
        }

        if (address instanceof Inet4Address) {
            return isPublicIPv4Address((Inet4Address) address);
        } else if (address instanceof Inet6Address) {
            return isPublicIPv6Address((Inet6Address) address);
        }

        return false;
    }

    private static boolean isPublicIPv4Address(Inet4Address address) {
        byte[] bytes = address.getAddress();
        int firstByte = bytes[0] & 0xFF;

        // 检查私有 IP 地址范围和其他特殊用途的 IP 地址范围
        return !(
                firstByte == 10  // 10.0.0.0/8 - 私有网络
                || (firstByte == 172 && (bytes[1] & 0xF0) == 16)  // 172.16.0.0/12 - 私有网络
                || (firstByte == 192 && (bytes[1] & 0xFF) == 168)  // 192.168.0.0/16 - 私有网络
                || (firstByte == 100 && (bytes[1] & 0xC0) == 64)  // 100.64.0.0/10 - 共享地址空间
                || (firstByte == 169 && (bytes[1] & 0xFF) == 254)  // 169.254.0.0/16 - 链路本地
                || (firstByte == 192 && (bytes[1] & 0xFF) == 0 && (bytes[2] & 0xFF) == 0)  // 192.0.0.0/24 - IETF 协议分配
                || (firstByte == 192 && (bytes[1] & 0xFF) == 0 && (bytes[2] & 0xFF) == 2)  // 192.0.2.0/24 - TEST-NET-1
                || (firstByte == 192 && (bytes[1] & 0xFF) == 88 && (bytes[2] & 0xFF) == 99)  // 192.88.99.0/24 - 6to4 中继任播
                || (firstByte >= 198 && firstByte <= 199 && (bytes[1] & 0xFF) == 18)  // 198.18.0.0/15 - 网络设备基准测试
                || (firstByte == 198 && (bytes[1] & 0xFF) == 51 && (bytes[2] & 0xFF) == 100)  // 198.51.100.0/24 - TEST-NET-2
                || (firstByte == 203 && (bytes[1] & 0xFF) == 0 && (bytes[2] & 0xFF) == 113)  // 203.0.113.0/24 - TEST-NET-3
                || firstByte >= 224  // 224.0.0.0/4 - 多播地址和 240.0.0.0/4 - 保留地址
        );
    }

    private static boolean isPublicIPv6Address(Inet6Address address) {
        byte[] bytes = address.getAddress();

        // 检查是否为 IPv4 映射的 IPv6 地址
        if (address.isIPv4CompatibleAddress()) {
            return isPublicIPv4Address(InetAddresses.get6to4IPv4Address(address));
        }

        // 检查特殊用途的 IPv6 地址
        if (bytes[0] == 0 && bytes[1] == 0) {
            // ::/128 (未指定地址), ::1/128 (回环地址)
            return false;
        }
        if ((bytes[0] & 0xFE) == 0xFC) {
            // FC00::/7 (唯一本地地址)
            return false;
        }
        if ((bytes[0] & 0xFF) == 0xFE && (bytes[1] & 0xC0) == 0x80) {
            // FE80::/10 (链路本地单播地址)
            return false;
        }
        if ((bytes[0] & 0xFF) == 0xFF) {
            // FF00::/8 (多播地址)
            return false;
        }

        // 2000::/3 到 3FFF::/3 是全局单播地址
        return (bytes[0] & 0xE0) == 0x20;
    }
}
