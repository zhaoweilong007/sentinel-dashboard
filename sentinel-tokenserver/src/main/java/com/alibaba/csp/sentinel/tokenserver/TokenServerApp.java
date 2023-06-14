package com.alibaba.csp.sentinel.tokenserver;

import com.alibaba.csp.sentinel.cluster.server.ClusterTokenServer;
import com.alibaba.csp.sentinel.cluster.server.SentinelDefaultTokenServer;
import com.alibaba.csp.sentinel.cluster.server.config.ClusterServerConfigManager;

import java.util.Collections;

/**
 * token-server服务，使用nacos动态配置
 *
 * @author ZhaoWeiLong
 * @since 2023/6/14
 **/
public class TokenServerApp {

    static {
        System.setProperty("csp.sentinel.dashboard.server", "localhost:8088");
        System.setProperty("csp.sentienl.api.port", "8719");
        System.setProperty("project.name", "token-server");
        System.setProperty("csp.sentinel.log.use.pid", "true");
    }

    public static void main(String[] args) throws Exception {
        ClusterTokenServer tokenServer = new SentinelDefaultTokenServer();
        ClusterServerConfigManager.loadServerNamespaceSet(Collections.singleton("APP_NAME"));
        tokenServer.start();
    }
}
