type LogTag = string;

function format(marker: string, tag: LogTag, message: string): string {
  return `${marker} [${tag}] ${message}`;
}

export const logger = {
  error(tag: LogTag, message: string, err?: unknown) {
    console.error(format("❌", tag, message), err ?? "");
  },
  warning(tag: LogTag, message: string) {
    console.warn(format("⚠️", tag, message));
  },
  success(tag: LogTag, message: string) {
    console.log(format("✅", tag, message));
  },
  info(tag: LogTag, message: string) {
    console.log(format("ℹ️", tag, message));
  },
  debug(tag: LogTag, message: string) {
    console.debug(format("🔧", tag, message));
  },
};
