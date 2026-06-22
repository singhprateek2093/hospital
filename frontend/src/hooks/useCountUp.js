import { useEffect, useRef, useState } from 'react'

/**
 * Animates a number from 0 up to `target` over `duration` ms using
 * requestAnimationFrame with an ease-out curve. Returns the current value.
 * Used for the dashboard stat tiles so the counts "tick up" on load.
 */
export default function useCountUp(target, duration = 900) {
  const [value, setValue] = useState(0)
  const startRef = useRef(null)

  useEffect(() => {
    if (target == null) return
    let frame
    const animate = (ts) => {
      if (startRef.current == null) startRef.current = ts
      const elapsed = ts - startRef.current
      const progress = Math.min(elapsed / duration, 1)
      const eased = 1 - Math.pow(1 - progress, 3) // ease-out cubic
      setValue(Math.round(eased * target))
      if (progress < 1) frame = requestAnimationFrame(animate)
    }
    frame = requestAnimationFrame(animate)
    return () => cancelAnimationFrame(frame)
  }, [target, duration])

  return value
}
