import { reactive, onMounted, onBeforeUnmount } from 'vue'

export function useImageRect(imageRef, containerRef) {
  const imgRect = reactive({
    width: 0,
    height: 0,
    offsetLeft: 0,
    offsetTop: 0
  })

  function updateImageRect() {
    if (!imageRef.value || !containerRef.value) return
    const img = imageRef.value.getBoundingClientRect()
    const container = containerRef.value.getBoundingClientRect()
    imgRect.width = img.width
    imgRect.height = img.height
    imgRect.offsetLeft = img.left - container.left
    imgRect.offsetTop = img.top - container.top
  }

  function onResize() {
    updateImageRect()
  }

  onMounted(() => {
    window.addEventListener('resize', onResize)
  })

  onBeforeUnmount(() => {
    window.removeEventListener('resize', onResize)
  })

  return {
    imgRect,
    updateImageRect,
    onResize
  }
}
