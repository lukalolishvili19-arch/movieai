import { describe, expect, it } from 'vitest'
import { ageOn, formatDate, formatRuntime, formatShortDate, initials } from './format'

describe('format helpers', () => {
  it('formats calendar dates without timezone shifts', () => {
    expect(formatDate('2024-03-01')).toBe('March 1, 2024')
    expect(formatShortDate('2026-09-18')).toBe('Sep 18')
  })

  it('treats missing dates as unavailable rather than inventing one', () => {
    expect(formatDate(null)).toBeNull()
    expect(formatDate('')).toBeNull()
    expect(formatShortDate(undefined)).toBeNull()
  })

  it('formats runtimes', () => {
    expect(formatRuntime(166)).toBe('2h 46m')
    expect(formatRuntime(45)).toBe('45m')
    expect(formatRuntime(null)).toBeNull()
    expect(formatRuntime(0)).toBeNull()
  })

  it('computes age at death or today', () => {
    expect(ageOn('1950-06-15', '2000-06-14')).toBe(49)
    expect(ageOn('1950-06-15', '2000-06-15')).toBe(50)
    expect(ageOn(null, null)).toBeNull()
  })

  it('builds initials', () => {
    expect(initials('Timothée Chalamet')).toBe('TC')
    expect(initials('Zendaya')).toBe('Z')
  })
})
