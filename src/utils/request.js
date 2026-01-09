const BASE_URL = 'http://119.29.249.72:8080';

const request = (options) => {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token');
    const isLoginRequest = options.url.includes('/user/user/wxLogin') || 
                           options.url.includes('/user/user/preLogin') ||
                           options.url.includes('/user/user/login');
    
    const header = {
      'Content-Type': 'application/json',
      ...options.header
    };

    if (token) {
      // Don't send token for login endpoints to avoid backend 401 on expired token
      if (!isLoginRequest) {
        header['authentication'] = token;
      }
    }

    if (!token && !isLoginRequest) {
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
          if (res.data.code === 0 || res.data.code === 1 || res.data.code === 200) {
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
          
          // Don't redirect if we are already on login flow endpoints
          if (!isLoginRequest) {
            uni.removeStorageSync('token');
            uni.reLaunch({
              url: '/pages/login/index'
            });
          }
          reject(res);
        } else if (res.statusCode === 404) {
             // 404 is often used for "User Not Found" in login flows, let the caller handle it
             console.warn('[404 Not Found]', options.url)
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
