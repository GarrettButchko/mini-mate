// Port of shared-user/src/commonMain/.../logic/SearchResultBusinessLogic.kt
export class SearchResultBusinessLogic {
  readonly retryButtonDelay: number = 3.0;

  distanceInMeters(
    startLatitude: number,
    startLongitude: number,
    endLatitude: number,
    endLongitude: number
  ): number {
    const earthRadiusMeters = 6_371_000.0;
    const deltaLat = ((endLatitude - startLatitude) * Math.PI) / 180.0;
    const deltaLng = ((endLongitude - startLongitude) * Math.PI) / 180.0;
    const a =
      Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
      Math.cos((startLatitude * Math.PI) / 180.0) *
        Math.cos((endLatitude * Math.PI) / 180.0) *
        Math.sin(deltaLng / 2) *
        Math.sin(deltaLng / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return earthRadiusMeters * c;
  }

  metersToMiles(meters: number): number {
    return meters / 1609.34;
  }

  formatDistance(distanceInMiles: number): string {
    const rounded = Math.round(distanceInMiles * 10) / 10;
    const str = rounded.toString();
    return str.includes(".") ? str : `${str}.0`;
  }

  buildSubtitle(distanceInMiles: number, address: string): string {
    return `${this.formatDistance(distanceInMiles)} mi - ${address}`;
  }
}
