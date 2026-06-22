// Central place for all hospital branding, so the name/location appears
// consistently across the app and is easy to change.
export const BRAND = {
  name: 'Multispeciality Hospital',
  short: 'Multispeciality',
  city: 'Hyderabad',
  state: 'Telangana',
  country: 'India',
  get location() {
    return `${this.city}, ${this.state}, ${this.country}`
  },
  tagline: 'Compassionate care across every speciality',
  logo: '🏥',
}
