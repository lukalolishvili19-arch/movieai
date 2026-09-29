import { useSyncExternalStore } from 'react'

export interface Toast {
  id: number
  message: string
}

let toasts: Toast[] = []
let nextId = 1
const listeners = new Set<() => void>()

function emit() {
  listeners.forEach(l => l())
}

export function showToast(message: string, durationMs = 4000) {
  const id = nextId++
  toasts = [...toasts.filter(t => t.message !== message), { id, message }]
  emit()
  window.setTimeout(() => dismissToast(id), durationMs)
}

export function dismissToast(id: number) {
  toasts = toasts.filter(t => t.id !== id)
  emit()
}

function subscribe(listener: () => void) {
  listeners.add(listener)
  return () => {
    listeners.delete(listener)
  }
}

export function useToasts() {
  return useSyncExternalStore(subscribe, () => toasts, () => toasts)
}
