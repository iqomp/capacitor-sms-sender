import { WebPlugin } from '@capacitor/core';

import type { SmsSenderPlugin } from './definitions';

export class SmsSenderWeb extends WebPlugin implements SmsSenderPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
