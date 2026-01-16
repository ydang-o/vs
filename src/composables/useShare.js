import { onShareAppMessage, onShareTimeline } from '@dcloudio/uni-app'

export const useShare = (options = {}) => {
  const defaultTitle = '合伙人投票系统'
  
  const resolveOption = (val) => {
    if (typeof val === 'function') return val()
    return val
  }

  onShareAppMessage((res) => {
    return {
      title: resolveOption(options.title) || defaultTitle,
      path: resolveOption(options.path), // If undefined, defaults to current page
      imageUrl: resolveOption(options.imageUrl)
    }
  })

  onShareTimeline(() => {
    return {
      title: resolveOption(options.title) || defaultTitle,
      query: resolveOption(options.query), // Query parameters for the shared page
      imageUrl: resolveOption(options.imageUrl)
    }
  })
}
