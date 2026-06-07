import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

function generateId() {
  return Date.now().toString(36) + Math.random().toString(36).substr(2)
}

export const useInspirationStore = defineStore('inspiration', () => {
  const boards = ref([])
  const activeBoardId = ref(null)

  const defaultBoards = [
    {
      id: generateId(),
      name: '我的灵感',
      color: '#667eea',
      createdAt: Date.now(),
      items: []
    }
  ]

  function init() {
    const stored = localStorage.getItem('inspiration-boards')
    if (stored) {
      try {
        boards.value = JSON.parse(stored)
      } catch (e) {
        boards.value = JSON.parse(JSON.stringify(defaultBoards))
      }
    } else {
      boards.value = JSON.parse(JSON.stringify(defaultBoards))
    }
    if (boards.value.length > 0 && !activeBoardId.value) {
      activeBoardId.value = boards.value[0].id
    }
    saveToStorage()
  }

  function saveToStorage() {
    localStorage.setItem('inspiration-boards', JSON.stringify(boards.value))
  }

  const activeBoard = ref(null)
  watch([boards, activeBoardId], () => {
    activeBoard.value = boards.value.find(b => b.id === activeBoardId.value) || null
  }, { immediate: true, deep: true })

  watch(boards, () => {
    saveToStorage()
  }, { deep: true })

  function createBoard(name, color = '#667eea') {
    const newBoard = {
      id: generateId(),
      name: name || '新画板',
      color,
      createdAt: Date.now(),
      items: []
    }
    boards.value.push(newBoard)
    activeBoardId.value = newBoard.id
    return newBoard
  }

  function deleteBoard(boardId) {
    const idx = boards.value.findIndex(b => b.id === boardId)
    if (idx === -1) return
    boards.value.splice(idx, 1)
    if (activeBoardId.value === boardId) {
      activeBoardId.value = boards.value.length > 0 ? boards.value[0].id : null
    }
  }

  function renameBoard(boardId, name) {
    const board = boards.value.find(b => b.id === boardId)
    if (board) {
      board.name = name
    }
  }

  function setActiveBoard(boardId) {
    activeBoardId.value = boardId
  }

  function addWorkCard(work, x = 100, y = 100) {
    if (!activeBoard.value) return
    const newItem = {
      id: generateId(),
      type: 'work',
      x,
      y,
      width: 220,
      height: 300,
      work: {
        id: work.id,
        title: work.title,
        description: work.description,
        coverImage: work.coverImage,
        authorId: work.authorId,
        authorName: work.authorName,
        authorAvatar: work.authorAvatar
      }
    }
    activeBoard.value.items.push(newItem)
    return newItem
  }

  function addNote(x = 100, y = 100) {
    if (!activeBoard.value) return
    const colors = ['#fff3cd', '#d4edda', '#d1ecf1', '#f8d7da', '#e2d8f5']
    const color = colors[Math.floor(Math.random() * colors.length)]
    const newItem = {
      id: generateId(),
      type: 'note',
      x,
      y,
      width: 200,
      height: 160,
      color,
      text: ''
    }
    activeBoard.value.items.push(newItem)
    return newItem
  }

  function updateItem(itemId, updates) {
    if (!activeBoard.value) return
    const item = activeBoard.value.items.find(i => i.id === itemId)
    if (item) {
      Object.assign(item, updates)
    }
  }

  function deleteItem(itemId) {
    if (!activeBoard.value) return
    const idx = activeBoard.value.items.findIndex(i => i.id === itemId)
    if (idx !== -1) {
      activeBoard.value.items.splice(idx, 1)
    }
  }

  function bringToFront(itemId) {
    if (!activeBoard.value) return
    const idx = activeBoard.value.items.findIndex(i => i.id === itemId)
    if (idx !== -1 && idx < activeBoard.value.items.length - 1) {
      const [item] = activeBoard.value.items.splice(idx, 1)
      activeBoard.value.items.push(item)
    }
  }

  return {
    boards,
    activeBoardId,
    activeBoard,
    init,
    createBoard,
    deleteBoard,
    renameBoard,
    setActiveBoard,
    addWorkCard,
    addNote,
    updateItem,
    deleteItem,
    bringToFront
  }
})
