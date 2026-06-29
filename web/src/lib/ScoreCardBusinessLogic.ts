// Port of shared-user/src/commonMain/.../logic/ScoreCardBusinessLogic.kt
export class ScoreCardBusinessLogic {
  getHoleCount(hasCustomPar: boolean | null, courseNumHoles: number | null, gameNumHoles: number): number {
    return hasCustomPar === false && courseNumHoles != null ? courseNumHoles : gameNumHoles;
  }

  calculateTotalPar(pars: (number | null)[]): number {
    return pars.filter((p): p is number => p != null).reduce((a, b) => a + b, 0);
  }

  shouldShowAds(isConnected: boolean, isGuest: boolean, isPro: boolean | null): boolean {
    return isConnected && (isGuest || !(isPro ?? false));
  }

  getBackButtonText(isGuest: boolean): string {
    return isGuest ? "Back to Sign In Menu" : "Go Back to Main Menu";
  }

  getBackButtonIcon(isGuest: boolean): string {
    return isGuest ? "person.crop.circle" : "house.fill";
  }

  formatTimeString(seconds: number): string {
    const minutes = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${minutes}:${String(secs).padStart(2, "0")}`;
  }

  processEndGame(hasUploaded: boolean, persistAction: () => void, markAsUploaded: () => void): void {
    if (!hasUploaded) {
      persistAction();
      markAsUploaded();
    }
  }
}
