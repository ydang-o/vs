<script>
export default {
  onLaunch: function () {
    console.log('App Launch')
    // Check login status
    const token = uni.getStorageSync('token');
    if (!token) {
      uni.reLaunch({
        url: '/pages/login/index'
      })
    }
  },
  onShow: function () {
    console.log('App Show')
  },
  onHide: function () {
    console.log('App Hide')
  }
}
</script>

<style>
/* Global CSS */
body {
  background-color: #F8F9FA;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif;
}
.container {
  padding: 20rpx;
}
.btn-primary {
  background-color: #3B82F6;
  color: white;
  border-radius: 8rpx;
}
.btn-primary:active {
  background-color: #2563EB;
}
</style>
