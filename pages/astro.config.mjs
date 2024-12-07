import { defineConfig } from 'astro/config';
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
			social: {
				github: 'https://github.com/smartboot/smart-servlet',
			},
			plugins:[starlightImageZoomPlugin()],
			// 为此网站设置英语为默认语言。
			defaultLocale: 'root',
			locales: {
				'zh-cn': {
					label: '简体中文',
					lang: 'zh-CN',
				},
				// 英文文档在 `src/content/docs/en/` 中。
				root: {
					label: 'English',
					lang: 'en'
				}
			},
			sidebar: [
				{
					label: '指南',
					autogenerate: {directory: 'guides'},
				},
				{
					label: '参考',
					autogenerate: { directory: 'reference' },
				},
			],
		}),
	],
});
