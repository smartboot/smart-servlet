/*
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: PathNode.java
 * Date: 2021-05-02
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.plugins.websocket.impl;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/5/2
 */
public class PathNode {

    /**
     * 路径节点名称
     */
    private String nodeName;
    /**
     * 是否模式匹配
     */
    private boolean patternMatching;

    public static List<PathNode> convertToPathNodes(String path) {
        if (path.charAt(0) != '/') {
            throw new IllegalStateException("invalid path:" + path);
        }
        List<PathNode> pathNodes = new ArrayList<>();
        for (int i = 1; i < path.length(); i++) {
            if (path.charAt(i) == '/') {
                throw new IllegalStateException("invalid path:" + path);
            }
            int start = i++;
            for (; i < path.length(); i++) {
                if (path.charAt(i) == '/') {
                    break;
                }
            }
            PathNode pathNode = new PathNode();
            pathNode.setPatternMatching(path.charAt(start) == '{' && path.charAt(i - 1) == '}');
            if (pathNode.isPatternMatching()) {
                pathNode.setNodeName(path.substring(start + 1, i - 1));
            } else {
                pathNode.setNodeName(path.substring(start, i));
            }


            pathNodes.add(pathNode);
        }
        return pathNodes;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public boolean isPatternMatching() {
        return patternMatching;
    }

    public void setPatternMatching(boolean patternMatching) {
        this.patternMatching = patternMatching;
    }
}
