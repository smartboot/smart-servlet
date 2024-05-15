/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package org.smartboot.servlet;

import org.smartboot.http.common.utils.NumberUtils;
import org.smartboot.http.common.utils.StringUtils;
import org.smartboot.servlet.conf.ErrorPageInfo;
import org.smartboot.servlet.conf.FilterInfo;
import org.smartboot.servlet.conf.FilterMappingInfo;
import org.smartboot.servlet.conf.ServletInfo;
import org.smartboot.servlet.conf.WebAppInfo;
import org.smartboot.servlet.enums.FilterMappingType;
import org.smartboot.servlet.util.CollectionUtils;
import org.smartboot.servlet.util.PathMatcherUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.servlet.DispatcherType;
import javax.servlet.MultipartConfigElement;
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
import java.util.Set;

/**
 * 解析web.xml文件
 *
 * @author 三刀
 * @version V1.0 , 2019/12/12
 */
class WebXmlParseEngine {

    public void load(WebAppInfo webAppInfo, InputStream contextFile) throws ParserConfigurationException, IOException, SAXException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(contextFile);
        Element parentElement = document.getDocumentElement();

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

    private void parseSessionConfig(WebAppInfo webAppInfo, Element parentElement) {
        List<Node> childNodeList = getChildNode(parentElement, "session-config");
        if (CollectionUtils.isNotEmpty(childNodeList)) {
            Map<String, String> nodeData = getNodeValue(childNodeList.get(0), Collections.singletonList("session-timeout"));
            webAppInfo.setSessionTimeout(NumberUtils.toInt(nodeData.get("session-timeout"), 0));
        }
    }

    private void parseContextParam(WebAppInfo webAppInfo, Element parentElement) {
        List<Node> childNodeList = getChildNode(parentElement, "context-param");
        for (Node node : childNodeList) {
            Map<String, String> nodeData = getNodeValue(node, Arrays.asList("param-name", "param-value"));
            webAppInfo.addContextParam(nodeData.get("param-name"), nodeData.get("param-value"));
        }
    }

    private void parseMimeMapping(WebAppInfo webAppInfo, Element parentElement) {
        List<Node> childNodeList = getChildNode(parentElement, "mime-mapping");
        for (Node node : childNodeList) {
            Map<String, String> nodeData = getNodeValue(node, Arrays.asList("extension", "mime-type"));
            webAppInfo.getMimeMappings().put(nodeData.get("extension"), nodeData.get("mime-type"));
        }
    }

    private void parseListener(WebAppInfo webAppInfo, Element parentElement) {
        List<Node> childNodeList = getChildNode(parentElement, "listener");
        for (Node node : childNodeList) {
            Map<String, String> nodeData = getNodeValue(node, Collections.singletonList("listener-class"));
            webAppInfo.addListener(nodeData.get("listener-class"));
        }
    }


    private void parseFilter(WebAppInfo webAppInfo, Element parentElement) {
        List<Node> childNodeList = getChildNode(parentElement, "filter");
        for (Node node : childNodeList) {
            Map<String, String> nodeData = getNodeValue(node, Arrays.asList("filter-name", "filter-class"));
            FilterInfo filterInfo = new FilterInfo();
            filterInfo.setFilterName(nodeData.get("filter-name"));
            filterInfo.setFilterClass(nodeData.get("filter-class"));
            Map<String, String> initParamMap = parseParam(node);
            initParamMap.forEach(filterInfo::addInitParam);
            webAppInfo.addFilter(filterInfo);
        }
    }

    private void parseErrorPage(WebAppInfo webAppInfo, Element parentElement) {
        List<Node> childNodeList = getChildNode(parentElement, "error-page");
        for (Node node : childNodeList) {
            Map<String, String> nodeData = getNodeValue(node, Arrays.asList("error-code", "location", "exception-type"));
            webAppInfo.addErrorPage(new ErrorPageInfo(nodeData.get("location"), NumberUtils.toInt(nodeData.get("error-code"), -1), nodeData.get("exception-type")));
        }
    }

    private void parseFilterMapping(WebAppInfo webAppInfo, Element parentElement) {
        List<Node> childNodeList = getChildNode(parentElement, "filter-mapping");
        for (Node node : childNodeList) {
            Map<String, String> nodeData = getNodeValue(node, Arrays.asList("filter-name", "url-pattern", "servlet-name"));
            String filterName = nodeData.get("filter-name");
            String urlPattern = nodeData.get("url-pattern");
            String servletName = nodeData.get("servlet-name");
            List<Node> dispatcher = getChildNode(node, "dispatcher");
            Set<DispatcherType> dispatcherTypes = new HashSet<>();
            if (CollectionUtils.isEmpty(dispatcher)) {
                dispatcherTypes.add(DispatcherType.REQUEST);
            } else {
                dispatcher.forEach(dispatcherElement -> dispatcherTypes.add(DispatcherType.valueOf(StringUtils.trim(dispatcherElement.getFirstChild().getNodeValue()))));
            }
            FilterMappingInfo filterInfo = new FilterMappingInfo(filterName, StringUtils.isBlank(urlPattern) ? FilterMappingType.SERVLET : FilterMappingType.URL, servletName, StringUtils.isBlank(urlPattern) ? null : PathMatcherUtil.addMapping(urlPattern), dispatcherTypes);
            webAppInfo.addFilterMapping(filterInfo);
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
            servletInfo.setLoadOnStartup(NumberUtils.toInt(nodeMap.get("load-on-startup"), 0));
            servletInfo.setAsyncSupported(Boolean.parseBoolean(nodeMap.get("async-supported")));
            Map<String, String> initParamMap = parseParam(node);
            initParamMap.forEach(servletInfo::addInitParam);
            servletInfo.setMultipartConfig(parseMultipartConfig(node));
            parseSecurityRole(node).forEach(servletInfo::addSecurityRole);
            webAppInfo.addServlet(servletInfo);
        }
    }

    private List<Node> getChildNode(Node node, String nodeName) {
        NodeList nodeList = node.getChildNodes();
        List<Node> childNodes = new ArrayList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node childNode = nodeList.item(i);
            if (StringUtils.equals(nodeName, childNode.getNodeName())) {
                childNodes.add(childNode);
            }
        }
        return childNodes;
    }

    private Map<String, String> getNodeValue(Node node, Collection<String> nodeNames) {
        NodeList nodeList = node.getChildNodes();
        Map<String, String> nodeMap = new HashMap<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node childNode = nodeList.item(i);
            nodeNames.stream().filter(nodeName -> StringUtils.equals(nodeName, childNode.getNodeName())).forEach(nodeName -> nodeMap.put(nodeName, StringUtils.trim(childNode.getFirstChild().getNodeValue())));
        }
        return nodeMap;
    }

    private List<String> getNodeValues(Node node, String nodeName) {
        NodeList nodeList = node.getChildNodes();
        List<String> list = new ArrayList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node childNode = nodeList.item(i);
            if (childNode.getNodeName().equals(nodeName)) {
                list.add(StringUtils.trim(childNode.getFirstChild().getNodeValue()));
            }
        }
        return list;
    }

    /**
     * 解析Servlet配置
     */
    private void parseServletMapping(WebAppInfo webAppInfo, Element parentElement) {
        List<Node> childNodeList = getChildNode(parentElement, "servlet-mapping");
        for (Node node : childNodeList) {
            Map<String, String> nodeData = getNodeValue(node, Collections.singletonList("servlet-name"));
            ServletInfo servletInfo = webAppInfo.getServlet(nodeData.get("servlet-name"));
            getNodeValues(node, "url-pattern").forEach(servletInfo::addMapping);
        }
    }

    private MultipartConfigElement parseMultipartConfig(Node parentElement) {
        List<Node> paramElementList = getChildNode(parentElement, "multipart-config");
        if (CollectionUtils.isEmpty(paramElementList)) {
            return null;
        }
        if (paramElementList.size() > 1) {
            throw new RuntimeException("config is error");
        }
        Node node = paramElementList.get(0);
        Map<String, String> nodeMap = getNodeValue(node, Arrays.asList("location", "max-file-size", "max-request-size", "file-size-threshold"));
        return new MultipartConfigElement(nodeMap.get("location"), NumberUtils.toInt(nodeMap.get("max-file-size"), -1), NumberUtils.toInt(nodeMap.get("max-request-size"), -1), NumberUtils.toInt(nodeMap.get("file-size-threshold"), -1));
    }

    private Map<String, String> parseSecurityRole(Node parentElement) {
        Map<String, String> roles = new HashMap<>();
        List<Node> childNodeList = getChildNode(parentElement, "security-role-ref");
        for (Node node : childNodeList) {
            Map<String, String> nodeData = getNodeValue(node, Arrays.asList("role-name", "role-link"));
            roles.put(nodeData.get("role-name"), nodeData.get("role-link"));
        }
        return roles;
    }

    private Map<String, String> parseParam(Node parentElement) {
        List<Node> paramElementList = getChildNode(parentElement, "init-param");
        Map<String, String> paramMap = new HashMap<>();
        for (Node element : paramElementList) {
            Map<String, String> nodeMap = getNodeValue(element, Arrays.asList("param-name", "param-value"));
            paramMap.put(nodeMap.get("param-name"), nodeMap.get("param-value"));
        }
        return paramMap;
    }

    /**
     * 解析 <welcome-file-list/>
     */
    private void parseWelcomeFile(WebAppInfo webAppInfo, Element parentElement) {
        List<Node> childNodeList = getChildNode(parentElement, "welcome-file-list");
        if (CollectionUtils.isEmpty(childNodeList)) {
            return;
        }
        List<Node> welcomeFileElement = getChildNode(childNodeList.get(0), "welcome-file");
        for (Node node : welcomeFileElement) {
            webAppInfo.addWelcomeFile(StringUtils.trim(node.getFirstChild().getNodeValue()));
        }
    }


    /**
     * 解析 <locale-encoding-mapping-list/>
     */
    private void parseLocaleEncodingMappings(WebAppInfo webAppInfo, Element parentElement) {
        List<Node> childNodeList = getChildNode(parentElement, "locale-encoding-mapping-list");
        if (CollectionUtils.isEmpty(childNodeList)) {
            return;
        }
        List<Node> mappings = getChildNode(childNodeList.get(0), "locale-encoding-mapping");
        for (Node node : mappings) {
            Map<String, String> mapping = getNodeValue(node, Arrays.asList("locale", "encoding"));
            webAppInfo.getLocaleEncodingMappings().put(mapping.get("locale"), mapping.get("encoding"));
        }
    }
}
