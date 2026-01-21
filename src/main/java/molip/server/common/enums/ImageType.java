package molip.server.common.enums;

public enum ImageType {
  USERS("users"),
  REFLECTIONS("reflections"),
  MESSAGES("messages");

  private final String folder;

  ImageType(String folder) {
    this.folder = folder;
  }

  public String folder() {
    return folder;
  }
}
