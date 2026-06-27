// Port of shared-user/src/commonMain/.../logic/JoinViewBusinessLogic.kt
export class JoinViewBusinessLogic {
  formatScannedCode(code: string): string {
    return code.trim().toUpperCase();
  }

  formatEnteredCode(code: string): string {
    return code
      .toUpperCase()
      .split("")
      .filter((c) => /[A-Z0-9]/.test(c))
      .join("")
      .slice(0, 6);
  }

  isJoinButtonDisabled(gameCode: string): boolean {
    return gameCode.length !== 6;
  }

  getJoinButtonOpacity(gameCode: string): number {
    return this.isJoinButtonDisabled(gameCode) ? 0.5 : 1.0;
  }

  getLocationName(name: string | null): string {
    return name ?? "No Location";
  }

  getPlayersHeaderText(playerCount: number): string {
    return `Players: ${playerCount}`;
  }

  canAttemptJoin(gameCode: string): boolean {
    return gameCode.length > 0;
  }

  shouldLeaveGameOnHostDismiss(showHost: boolean, gameId: string, hasStarted: boolean): boolean {
    return !showHost && gameId.length > 0 && !hasStarted;
  }

  shouldNavigateOnGameStart(hasStarted: boolean): boolean {
    return hasStarted;
  }

  shouldResetOnGameDismiss(isDismissed: boolean): boolean {
    return isDismissed;
  }
}
