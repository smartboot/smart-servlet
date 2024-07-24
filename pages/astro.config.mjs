import { defineConfig } from 'astro/config';
import starlight from '@astrojs/starlight';

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
			// 为此网站设置英语为默认语言。
			defaultLocale: 'root',
			locales: {
				root: {
					label: '简体中文',
					lang: 'zh-CN',
				},
				// 英文文档在 `src/content/docs/en/` 中。
				en: {
					label: 'English',
					lang: 'en'
				}
			},
			sidebar: [
				{
					label: 'Guides',
					items: [
						// Each item here is one entry in the navigation menu.
						{ label: 'Example Guide', slug: 'guides/example' },
					],
				},
				{
					label: 'Reference',
					autogenerate: { directory: 'reference' },
				},
			],
		}),
	],
});
