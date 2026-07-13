declare module "lob" {
  interface LobOptions {
    apiKey: string;
  }
  interface LetterOptions {
    description?: string;
    to: Record<string, string>;
    from: Record<string, string>;
    file: string;
    color: boolean;
    double_sided: boolean;
    mail_type: string;
  }
  interface Letters {
    create(options: LetterOptions): Promise<unknown>;
  }
  class LobClient {
    constructor(options: LobOptions);
    letters: Letters;
  }
  export = LobClient;
}
