/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: WebXmlParseEngine.java
 * Date: 2020-12-31
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet;

import org.smartboot.http.common.utils.CollectionUtils;
import org.smartboot.http.common.utils.NumberUtils;
import org.smartboot.http.common.utils.StringUtils;
import org.smartboot.servlet.conf.ErrorPageInfo;
import org.smartboot.servlet.conf.FilterInfo;
import org.smartboot.servlet.conf.FilterMappingInfo;
import org.smartboot.servlet.conf.ServletInfo;
import org.smartboot.servlet.conf.WebAppInfo;
import org.smartboot.servlet.enums.FilterMappingType;
import org.smartboot.servlet.util.PathMatcherUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.servlet.DispatcherType;
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

    public WebAppInfo load(InputStream webXmlStream) throws ParserConfigurationException, IOException, SAXException {
        WebAppInfo webAppInfo = new WebAppInfo();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(webXmlStream);
        Element parentElement = document.getDocumentElement();

        parseServlet(webAppInfo, parentElement);
        parseServletMapping(webAppInfo, parentElement);

        parseFilter(webAppInfo, parentElement);
        parseFilterMapping(webAppInfo, parentElement);

        parseListener(webAppInfo, parentElement);

        parseContextParam(webAppInfo, parentElement);

        parseErrorPage(webAppInfo, parentElement);

        parseSessionConfig(webAppInfo, parentElement);

        parseWelcomeFile(webAppInfo, parentElement);
        return webAppInfo;
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
            int errorCode = NumberUtils.toInt(nodeData.get("error-code"), -1);
            if (errorCode < 0) {
                continue;
            }
            webAppInfo.addErrorPage(new ErrorPageInfo(nodeData.get("location"), errorCode, nodeData.get("exception-type")));
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
            FilterMappingInfo filterInfo = new FilterMappingInfo(filterName
                    , StringUtils.isBlank(urlPattern) ? FilterMappingType.SERVLET : FilterMappingType.URL,
                    servletName, StringUtils.isBlank(urlPattern) ? null : PathMatcherUtil.addMapping(urlPattern),
                    dispatcherTypes);
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
            Map<String, String> nodeMap = getNodeValue(node, Arrays.asList("servlet-name", "servlet-class", "load-on-startup"));
            ServletInfo servletInfo = new ServletInfo();
            servletInfo.setServletName(nodeMap.get("servlet-name"));
            servletInfo.setServletClass(nodeMap.get("servlet-class"));
            servletInfo.setLoadOnStartup(NumberUtils.toInt(nodeMap.get("load-on-startup"), 0));
            Map<String, String> initParamMap = parseParam(node);
            initParamMap.forEach(servletInfo::addInitParam);
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
            nodeNames.stream()
                    .filter(nodeName -> StringUtils.equals(nodeName, childNode.getNodeName()))
                    .forEach(nodeName -> nodeMap.put(nodeName, StringUtils.trim(childNode.getFirstChild().getNodeValue())));
        }
        return nodeMap;
    }

    /**
     * 解析Servlet配置
     */
    private void parseServletMapping(WebAppInfo webAppInfo, Element parentElement) {
        List<Node> childNodeList = getChildNode(parentElement, "servlet-mapping");
        for (Node node : childNodeList) {
            Map<String, String> nodeData = getNodeValue(node, Arrays.asList("servlet-name", "url-pattern"));
            ServletInfo servletInfo = webAppInfo.getServlet(nodeData.get("servlet-name"));
            servletInfo.addMapping(nodeData.get("url-pattern"));
        }
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
}
