import {defineConfig} from 'astro/config';
import starlight from '@astrojs/starlight';
import starlightImageZoomPlugin from "starlight-image-zoom";

// https://astro.build/config
export default defineConfig({
    site: 'https://smartboot.tech/',
    base: '/smart-servlet',
    trailingSlash: "always",
    integrations: [
        starlight({
            title: 'smart-servlet',
            logo: {
                src: './public/logo.svg',
            },
            head: [
                {
                    tag: 'meta',
                    attrs: {
                        name: 'keywords',
                        content: 'smart-servlet,Servlet 容器,Tomcat 替代,轻量级,Jakarta Servlet 6.1,Spring Boot,Java,国产中间件,信创',
                    }
                }, {
                    tag: 'meta',
                    attrs: {
                        name: 'description',
                        content: 'smart-servlet 是基于 Jakarta Servlet 6.1 的轻量级高性能 Servlet 容器，可作为 Tomcat / Undertow 的替代方案，无缝运行 Spring Boot 应用。',
                    }
                }
            ],
            social: {
                github: 'https://github.com/smartboot/smart-servlet',
            },
            plugins: [starlightImageZoomPlugin()],
            defaultLocale: 'root',
            locales: {
                root: {
                    label: '简体中文',
                    lang: 'zh-CN',
                }
            },
            sidebar: [
                {
                    label: '指南',
                    autogenerate: {directory: 'guides'},
                },
                {
                    label: '企业服务',
                    items: [
                        {
                            label: '法律声明与使用条款',
                            link: '/legal/',
                        },
                    ],
                },
            ],
        }),
    ],
});
