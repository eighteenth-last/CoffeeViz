/**
 * 防抖函数
 * 在事件被触发n秒后再执行回调，如果在这n秒内又被触发，则重新计时
 * 
 * @param {Function} func 要执行的函数
 * @param {number} wait 等待时间（毫秒）
 * @param {boolean} immediate 是否立即执行
 * @returns {Function} 防抖后的函数
 */
export function debounce(func, wait = 300, immediate = false) {
  let timeout
  
  return function executedFunction(...args) {
    const context = this
    
    const later = () => {
      timeout = null
      if (!immediate) func.apply(context, args)
    }
    
    const callNow = immediate && !timeout
    
    clearTimeout(timeout)
    timeout = setTimeout(later, wait)
    
    if (callNow) func.apply(context, args)
  }
}

/**
 * 节流函数
 * 规定在一个单位时间内，只能触发一次函数。如果这个单位时间内触发多次函数，只有一次生效
 * 
 * @param {Function} func 要执行的函数
 * @param {number} wait 等待时间（毫秒）
 * @returns {Function} 节流后的函数
 */
export function throttle(func, wait = 300) {
  let timeout
  let previous = 0
  
  return function executedFunction(...args) {
    const context = this
    const now = Date.now()
    const remaining = wait - (now - previous)
    
    if (remaining <= 0 || remaining > wait) {
      if (timeout) {
        clearTimeout(timeout)
        timeout = null
      }
      previous = now
      func.apply(context, args)
    } else if (!timeout) {
      timeout = setTimeout(() => {
        previous = Date.now()
        timeout = null
        func.apply(context, args)
      }, remaining)
    }
  }
}

/**
 * 创建一个防抖的 ref
 * 用于 Vue 3 Composition API
 * 
 * @param {any} initialValue 初始值
 * @param {number} delay 延迟时间（毫秒）
 * @returns {Object} { value, debouncedValue }
 */
export function useDebouncedRef(initialValue, delay = 300) {
  const value = ref(initialValue)
  const debouncedValue = ref(initialValue)
  
  const updateDebounced = debounce((newValue) => {
    debouncedValue.value = newValue
  }, delay)
  
  watch(value, (newValue) => {
    updateDebounced(newValue)
  })
  
  return { value, debouncedValue }
}
