/**
 * 微信位置服务工具类
 * 封装微信SDK获取位置和逆地理编码功能
 */
import http from '@/utils/request';

export class WeChatLocationService {
    constructor() {
        this.wechatSDKConfigured = false;
    }

    // 检查是否在微信浏览器中
    isWeChatBrowser() {
        return /micromessenger/i.test(navigator.userAgent);
    }

    // 等待微信SDK加载
    async waitForWeChatSDK() {
        return new Promise((resolve, reject) => {
            if (window.wx) {
                resolve();
                return;
            }

            const checkInterval = setInterval(() => {
                if (window.wx) {
                    clearInterval(checkInterval);
                    resolve();
                }
            }, 100);

            setTimeout(() => {
                clearInterval(checkInterval);
                reject(new Error('微信 SDK 加载超时'));
            }, 10000);
        });
    }

    // 等待URL稳定
    async waitForStableUrl(timeout = 3000) {
        await new Promise(resolve => setTimeout(resolve, 500));

        let lastUrl = window.location.href.split('#')[0];
        let stableCount = 0;
        const requiredStableCount = 3;

        return new Promise((resolve) => {
            const startTime = Date.now();
            const checkInterval = setInterval(() => {
                const currentUrl = window.location.href.split('#')[0];
                
                if (currentUrl === lastUrl) {
                    stableCount++;
                    if (stableCount >= requiredStableCount) {
                        clearInterval(checkInterval);
                        resolve(currentUrl);
                    }
                } else {
                    stableCount = 0;
                    lastUrl = currentUrl;
                }

                if (Date.now() - startTime > timeout) {
                    clearInterval(checkInterval);
                    resolve(lastUrl);
                }
            }, 100);
        });
    }

    // 配置微信SDK
    async configWeChatSDK(appId, timestamp, nonceStr, signature) {
        return new Promise((resolve, reject) => {
            window.wx.config({
                debug: false,
                appId: appId,
                timestamp: timestamp,
                nonceStr: nonceStr,
                signature: signature,
                jsApiList: ['getLocation']
            });

            const timeout = setTimeout(() => {
                reject(new Error('微信 JS-SDK 配置超时'));
            }, 8000);

            window.wx.ready(() => {
                clearTimeout(timeout);
                this.wechatSDKConfigured = true;
                resolve();
            });
            window.wx.error((res) => {
                clearTimeout(timeout);
                this.wechatSDKConfigured = false;
                reject(new Error('微信 JS-SDK 配置失败: ' + res.errMsg));
            });
        });
    }

    // 获取位置（带重试机制）
    async getLocationWithRetry(maxRetries = 2) {
        let lastError;
        for (let i = 0; i <= maxRetries; i++) {
            try {
                if (i === 0) {
                    await new Promise(resolve => setTimeout(resolve, 800));
                } else {
                    await new Promise(resolve => setTimeout(resolve, 500));
                }

                return await new Promise((resolve, reject) => {
                    const timeout = setTimeout(() => {
                        reject(new Error('获取位置超时'));
                    }, 10000);

                    window.wx.getLocation({
                        type: 'gcj02',
                        success: (res) => {
                            clearTimeout(timeout);
                            resolve({
                                longitude: res.longitude,
                                latitude: res.latitude
                            });
                        },
                        fail: (err) => {
                            clearTimeout(timeout);
                            if (err.errMsg && (err.errMsg.includes('invalid signature') || err.errMsg.includes('invalid url domain'))) {
                                this.wechatSDKConfigured = false;
                            }
                            reject(new Error(err.errMsg || '获取位置失败'));
                        }
                    });
                });
            } catch (error) {
                lastError = error;
                if (error.message && (error.message.includes('invalid signature') || error.message.includes('invalid url domain'))) {
                    this.wechatSDKConfigured = false;
                    if (i < maxRetries) {
                        try {
                            const stableUrl = await this.waitForStableUrl(2000);
                            const response = await http.get('/admin-api/wechat/sign', { url: stableUrl });
                            if (response.code === 0) {
                                const { appId, timestamp, nonceStr, signature } = response.data;
                                await this.configWeChatSDK(appId, timestamp, nonceStr, signature);
                            }
                        } catch (configError) {
                            // 配置失败，继续重试
                        }
                    }
                }
            }
        }
        throw lastError;
    }

    // 获取当前位置（完整流程）
    async getCurrentLocation(silentMode = false) {
        try {
            if (!this.isWeChatBrowser()) {
                if (!silentMode) {
                    throw new Error('请在微信浏览器中打开');
                }
                return null;
            }

            await this.waitForWeChatSDK();
            const stableUrl = await this.waitForStableUrl();
            const currentUrlForConfig = window.location.href.split('#')[0];
            const needReconfig = !this.wechatSDKConfigured || currentUrlForConfig !== stableUrl;

            if (needReconfig) {
                const response = await http.get('/admin-api/wechat/sign', { url: stableUrl });
                if (response.code !== 0) {
                    throw new Error(response.msg || '获取签名失败');
                }

                const { appId, timestamp, nonceStr, signature } = response.data;
                const finalUrl = window.location.href.split('#')[0];
                
                if (finalUrl !== stableUrl) {
                    const retryResponse = await http.get('/admin-api/wechat/sign', { url: finalUrl });
                    if (retryResponse.code !== 0) {
                        throw new Error(retryResponse.msg || '获取签名失败');
                    }
                    const retryData = retryResponse.data;
                    await this.configWeChatSDK(retryData.appId, retryData.timestamp, retryData.nonceStr, retryData.signature);
                } else {
                    await this.configWeChatSDK(appId, timestamp, nonceStr, signature);
                }
            } else {
                await new Promise(resolve => setTimeout(resolve, 200));
            }

            const wechatLocation = await this.getLocationWithRetry();
            return wechatLocation;
        } catch (error) {
            if (silentMode) {
                try {
                    await new Promise(resolve => setTimeout(resolve, 1000));
                    this.wechatSDKConfigured = false;
                    return await this.getCurrentLocation(false);
                } catch (retryError) {
                    return null;
                }
            }
            throw error;
        }
    }

    // 根据经纬度获取地址信息（逆地理编码）
    async getAddressFromCoordinates(longitude, latitude) {
        try {
            const amapKey = window.getConfig('amapKey');
            
            if (!amapKey) {
                return `${longitude.toFixed(6)}, ${latitude.toFixed(6)}`;
            }
            
            const location = `${longitude.toFixed(6)},${latitude.toFixed(6)}`;
            const url = `https://restapi.amap.com/v3/geocode/regeo?key=${amapKey}&location=${location}&output=json`;
            
            const response = await fetch(url);
            const data = await response.json();
            
            if (data && data.status === '1' && data.regeocode && data.regeocode.formatted_address) {
                const address = this.removeProvinceCity(data.regeocode.formatted_address);
                return address || data.regeocode.formatted_address;
            }
            
            return `${longitude.toFixed(6)}, ${latitude.toFixed(6)}`;
        } catch (error) {
            return `${longitude.toFixed(6)}, ${latitude.toFixed(6)}`;
        }
    }

    // 去除地址中的省市信息
    removeProvinceCity(address) {
        if (!address || typeof address !== 'string') {
            return address;
        }
        
        let processedAddress = address.trim();
        let previousLength = processedAddress.length;
        let maxIterations = 10;
        let iterations = 0;
        
        while (iterations < maxIterations) {
            const beforeReplace = processedAddress;
            
            processedAddress = processedAddress.replace(/^[^省]+省/, '');
            processedAddress = processedAddress.replace(/^[^市]+市/, '');
            processedAddress = processedAddress.replace(/^[\s,，、]+/, '').trim();
            
            if (processedAddress === beforeReplace || processedAddress.length === previousLength) {
                break;
            }
            
            previousLength = processedAddress.length;
            iterations++;
        }
        
        return processedAddress || address;
    }
}

// 导出单例
export default new WeChatLocationService();
