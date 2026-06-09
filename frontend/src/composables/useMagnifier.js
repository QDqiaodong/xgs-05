import { ref, reactive, computed } from 'vue'

export function useMagnifier(options) {
  const {
    lensSize = 120,
    magnifierSize = 320,
    defaultMagnification = 2,
    imgRect,
    containerRef,
    emit
  } = options

  const isHovering = ref(false)
  const isMouseOnImage = ref(false)
  const magnification = ref(defaultMagnification)

  const lensPosition = reactive({ x: 0, y: 0 })
  const bgPosition = reactive({ x: 0, y: 0 })
  const lastMousePoint = reactive({ relX: 0, relY: 0 })

  const showMagnifier = computed(() => imgRect.width > 0 && imgRect.height > 0)

  function recalcPositionsByRelPoint(relX, relY) {
    const halfLens = lensSize / 2
    let lensX = relX - halfLens
    let lensY = relY - halfLens

    lensX = Math.max(0, Math.min(lensX, imgRect.width - lensSize))
    lensY = Math.max(0, Math.min(lensY, imgRect.height - lensSize))

    lensPosition.x = lensX + imgRect.offsetLeft
    lensPosition.y = lensY + imgRect.offsetTop

    const ratio = magnification.value
    bgPosition.x = (lensX + halfLens) * ratio - magnifierSize / 2
    bgPosition.y = (lensY + halfLens) * ratio - magnifierSize / 2
  }

  function onMouseEnter() {
    isHovering.value = true
  }

  function onMouseLeave() {
    isHovering.value = false
    isMouseOnImage.value = false
  }

  function onMouseMove(e) {
    if (!containerRef.value) return
    const container = containerRef.value.getBoundingClientRect()
    const mouseX = e.clientX - container.left
    const mouseY = e.clientY - container.top

    const relX = mouseX - imgRect.offsetLeft
    const relY = mouseY - imgRect.offsetTop

    if (
      relX >= 0 &&
      relX <= imgRect.width &&
      relY >= 0 &&
      relY <= imgRect.height
    ) {
      isMouseOnImage.value = true
      lastMousePoint.relX = relX
      lastMousePoint.relY = relY
      recalcPositionsByRelPoint(relX, relY)
    } else {
      isMouseOnImage.value = false
    }
  }

  function setMagnification(m) {
    magnification.value = m
    if (isMouseOnImage.value) {
      recalcPositionsByRelPoint(lastMousePoint.relX, lastMousePoint.relY)
    }
    if (emit) {
      emit('magnification-change', m)
    }
  }

  return {
    isHovering,
    isMouseOnImage,
    magnification,
    lensPosition,
    bgPosition,
    showMagnifier,
    onMouseEnter,
    onMouseLeave,
    onMouseMove,
    setMagnification
  }
}
