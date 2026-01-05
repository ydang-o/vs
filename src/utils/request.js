const BASE_URL = 'http://119.29.249.72:8080';

const request = (options) => {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token');
    
    const header = {
      'Content-Type': 'application/json',
      'authentication': token || '', // Custom header as requested
      ...options.header
    };

    if (!token && options.url !== '/user/user/login') {
      console.warn('[Request] Warning: No token found in storage for non-login request:', options.url);
    }

    // Log Request
    console.log(`[Request] ${options.method || 'GET'} ${options.url}`, {
      data: options.data || {},
      header: header,
      tokenLength: token ? token.length : 0
    })

    uni.request({
      url: `${BASE_URL}${options.url}`,
      method: options.method || 'GET',
      data: options.data || {},
      header: header,
      success: (res) => {
        // Log Response
        console.log(`[Response] ${options.url}`, res)

        if (res.statusCode === 200) {
          if (res.data.code === 1 || res.data.code === 200) {
             resolve(res.data);
          } else {
             uni.showToast({
               title: res.data.msg || '请求失败',
               icon: 'none'
             });
             reject(res.data);
          }
        } else if (res.statusCode === 401) {
          console.warn('[401 Unauthorized] Token invalid or expired')
          uni.removeStorageSync('token');
          uni.reLaunch({
            url: '/pages/login/index'
          });
          reject(res);
        } else {
          console.error('[Response Error]', res)
          uni.showToast({
            title: '服务器错误',
            icon: 'none'
          });
          reject(res);
        }
      },
      fail: (err) => {
        console.error('[Network Error]', err)
        uni.showToast({
          title: '网络请求失败',
          icon: 'none'
        });
        reject(err);
      }
    });
  });
};

export default request;
