import { registerPlugin } from '@capacitor/core'
import type { SmsSenderPlugin } from './definitions'

const SmsSender = registerPlugin<SmsSenderPlugin>('SmsSender')

export * from './definitions'
export { SmsSender }
