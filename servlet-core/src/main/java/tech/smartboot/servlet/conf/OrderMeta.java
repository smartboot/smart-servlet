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

package tech.smartboot.servlet.conf;

import java.net.URL;
import java.util.List;

public class OrderMeta {
    private String name;
    private List<String> before;
    private boolean beforeOthers;
    private List<String> after;
    private boolean afterOthers;
    private URL url;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getBefore() {
        return before;
    }

    public void setBefore(List<String> before) {
        this.before = before;
    }

    public List<String> getAfter() {
        return after;
    }

    public void setAfter(List<String> after) {
        this.after = after;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public boolean isBeforeOthers() {
        return beforeOthers;
    }

    public void setBeforeOthers(boolean beforeOthers) {
        this.beforeOthers = beforeOthers;
    }

    public boolean isAfterOthers() {
        return afterOthers;
    }

    public void setAfterOthers(boolean afterOthers) {
        this.afterOthers = afterOthers;
    }
}
