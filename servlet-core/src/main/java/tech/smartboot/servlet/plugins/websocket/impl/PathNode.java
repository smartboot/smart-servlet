/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package tech.smartboot.servlet.plugins.websocket.impl;

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
