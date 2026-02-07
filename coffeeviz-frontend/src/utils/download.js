/**
 * 下载文件
 * @param {string} content - 文件内容
 * @param {string} filename - 文件名
 * @param {string} mimeType - MIME 类型
 */
export function downloadFile(content, filename, mimeType = 'text/plain') {
  const blob = new Blob([content], { type: mimeType })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
}

/**
 * 下载 Base64 图片
 * @param {string} base64 - Base64 字符串
 * @param {string} filename - 文件名
 */
export function downloadBase64Image(base64, filename) {
  const a = document.createElement('a')
  a.href = base64
  a.download = filename
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
}
