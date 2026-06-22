// Captures just the new/changed screens: clean login + change-password page.
import { chromium } from 'playwright'
const BASE = process.env.BASE_URL || 'http://localhost:3000'
const OUT = 'screenshots'

const browser = await chromium.launch()
const page = await browser.newPage({ viewport: { width: 1280, height: 1000 }, deviceScaleFactor: 2 })

// clean login (no prefilled creds, no demo box)
await page.goto(`${BASE}/login`)
await page.waitForSelector('input[type=email]')
await page.waitForTimeout(600)
await page.screenshot({ path: `${OUT}/12-login-clean.png` })
console.log('saved 12-login-clean')

// log in, then open change-password
await page.fill('input[type=email]', 'rakesh@hospital.in')
await page.fill('input[type=password]', 'admin123')
await page.click('button[type=submit]')
await page.waitForSelector('text=Welcome,')
await page.goto(`${BASE}/change-password`)
await page.waitForSelector('text=Change password')
await page.waitForTimeout(500)
await page.screenshot({ path: `${OUT}/13-change-password.png` })
console.log('saved 13-change-password')

await browser.close()
console.log('DONE')
