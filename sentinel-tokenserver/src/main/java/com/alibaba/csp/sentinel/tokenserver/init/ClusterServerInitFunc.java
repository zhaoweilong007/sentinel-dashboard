/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.tokenserver.init;

import com.alibaba.csp.sentinel.cluster.flow.rule.ClusterFlowRuleManager;
import com.alibaba.csp.sentinel.cluster.flow.rule.ClusterParamFlowRuleManager;
import com.alibaba.csp.sentinel.cluster.server.config.ClusterServerConfigManager;
import com.alibaba.csp.sentinel.cluster.server.config.ServerTransportConfig;
import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.datasource.nacos.NacosDataSource;
import com.alibaba.csp.sentinel.init.InitFunc;
import com.alibaba.csp.sentinel.init.InitOrder;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.nacos.api.PropertyKeyConst;

import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * 参考官方配置
 *
 * @author Eric Zhao
 */
@InitOrder(1)
public class ClusterServerInitFunc implements InitFunc {

    /**
     * Nacos 地址
     */
    public static final String NACOS_ADDRS = "localhost:8848";
    public static final String NACOS_USERNAME = "nacos";
    public static final String NACOS_PASSWORD = "nacos";

    /**
     * 对应 Nacos 的命名空间 ID
     */
    public static final String NACOS_SENTINEL_NAMESPACE = "";

    /**
     * Nacos group id
     */
    public static final String NACOS_SENTINEL_GROUPID = "SENTINEL_GROUP";

    /**
     * 集群限流规则 dataID
     * 获取到的值为规则列表，如： List<FlowRule>
     */
    public static final String NACOS_SENTINEL_CLUSTER_Flow_RULES_DATAID = "Cluster-Flow-Rule";

    public static final String NACOS_SENTINEL_CLUSTER_Param_RULES_DATAID = "Cluster-Param-Rule";

    /**
     * 集群流控作用域（默认对应集群应用的 project.name 应用名）
     * 获取到的值为 Set<String> 类型，即为应用名列表
     */
    public static final String NACOS_SENTINEL_NAMESPACES_DATAID = "Cluster-Name-Space";

    /**
     * 集群流控 Server 端配置
     * 获取到的值为 ServerTransportConfig 类型
     */
    public static final String NACOS_SENTINEL_SERVER_TRANSPORT_CONFIG_DATAID = "Cluster-Server-Config";

    @Override
    public void init() throws Exception {
        Properties properties = new Properties();
        properties.put(PropertyKeyConst.SERVER_ADDR, NACOS_ADDRS);
        properties.put(PropertyKeyConst.NAMESPACE, NACOS_SENTINEL_NAMESPACE);
        properties.put(PropertyKeyConst.USERNAME, NACOS_USERNAME);
        properties.put(PropertyKeyConst.PASSWORD, NACOS_PASSWORD);

        // Register cluster flow rule property supplier which creates data source by namespace.
        // 配置 Nacos 动态规则数据源 (此处只定义了限流规则，其他规则如：热点参数规则可按此示例配置即可)
        ClusterFlowRuleManager.setPropertySupplier(namespace -> {
            ReadableDataSource<String, List<FlowRule>> ds = new NacosDataSource<>(properties, NACOS_SENTINEL_GROUPID, NACOS_SENTINEL_CLUSTER_Flow_RULES_DATAID,
                    source -> JSON.parseObject(source, new TypeReference<List<FlowRule>>() {
                    }));
            return ds.getProperty();
        });

        // Register cluster parameter flow rule property supplier.
        ClusterParamFlowRuleManager.setPropertySupplier(namespace -> {
            ReadableDataSource<String, List<ParamFlowRule>> ds = new NacosDataSource<>(properties, NACOS_SENTINEL_GROUPID, NACOS_SENTINEL_CLUSTER_Param_RULES_DATAID,
                    source -> JSON.parseObject(source, new TypeReference<List<ParamFlowRule>>() {
                    }));
            return ds.getProperty();
        });

        // Server namespace set (scope) data source.
        ReadableDataSource<String, Set<String>> namespaceDs = new NacosDataSource<>(properties, NACOS_SENTINEL_GROUPID, NACOS_SENTINEL_NAMESPACES_DATAID,
                source -> JSON.parseObject(source, new TypeReference<Set<String>>() {
                }));
        ClusterServerConfigManager.registerNamespaceSetProperty(namespaceDs.getProperty());


        // Server transport configuration data source.
        ReadableDataSource<String, ServerTransportConfig> transportConfigDs = new NacosDataSource<>(properties, NACOS_SENTINEL_GROUPID,
                NACOS_SENTINEL_SERVER_TRANSPORT_CONFIG_DATAID,
                source -> JSON.parseObject(source, new TypeReference<ServerTransportConfig>() {
                }));
        ClusterServerConfigManager.registerServerTransportProperty(transportConfigDs.getProperty());
    }
}
