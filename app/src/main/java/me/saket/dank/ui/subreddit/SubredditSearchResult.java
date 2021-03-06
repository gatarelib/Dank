package me.saket.dank.ui.subreddit;

import com.google.auto.value.AutoValue;

public interface SubredditSearchResult {

  enum Type {
    SUCCESS,
    ERROR_PRIVATE,
    ERROR_NOT_FOUND,
    ERROR_UNKNOWN
  }

  Type type();

  static SubredditSearchResult success(Subscribeable subreddit) {
    return new AutoValue_SubredditSearchResult_Success(subreddit);
  }

  static SubredditSearchResult privateError() {
    return new AutoValue_SubredditSearchResult_Private();
  }

  static SubredditSearchResult notFound() {
    return new AutoValue_SubredditSearchResult_NotFound();
  }

  static SubredditSearchResult unknownError(Throwable error) {
    return new AutoValue_SubredditSearchResult_UnknownError(error);
  }

  @AutoValue
  abstract class Success implements SubredditSearchResult {
    public abstract Subscribeable subscribeable();

    @Override
    public Type type() {
      return Type.SUCCESS;
    }
  }

  @AutoValue
  abstract class Private implements SubredditSearchResult {

    @Override
    public Type type() {
      return Type.ERROR_PRIVATE;
    }
  }

  @AutoValue
  abstract class NotFound implements SubredditSearchResult {

    @Override
    public Type type() {
      return Type.ERROR_NOT_FOUND;
    }
  }

  @AutoValue
  abstract class UnknownError implements SubredditSearchResult {
    public abstract Throwable error();

    @Override
    public Type type() {
      return Type.ERROR_UNKNOWN;
    }
  }
}
