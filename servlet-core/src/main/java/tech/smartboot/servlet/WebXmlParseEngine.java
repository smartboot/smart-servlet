/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package tech.smartboot.servlet;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.annotation.ServletSecurity;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import tech.smartboot.feat.core.common.FeatUtils;
import tech.smartboot.servlet.conf.ErrorPageInfo;
import tech.smartboot.servlet.conf.FilterInfo;
import tech.smartboot.servlet.conf.FilterMappingInfo;
import tech.smartboot.servlet.conf.LoginConfig;
import tech.smartboot.servlet.conf.OrderMeta;
import tech.smartboot.servlet.conf.SecurityConstraint;
import tech.smartboot.servlet.conf.ServletInfo;
import tech.smartboot.servlet.conf.UrlPattern;
import tech.smartboot.servlet.conf.WebAppInfo;
import tech.smartboot.servlet.conf.WebFragmentInfo;
import tech.smartboot.servlet.enums.FilterMappingType;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 解析web.xml文件
 *
 * @author 三刀
 * @version V1.0 , 2019/12/12
 */
class WebXmlParseEngine {

    public void loadFragment(WebAppInfo webAppInfo, InputStream contextFile) throws ParserConfigurationException, IOException, SAXException {
        WebFragmentInfo fragment = new WebFragmentInfo();
        Element element = commonParse(fragment, contextFile);
        fragment.setName(parseFragmentName(element));
        fragment.mergeTo(webAppInfo);
    }

    public String parseFragmentName(InputStream contextFile) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(contextFile);
        Element parentElement = document.getDocumentElement();
        return parseFragmentName(parentElement);
    }

    public OrderMeta parseFragmentRelativeOrdering(InputStream contextFile) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(contextFile);
        Element parentElement = document.getDocumentElement();
        OrderMeta orderMeta = new OrderMeta();
        String name = parseFragmentName(parentElement);
        orderMeta.setName(name);
        parseOrdering(orderMeta, parentElement);
        return orderMeta;
    }

    public void load(WebAppInfo webAppInfo, InputStream contextFile) throws ParserConfigurationException, IOException, SAXException {
        commonParse(webAppInfo, contextFile);
    }

    private Element commonParse(WebAppInfo webAppInfo, InputStream contextFile) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(contextFile);
        Element parentElement = document.getDocumentElement();

        webAppInfo.setMetadataComplete("true".equals(parentElement.getAttribute("metadata-complete")));
        webAppInfo.setVersion(parentElement.getAttribute("version"));
        parseBasicInfo(webAppInfo, parentElement);

        parseServlet(webAppInfo, parentElement);
        parseServletMapping(webAppInfo, parentElement);

        parseFilter(webAppInfo, parentElement);
        parseFilterMapping(webAppInfo, parentElement);

        parseListener(webAppInfo, parentElement);

        parseContextParam(webAppInfo, parentElement);

        parseErrorPage(webAppInfo, parentElement);

        parseSessionConfig(webAppInfo, parentElement);

        parseWelcomeFile(webAppInfo, parentElement);

        parseLocaleEncodingMappings(webAppInfo, parentElement);

        parseMimeMapping(webAppInfo, parentElement);

        parseSecurityRole(webAppInfo, parentElement);

        parseSecurityConstraint(webAppInfo, parentElement);

        parseSecurityRoleMapping(webAppInfo, parentElement);

        parseAbsoluteOrdering(webAppInfo, parentElement);

        parseLoginConfig(webAppInfo, parentElement);
        return parentElement;
    }

    private void parseLoginConfig(WebAppInfo webAppInfo, Element parentElement) {
        Node node = getChildNode(parentElement, "login-config");
        if (node == null) {
            return;
        }
        Map<String, String> nodeValues = getNodeValue(node, List.of("realm-name"));
        LoginConfig loginConfig = new LoginConfig();
        loginConfig.setRealmName(nodeValues.get("realm-name"));
        Node formLoginConfig = getChildNode(node, "form-login-config");
        if (formLoginConfig != null) {
            nodeValues = getNodeValue(formLoginConfig, List.of("form-login-page", "form-error-page"));
            loginConfig.setErrorPage(nodeValues.get("form-error-page"));
            loginConfig.setLoginPage(nodeValues.get("form-login-page"));
        }
        Map<String, String> authMethod = getNodeValue(node, List.of("auth-method"));
        loginConfig.setAuthMethod(authMethod.get("auth-method"));
        webAppInfo.setLoginConfig(loginConfig);
    }

    private void parseBasicInfo(WebAppInfo webAppInfo, Element parentElement) {
        Map<String, String> map = getNodeValue(parentElement, Arrays.asList("display-name", "description"));
        if (map.containsKey("display-name")) {
            webAppInfo.setDisplayName(map.get("display-name"));
        }
        if (map.containsKey("description")) {
            webAppInfo.setDescription(map.get("description"));
        }
    }

    private String parseFragmentName(Element parentElement) {
        Map<String, String> map = getNodeValue(parentElement, List.of("name"));
        return map.containsKey("name") ? map.get("name") : "default_" + parentElement.hashCode();
    }

    private void parseOrdering(OrderMeta orderMeta, Element parentElement) {
        Node ordering = getChildNode(parentElement, "ordering");
        if (ordering == null) {
            return;
        }
        Node before = getChildNode(ordering, "before");
        if (before != null) {
            orderMeta.setBefore(getNodeValues(before, "name"));
            var othersNode = getChildNode(before, "others");
            if (othersNode != null) {
                orderMeta.setBeforeOthers(true);
            }
        }
        Node after = getChildNode(ordering, "after");
        if (after != null) {
            orderMeta.setAfter(getNodeValues(after, "name"));
            var othersNode = getChildNode(after, "others");
            if (othersNode != null) {
                orderMeta.setAfterOthers(true);
            }
        }
    }

    private void parseSessionConfig(WebAppInfo webAppInfo, Element parentElement) {
        List<Node> childNodeList = getChildNodes(parentElement, "session-config");
        if (FeatUtils.isNotEmpty(childNodeList)) {
            Map<String, String> nodeData = getNodeValue(childNodeList.get(0), Collections.singletonList("session-timeout"));
            webAppInfo.setSessionTimeout(FeatUtils.toInt(nodeData.get("session-timeout"), 0));
        }
    }

    private void parseContextParam(WebAppInfo webAppInfo, Element parentElement) {
        List<Node> childNodeList = getChildNodes(parentElement, "context-param");
        for (Node node : childNodeList) {
            Map<String, String> nodeData = getNodeValue(node, Arrays.asList("param-name", "param-value"));
            webAppInfo.addContextParam(nodeData.get("param-name"), nodeData.get("param-value"));
        }
    }

    private void parseMimeMapping(WebAppInfo webAppInfo, Element parentElement) {
        List<Node> childNodeList = getChildNodes(parentElement, "mime-mapping");
        for (Node node : childNodeList) {
            Map<String, String> nodeData = getNodeValue(node, Arrays.asList("extension", "mime-type"));
            webAppInfo.getMimeMappings().put(nodeData.get("extension"), nodeData.get("mime-type"));
        }
    }

    private void parseListener(WebAppInfo webAppInfo, Element parentElement) {
        List<Node> childNodeList = getChildNodes(parentElement, "listener");
        for (Node node : childNodeList) {
            Map<String, String> nodeData = getNodeValue(node, Collections.singletonList("listener-class"));
            webAppInfo.addListener(nodeData.get("listener-class"));
        }
    }


    private void parseFilter(WebAppInfo webAppInfo, Element parentElement) {
        List<Node> childNodeList = getChildNodes(parentElement, "filter");
        for (Node node : childNodeList) {
            Map<String, String> nodeData = getNodeValue(node, Arrays.asList("filter-name", "filter-class", "async-supported"));
            FilterInfo filterInfo = new FilterInfo();
            filterInfo.setFilterName(nodeData.get("filter-name"));
            filterInfo.setFilterClass(nodeData.get("filter-class"));
            filterInfo.setAsyncSupported(Boolean.parseBoolean(nodeData.get("async-supported")));
            Map<String, String> initParamMap = parseParam(node);
            initParamMap.forEach(filterInfo::addInitParam);
            webAppInfo.addFilter(filterInfo);
        }
    }

    private void parseErrorPage(WebAppInfo webAppInfo, Element parentElement) {
        List<Node> childNodeList = getChildNodes(parentElement, "error-page");
        for (Node node : childNodeList) {
            Map<String, String> nodeData = getNodeValue(node, Arrays.asList("error-code", "location", "exception-type"));
            webAppInfo.addErrorPage(new ErrorPageInfo(nodeData.get("location"), FeatUtils.toInt(nodeData.get("error-code"), -1), nodeData.get("exception-type")));
        }
    }

    private void parseFilterMapping(WebAppInfo webAppInfo, Element parentElement) {
        List<Node> childNodeList = getChildNodes(parentElement, "filter-mapping");
        for (Node node : childNodeList) {
            Map<String, String> nodeData = getNodeValue(node, Arrays.asList("filter-name"));
            String filterName = nodeData.get("filter-name");
            List<String> dispatchers = getNodeValues(node, "dispatcher");
            Set<DispatcherType> dispatcherTypes = new HashSet<>();
            if (FeatUtils.isEmpty(dispatchers)) {
                dispatcherTypes.add(DispatcherType.REQUEST);
            } else {
                dispatchers.forEach(dispatcher -> dispatcherTypes.add(DispatcherType.valueOf(dispatcher)));
            }
            getNodeValues(node, "url-pattern").forEach(urlPattern -> {
                FilterMappingInfo filterInfo = new FilterMappingInfo(filterName, FilterMappingType.URL, null, urlPattern, dispatcherTypes);
                webAppInfo.getFilterMappingInfos().add(filterInfo);
            });
            getNodeValues(node, "servlet-name").forEach(servletName -> {
                FilterMappingInfo filterInfo = new FilterMappingInfo(filterName, FilterMappingType.SERVLET, servletName, null, dispatcherTypes);
                webAppInfo.getFilterMappingInfos().add(filterInfo);
            });
        }
    }

    /**
     * 解析Servlet配置
     */
    private void parseServlet(WebAppInfo webAppInfo, Element parentElement) {
        NodeList rootNodeList = parentElement.getElementsByTagName("servlet");
        for (int i = 0; i < rootNodeList.getLength(); i++) {
            Node node = rootNodeList.item(i);
            Map<String, String> nodeMap = getNodeValue(node, Arrays.asList("servlet-name", "servlet-class", "load-on-startup", "async-supported", "jsp-file"));
            ServletInfo servletInfo = new ServletInfo();
            servletInfo.setServletName(nodeMap.get("servlet-name"));
            servletInfo.setServletClass(nodeMap.get("servlet-class"));
            servletInfo.setJspFile(nodeMap.get("jsp-file"));
            servletInfo.setLoadOnStartup(FeatUtils.toInt(nodeMap.get("load-on-startup"), 0));
            servletInfo.setAsyncSupported(Boolean.parseBoolean(nodeMap.get("async-supported")));
            Map<String, String> initParamMap = parseParam(node);
            initParamMap.forEach(servletInfo::addInitParam);
            servletInfo.setMultipartConfig(parseMultipartConfig(node));
            parseSecurityRoleRef(node).forEach(servletInfo::addSecurityRole);
            webAppInfo.addServlet(servletInfo);
        }
    }

    private List<Node> getChildNodes(Node node, String nodeName) {
        NodeList nodeList = node.getChildNodes();
        List<Node> childNodes = new ArrayList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node childNode = nodeList.item(i);
            if (FeatUtils.equals(nodeName, childNode.getNodeName())) {
                childNodes.add(childNode);
            }
        }
        return childNodes;
    }

    private Node getChildNode(Node node, String nodeName) {
        List<Node> childNodes = getChildNodes(node, nodeName);
        if (childNodes.size() > 1) {
            throw new IllegalStateException();
        }
        return childNodes.size() == 1 ? childNodes.get(0) : null;
    }

    private Map<String, String> getNodeValue(Node node, Collection<String> nodeNames) {
        NodeList nodeList = node.getChildNodes();
        Map<String, String> nodeMap = new HashMap<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node childNode = nodeList.item(i);
            nodeNames.stream().filter(nodeName -> FeatUtils.equals(nodeName, childNode.getNodeName())).forEach(nodeName -> nodeMap.put(nodeName, FeatUtils.trim(childNode.getFirstChild().getNodeValue())));
        }
        return nodeMap;
    }

    private List<String> getNodeValues(Node node, String nodeName) {
        NodeList nodeList = node.getChildNodes();
        List<String> list = new ArrayList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node childNode = nodeList.item(i);
            if (childNode.getNodeName().equals(nodeName)) {
                list.add(FeatUtils.trim(childNode.getFirstChild().getNodeValue()));
            }
        }
        return list;
    }

    /**
     * 解析Servlet配置
     */
    private void parseServletMapping(WebAppInfo webAppInfo, Element parentElement) {
        List<Node> childNodeList = getChildNodes(parentElement, "servlet-mapping");
        for (Node node : childNodeList) {
            Map<String, String> nodeData = getNodeValue(node, Collections.singletonList("servlet-name"));
            String servletName = nodeData.get("servlet-name");
            getNodeValues(node, "url-pattern").forEach(urlPattern -> {
                webAppInfo.getServletMappings().computeIfAbsent(servletName, s -> new ArrayList<>()).add(urlPattern);
            });
        }
    }

    private MultipartConfigElement parseMultipartConfig(Node parentElement) {
        List<Node> paramElementList = getChildNodes(parentElement, "multipart-config");
        if (FeatUtils.isEmpty(paramElementList)) {
            return null;
        }
        if (paramElementList.size() > 1) {
            throw new RuntimeException("config is error");
        }
        Node node = paramElementList.get(0);
        Map<String, String> nodeMap = getNodeValue(node, Arrays.asList("location", "max-file-size", "max-request-size", "file-size-threshold"));
        return new MultipartConfigElement(nodeMap.get("location"), FeatUtils.toInt(nodeMap.get("max-file-size"), -1), FeatUtils.toInt(nodeMap.get("max-request-size"), -1), FeatUtils.toInt(nodeMap.get("file-size-threshold"), -1));
    }

    private Map<String, String> parseSecurityRoleRef(Node parentElement) {
        Map<String, String> roles = new HashMap<>();
        List<Node> childNodeList = getChildNodes(parentElement, "security-role-ref");
        for (Node node : childNodeList) {
            Map<String, String> nodeData = getNodeValue(node, Arrays.asList("role-name", "role-link"));
            roles.put(nodeData.get("role-name"), nodeData.get("role-link"));
        }
        return roles;
    }

    private void parseSecurityRole(WebAppInfo webAppInfo, Element parentElement) {
        List<Node> childNodeList = getChildNodes(parentElement, "security-role");
        for (Node node : childNodeList) {
            Map<String, String> nodeData = getNodeValue(node, Arrays.asList("role-name", "description"));
            webAppInfo.getSecurityRoles().put(nodeData.get("role-name"), nodeData.get("description"));
        }
    }

    private void parseAbsoluteOrdering(WebAppInfo webAppInfo, Element parentElement) {
        Node node = getChildNode(parentElement, "absolute-ordering");
        if (node == null) {
            return;
        }
        List<String> names = getNodeValues(node, "name");
        webAppInfo.setAbsoluteOrdering(names);
        var othersNode = getChildNode(node, "others");
        if (othersNode != null) {
            webAppInfo.setAbsoluteOrderingOther(true);
        }
    }

    private void parseSecurityRoleMapping(WebAppInfo webAppInfo, Element parentElement) {
        List<Node> childNodeList = getChildNodes(parentElement, "security-role-mapping");
        for (Node node : childNodeList) {
            Map<String, String> nodeData = getNodeValue(node, Arrays.asList("role-name"));
            String roleName = nodeData.get("role-name");
            getNodeValues(node, "principal-name").forEach(principalName -> webAppInfo.getSecurityRoleMapping().computeIfAbsent(principalName, k -> new HashSet<>()).add(roleName));
        }
    }

    private void parseSecurityConstraint(WebAppInfo webAppInfo, Element parentElement) {
        List<Node> childNodeList = getChildNodes(parentElement, "security-constraint");
        for (Node node : childNodeList) {
            SecurityConstraint securityConstraint = new SecurityConstraint();
            Node webResourceCollection = Objects.requireNonNull(getChildNode(node, "web-resource-collection"));
//            Map<String, List<String>> data = getNodeValues(webResourceCollection, Arrays.asList("web-resource-name", "url-pattern", "http-method"));
            securityConstraint.getHttpMethods().addAll(getNodeValues(webResourceCollection, "http-method"));
            securityConstraint.getHttpMethodOmissions().addAll(getNodeValues(webResourceCollection, "http-method-omission"));
            getNodeValues(webResourceCollection, "url-pattern").forEach(urlPattern -> securityConstraint.getUrlPatterns().add(new UrlPattern(urlPattern)));
//            securityConstraint.getResourceNames().addAll(getNodeValues(webResourceCollection, "web-resource-name"));


            Node authConstraint = getChildNode(node, "auth-constraint");
            if (authConstraint != null) {
                securityConstraint.setRoleNames(getNodeValues(authConstraint, "role-name"));
            } else {
                securityConstraint.setEmptyRoleSemantic(ServletSecurity.EmptyRoleSemantic.PERMIT);
            }

            Node userDataConstraint = getChildNode(node, "user-data-constraint");
            if (userDataConstraint != null) {
                String transportGuarantees = getNodeValue(userDataConstraint, Collections.singletonList("transport-guarantee")).get("transport-guarantee");
                if ("CONFIDENTIAL".equals(transportGuarantees)) {
                    securityConstraint.setTransportGuarantee(ServletSecurity.TransportGuarantee.CONFIDENTIAL);
                } else {
                    securityConstraint.setTransportGuarantee(ServletSecurity.TransportGuarantee.NONE);
                }
            }
            webAppInfo.getSecurityConstraints().add(securityConstraint);
        }
    }

    private Map<String, String> parseParam(Node parentElement) {
        List<Node> paramElementList = getChildNodes(parentElement, "init-param");
        Map<String, String> paramMap = new HashMap<>();
        for (Node element : paramElementList) {
            Map<String, String> nodeMap = getNodeValue(element, Arrays.asList("param-name", "param-value"));
            if (!paramMap.containsKey(nodeMap.get("param-name"))) {
                paramMap.put(nodeMap.get("param-name"), nodeMap.get("param-value"));
            }
        }
        return paramMap;
    }

    /**
     * 解析 <welcome-file-list/>
     */
    private void parseWelcomeFile(WebAppInfo webAppInfo, Element parentElement) {
        List<Node> childNodeList = getChildNodes(parentElement, "welcome-file-list");
        if (FeatUtils.isEmpty(childNodeList)) {
            return;
        }
        List<Node> welcomeFileElement = getChildNodes(childNodeList.get(0), "welcome-file");
        for (Node node : welcomeFileElement) {
            webAppInfo.addWelcomeFile(FeatUtils.trim(node.getFirstChild().getNodeValue()));
        }
    }


    /**
     * 解析 <locale-encoding-mapping-list/>
     */
    private void parseLocaleEncodingMappings(WebAppInfo webAppInfo, Element parentElement) {
        List<Node> childNodeList = getChildNodes(parentElement, "locale-encoding-mapping-list");
        if (FeatUtils.isEmpty(childNodeList)) {
            return;
        }
        List<Node> mappings = getChildNodes(childNodeList.get(0), "locale-encoding-mapping");
        for (Node node : mappings) {
            Map<String, String> mapping = getNodeValue(node, Arrays.asList("locale", "encoding"));
            webAppInfo.getLocaleEncodingMappings().put(mapping.get("locale"), mapping.get("encoding"));
        }
    }
}
