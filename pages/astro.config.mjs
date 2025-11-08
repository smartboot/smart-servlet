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
                        property: 'keywords',
                        content: '信创,国产,国产化,替代,tomcat,国产Servlet容器,国产Servlet,smart-servlet,smartservlet,undertow,jetty,自研,自研Tomcat',
                    }
                }, {
                    tag: 'meta',
                    attrs: {
                        property: 'description',
                        content: 'smart-servlet 提供一个自主可控的轻量级 Tomcat/Undertow 可替代版本，重新定义下一代 Servlet 容器!',
                    }
                },{
                tag:'script',
                    attrs: {
                        src: 'https://smartboot.tech/js/gitee.js'
                    }
                },
                {
                    tag:'script',
                    content: 'if(!location.pathname.endsWith("smart-servlet/")){checkStar("smartboot-x","smart-servlet");}'
                },
            ],
            social: {
                github: 'https://github.com/smartboot/smart-servlet',
            },
            plugins: [starlightImageZoomPlugin()],
            // 为此网站设置英语为默认语言。
            defaultLocale: 'root',
            locales: {
                // 'zh-cn': {
                //     label: '简体中文',
                //     lang: 'zh-CN',
                // },
                // 'en': {
                //     label: 'English',
                //     lang: 'en',
                // },
                // 英文文档在 `src/content/docs/en/` 中。
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
                    label: '参考',
                    autogenerate: {directory: 'reference'},
                },
            ],
        }),
    ],
});
