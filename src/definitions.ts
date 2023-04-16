export interface SmsSenderPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
