import { registerPlugin } from '@capacitor/core';

import type { SmsSenderPlugin } from './definitions';

const SmsSender = registerPlugin<SmsSenderPlugin>('SmsSender', {
  web: () => import('./web').then(m => new m.SmsSenderWeb()),
});

export * from './definitions';
export { SmsSender };
