// Port of shared-user/src/commonMain/.../logic/RecapViewBusinessLogic.kt
export enum PlayerStanding {
  FIRST = "FIRST",
  SECOND = "SECOND",
  THIRD = "THIRD",
}

export interface Player {
  name: string;
  totalStrokes: number;
}

export class RecapViewBusinessLogic {
  sortPlayers(players: Player[] | null): Player[] {
    return [...(players ?? [])].sort((a, b) => a.totalStrokes - b.totalStrokes);
  }

  getMoji(place: PlayerStanding | null): string {
    switch (place) {
      case PlayerStanding.FIRST: return "🥇";
      case PlayerStanding.SECOND: return "🥈";
      case PlayerStanding.THIRD: return "🥉";
      default: return "";
    }
  }

  getImageSize(place: PlayerStanding | null): number {
    switch (place) {
      case PlayerStanding.FIRST: return 70;
      case PlayerStanding.SECOND:
      case PlayerStanding.THIRD: return 40;
      default: return 30;
    }
  }

  formatPlayerName(name: string, place: PlayerStanding | null, onlyPlayer: boolean): string {
    return place != null && !onlyPlayer ? name + this.getMoji(place) : name;
  }
}
