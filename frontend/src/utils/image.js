const SIZE_SUFFIXES = {
  small: '_small',
  medium: '_medium',
  large: '_large',
  original: '_original'
}

export function getImageUrl(url, size = 'medium') {
  if (!url) return url

  const suffix = SIZE_SUFFIXES[size] || SIZE_SUFFIXES.medium

  const lastDotIndex = url.lastIndexOf('.')
  const lastSlashIndex = url.lastIndexOf('/')

  if (lastDotIndex <= lastSlashIndex) {
    return url
  }

  const base = url.substring(0, lastDotIndex)
  const ext = url.substring(lastDotIndex)

  let cleanBase = base
  const existingSizes = Object.values(SIZE_SUFFIXES)
  for (const s of existingSizes) {
    if (cleanBase.endsWith(s)) {
      cleanBase = cleanBase.substring(0, cleanBase.length - s.length)
      break
    }
  }

  return cleanBase + suffix + ext
}

export function getSmallImage(url) {
  return getImageUrl(url, 'small')
}

export function getMediumImage(url) {
  return getImageUrl(url, 'medium')
}

export function getLargeImage(url) {
  return getImageUrl(url, 'large')
}

export function getOriginalImage(url) {
  return getImageUrl(url, 'original')
}
