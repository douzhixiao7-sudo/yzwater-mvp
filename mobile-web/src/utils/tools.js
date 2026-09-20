/**
 * 工具类 - 提供项目中常用的工具方法
 * @author 项目团队
 * @version 1.0.0
 */

import dayjs from "dayjs";

// 工具类对象
const tool = { dayjs: dayjs };

/**
 * 日期格式化方法
 * @param {Date|string|number} date - 要格式化的日期
 * @param {string} fmt - 格式化模板，默认 "yyyy-MM-dd hh:mm:ss"
 * @returns {string} 格式化后的日期字符串
 * 
 * @example
 * tool.dateFormat(new Date(), "yyyy-MM-dd") // "2024-01-15"
 * tool.dateFormat("2024-01-15", "yyyy年MM月dd日") // "2024年01月15日"
 */
tool.dateFormat = function (date, fmt = "yyyy-MM-dd hh:mm:ss") {
	date = new Date(date);
	var o = {
		"M+": date.getMonth() + 1, // 月份
		"d+": date.getDate(), // 日
		"h+": date.getHours(), // 小时
		"m+": date.getMinutes(), // 分
		"s+": date.getSeconds(), // 秒
		"q+": Math.floor((date.getMonth() + 3) / 3), // 季度
		S: date.getMilliseconds(), // 毫秒
	};
	if (/(y+)/.test(fmt)) {
		fmt = fmt.replace(
			RegExp.$1,
			(date.getFullYear() + "").substr(4 - RegExp.$1.length)
		);
	}
	for (var k in o) {
		if (new RegExp("(" + k + ")").test(fmt)) {
			fmt = fmt.replace(
				RegExp.$1,
				RegExp.$1.length == 1
					? o[k]
					: ("00" + o[k]).substr(("" + o[k]).length)
			);
		}
	}
	return fmt;
};

/**
 * 解析图片路径的内部方法
 * @param {string} imagePath - 图片路径
 * @returns {string} 解析后的图片URL
 * 
 * @private
 */
function resolveImagePath(imagePath) {
	// 网络图片直接返回
	if (/^(https?:)?\/\//.test(imagePath)) {
		return imagePath;
	}

	// 如果已经是Vite处理过的URL（包含hash），直接返回
	if (imagePath.includes('/assets/') && imagePath.includes('-')) {
		return imagePath;
	}

	// 处理@别名路径（指向src/assets）
	if (imagePath.startsWith('@/')) {
		try {
			// 在Vite中，使用import.meta.url和new URL来正确处理资源路径
			// 将@/assets路径转换为相对于当前文件的路径
			const relativePath = imagePath.replace('@/', '../');
			const url = new URL(relativePath, import.meta.url).href;
			return url;
		} catch (err) {
			console.warn(`Failed to resolve image: ${imagePath}`, err);
			// 如果是天气图标且出现错误，返回默认值
			if (imagePath.includes('weatherIcon')) {
				try {
					return new URL('../assets/img/weatherIcon/pz-commonWeather.png', import.meta.url).href;
				} catch (defaultErr) {
					return '';
				}
			}
			return '';
		}
	}

	// 处理public目录下的静态资源
	if (imagePath.startsWith('/')) {
		return process.env.NODE_ENV === 'production'
			? `${import.meta.env.BASE_URL}${imagePath.substring(1)}`
			: imagePath;
	}

	// 其他情况直接返回（可能是相对路径）
	return imagePath;
}

/**
 * 设置背景样式
 * @param {string} imagePath - 图片路径，支持多种格式
 * @param {Object} extraStyles - 额外的样式对象
 * @returns {Object} 背景样式对象
 * 
 * @example
 * tool.setBackgroundStyle('@/assets/img/logo.png')
 * tool.setBackgroundStyle('/img/logo.png')
 * tool.setBackgroundStyle('https://example.com/image.jpg')
 */
tool.setBackgroundStyle = function (imagePath, extraStyles = {}) {
	const url = resolveImagePath(imagePath);
	return {
		backgroundImage: url ? `url('${url}')` : 'none',
		backgroundRepeat: 'no-repeat',
		backgroundPosition: 'center',
		backgroundSize: 'cover',
		...extraStyles
	};
}

/**
 * 同步等待方法
 * @param {number} ms - 等待时间（毫秒）
 * @returns {Promise} Promise对象
 * 
 * @example
 * await tool.sleep(1000); // 等待1秒
 * tool.sleep(500).then(() => console.log('等待完成'));
 */
tool.sleep = function(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
};

/**
 * 根据屏幕宽度动态计算字号
 * @param {number} size - 基准字号
 * @returns {number} 计算后的字号
 * 
 * @example
 * tool.autoSize(15) // 根据屏幕宽度计算15px的字号
 */
tool.autoSize = function(size) {
    const baseWidth = 1920; // 设计稿基准宽度
    const scale = window.innerWidth / baseWidth;
    return size * scale;
}

/**
 * 检测设备系统类型
 * @returns {string} 返回设备类型：'android' | 'ios' | 'other'
 * 
 * @example
 * tool.getDeviceType() // 'android' | 'ios' | 'other'
 */
tool.getDeviceType = function() {
    const ua = navigator.userAgent || navigator.vendor || window.opera;
    
    // 检测 Android
    if (/android/i.test(ua)) {
        return 'android';
    }
    
    // 检测 iOS
    if (/iPad|iPhone|iPod/.test(ua) && !window.MSStream) {
        return 'ios';
    }
    
    // 其他设备
    return 'other';
}

/**
 * 判断是否为安卓设备
 * @returns {boolean} 是否为安卓设备
 * 
 * @example
 * tool.isAndroid() // true | false
 */
tool.isAndroid = function() {
    return tool.getDeviceType() === 'android';
}

/**
 * 判断是否为 iOS 设备
 * @returns {boolean} 是否为 iOS 设备
 * 
 * @example
 * tool.isIOS() // true | false
 */
tool.isIOS = function() {
    return tool.getDeviceType() === 'ios';
}

export default tool;