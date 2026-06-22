// Drives the running app in a real browser and saves screenshots of every screen,
// for both an ADMIN and a DOCTOR, to ./screenshots/.
//
// Usage: node scripts/screenshots.mjs   (app must be running on :5173 / :8080)
import { chromium } from 'playwright'
import { mkdirSync } from 'fs'

const BASE = process.env.BASE_URL || 'http://localhost:5173'
const OUT = 'screenshots'
mkdirSync(OUT, { recursive: true })

const browser = await chromium.launch()
const page = await browser.newPage({ viewport: { width: 1280, height: 1000 }, deviceScaleFactor: 2 })

async function shot(name) {
  await page.waitForTimeout(600) // let data settle
  await page.screenshot({ path: `${OUT}/${name}.png` }) // viewport only (reliable headless)
  console.log('saved', name)
}

async function login(email, password) {
  await page.goto(`${BASE}/login`)
  await page.waitForSelector('input[type=email]')
  await page.fill('input[type=email]', email)
  await page.fill('input[type=password]', password)
  await page.click('button[type=submit]')
  await page.waitForSelector('text=Welcome,') // dashboard loaded
}

async function logout() {
  // Clear the stored JWT directly — more robust than driving the navbar.
  await page.goto(`${BASE}/`)
  await page.evaluate(() => localStorage.removeItem('token'))
  await page.goto(`${BASE}/login`)
  await page.waitForSelector('input[type=email]')
}

// ---------- not logged in ----------
await page.goto(`${BASE}/login`)
await page.waitForSelector('input[type=email]')
await shot('01-login')

// ---------- ADMIN ----------
await login('rakesh@hospital.in', 'admin123')
await shot('02-admin-dashboard')

await page.goto(`${BASE}/patients`)
await page.waitForSelector('table')
await shot('03-admin-patients')

// open the first patient
await page.goto(`${BASE}/patients/1`)
await page.waitForSelector('text=Intake history')
await shot('04-admin-patient-detail')

await page.goto(`${BASE}/patients/new`)
await page.waitForSelector('text=Register patient')
await shot('05-admin-new-patient')

await page.goto(`${BASE}/appointments`)
await page.waitForSelector('table')
await shot('06-admin-appointments')

await page.goto(`${BASE}/appointments/new`)
await page.waitForSelector('text=Book appointment')
await shot('07-admin-new-appointment')

await page.goto(`${BASE}/register`)
await page.waitForSelector('text=Add user')
await shot('08-admin-register')

// ---------- DOCTOR (restricted view) ----------
await logout()
await login('kamlesh@hospital.in', 'doctor123')
await shot('09-doctor-dashboard')

await page.goto(`${BASE}/patients`)
await page.waitForSelector('table')
await shot('10-doctor-patients')

await page.goto(`${BASE}/appointments`)
await page.waitForSelector('table')
await shot('11-doctor-appointments')

await browser.close()
console.log('DONE')
