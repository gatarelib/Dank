package me.saket.dank.ui.preferences;

import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import me.saket.dank.BuildConfig;
import me.saket.dank.R;
import me.saket.dank.ui.DankPullCollapsibleActivity;
import me.saket.dank.utils.Views;
import me.saket.dank.widgets.InboxUI.ExpandablePageLayout;
import me.saket.dank.widgets.InboxUI.InboxRecyclerView;
import me.saket.dank.widgets.InboxUI.IndependentExpandablePageLayout;

public class UserPreferencesActivity extends DankPullCollapsibleActivity {

  private static final String KEY_INITIAL_PREF_GROUP = "initialPrefGroup";

  @BindView(R.id.userpreferences_root) IndependentExpandablePageLayout activityContentPage;
  @BindView(R.id.toolbar) Toolbar toolbar;
  @BindView(R.id.userpreferences_list) InboxRecyclerView preferenceList;
  @BindView(R.id.userpreferences_preferences_page) ExpandablePageLayout preferencesPage;
  @BindView(R.id.userpreferences_hiddenoptions) Button hiddenOptionsButton;

  private PreferencesFragment preferencesFragment;
  private List<UserPreferenceGroup> userPreferenceGroups;
  private PreferencesAdapter preferencesAdapter;

  public static Intent intent(Context context) {
    return new Intent(context, UserPreferencesActivity.class);
  }

  public static Intent intent(Context context, UserPreferenceGroup initialPreferenceGroup) {
    return new Intent(context, UserPreferencesActivity.class)
        .putExtra(KEY_INITIAL_PREF_GROUP, initialPreferenceGroup);
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    setPullToCollapseEnabled(true);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_user_preferences);
    ButterKnife.bind(this);
    findAndSetupToolbar();

    setupContentExpandablePage(activityContentPage);
    activityContentPage.setNestedExpandablePage(preferencesPage);
    expandFromBelowToolbar();

    activityContentPage.setPullToCollapseIntercepter((event, downX, downY, upwardPagePull) -> {
      //noinspection CodeBlock2Expr
      return Views.touchLiesOn(preferenceList, downX, downY) && preferenceList.canScrollVertically(upwardPagePull ? 1 : -1);
    });
  }

  @Override
  protected void onPostCreate(@Nullable Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);

    userPreferenceGroups = constructPreferenceGroups();

    setupPreferencesFragment();
    setupPreferencesGroupList(savedInstanceState);

    if (getIntent().hasExtra(KEY_INITIAL_PREF_GROUP)) {
      UserPreferenceGroup initialPreferenceGroup = (UserPreferenceGroup) getIntent().getSerializableExtra(KEY_INITIAL_PREF_GROUP);
      preferencesFragment.populatePreferences(initialPreferenceGroup);

      Observable.timer(750, TimeUnit.MILLISECONDS, mainThread())
          .takeUntil(lifecycle().onDestroy())
          .subscribe(o -> preferencesPage.post(() -> {
            int prefPosition = userPreferenceGroups.indexOf(initialPreferenceGroup);
            preferenceList.expandItem(prefPosition, preferencesAdapter.getItemId(prefPosition));
          }));
    }

    hiddenOptionsButton.setVisibility(BuildConfig.DEBUG ? View.VISIBLE : View.GONE);
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    preferenceList.handleOnSaveInstance(outState);
    super.onSaveInstanceState(outState);
  }

  public void onClickPreferencesToolbarUp() {
    preferenceList.collapse();
  }

  private void setupPreferencesFragment() {
    preferencesFragment = (PreferencesFragment) getSupportFragmentManager().findFragmentById(preferencesPage.getId());
    if (preferencesFragment == null) {
      preferencesFragment = PreferencesFragment.create();
    }
    getSupportFragmentManager()
        .beginTransaction()
        .replace(preferencesPage.getId(), preferencesFragment)
        .commitNow();
  }

  private void setupPreferencesGroupList(@Nullable Bundle savedInstanceState) {
    preferenceList.setLayoutManager(preferenceList.createLayoutManager());
    preferenceList.setExpandablePage(preferencesPage, toolbar);
    preferenceList.setHasFixedSize(true);
    if (savedInstanceState != null) {
      preferenceList.handleOnRestoreInstanceState(savedInstanceState);
    }

    preferencesAdapter = new PreferencesAdapter(userPreferenceGroups);
    preferencesAdapter.setOnPreferenceGroupClickListener((preferenceGroup, itemView, groupId) -> {
      preferencesFragment.populatePreferences(preferenceGroup);
      preferencesPage.post(() -> preferenceList.expandItem(preferenceList.indexOfChild(itemView), groupId));
    });
    preferenceList.setAdapter(preferencesAdapter);
  }

  private List<UserPreferenceGroup> constructPreferenceGroups() {
    List<UserPreferenceGroup> preferenceGroups = new ArrayList<>();
    preferenceGroups.add(UserPreferenceGroup.LOOK_AND_FEEL);
    preferenceGroups.add(UserPreferenceGroup.FILTERS);
    preferenceGroups.add(UserPreferenceGroup.DATA_USAGE);
    preferenceGroups.add(UserPreferenceGroup.MISCELLANEOUS);
    preferenceGroups.add(UserPreferenceGroup.ABOUT_DANK);
    return preferenceGroups;
  }

  @OnClick(R.id.userpreferences_hiddenoptions)
  void onClickHiddenOptions() {
    HiddenPreferencesActivity.start(this);
  }

  @Override
  public void onBackPressed() {
    if (preferencesPage.isExpandedOrExpanding()) {
      preferenceList.collapse();
    } else {
      super.onBackPressed();
    }
  }

}
