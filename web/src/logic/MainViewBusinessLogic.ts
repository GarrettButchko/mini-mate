// Port of shared-user/src/commonMain/.../logic/MainViewBusinessLogic.kt
export class MainViewBusinessLogic {
  readonly proPromotionDisplayDuration: number = 7.0;

  isPlayDisabled(isPro: boolean | null, gameCount: number): boolean {
    return !(isPro ?? false) && gameCount >= 2;
  }

  shouldShowProStopper(isPro: boolean | null, gameCount: number): boolean {
    return this.isPlayDisabled(isPro, gameCount);
  }

  getGreetingName(name: string | null): string {
    return name ?? "User";
  }

  getHeaderTitle(isOnlineMode: boolean): string {
    return isOnlineMode ? "Online Options" : "Start a Round";
  }

  getInfoMessage(isOnlineMode: boolean): string {
    return isOnlineMode
      ? "Host starts a server game. Join connects to an existing one. Multiple devices sync in real time."
      : "Quick starts a local game. Online lets you host or join a networked game.";
  }

  formatWinnerName(name: string | null): string {
    return `${name ?? "N/A"} 🥇`;
  }
}
