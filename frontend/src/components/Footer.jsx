import { BRAND } from '../config/brand'

export default function Footer() {
  return (
    <footer className="footer">
      <div className="footer-inner">
        <span className="footer-brand">{BRAND.logo} {BRAND.name}</span>
        <span className="footer-loc">📍 {BRAND.location}</span>
        <span className="footer-tag">{BRAND.tagline}</span>
      </div>
    </footer>
  )
}
