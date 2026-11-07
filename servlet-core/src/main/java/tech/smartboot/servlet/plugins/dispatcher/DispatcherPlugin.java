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

package tech.smartboot.servlet.plugins.dispatcher;

import tech.smartboot.servlet.ServletContextRuntime;
import tech.smartboot.servlet.plugins.Plugin;

/**
 * @author 三刀
 * @version V1.0 , 2020/11/27
 */
public class DispatcherPlugin extends Plugin {

    @Override
    public void willStartServletContext(ServletContextRuntime containerRuntime) {
        containerRuntime.setDispatcherProvider(new DispatcherProviderImpl());
    }

}
