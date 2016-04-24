package com.example.xyzreader.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>  {

    private static final String FRAGMENT_TAG = "ArticleDetailFragment";

    public static final String ARG_ITEM_ID = "item_id";
    private static final String LOG_TAG = ArticleDetailFragment.class.getSimpleName();

    private Cursor mCursor;
    private long mItemId;

    @Bind(R.id.photo)
    DynamicHeightNetworkImageView mPhotoView;
    @Bind(R.id.article_title)
    TextView mTitleView;
    @Bind(R.id.article_byline)
    TextView mByLineView;
    @Bind(R.id.article_body)
    TextView mBodyView;
    @Bind(R.id.collapsing_toolbar)
    CollapsingToolbarLayout mCollapsingToolbar;
    @Bind(R.id.appbar)
    AppBarLayout mAppbarLayout;
    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.detail_info_container)
    View mDetailInfoContainer;
    @Bind(R.id.title_collapsed)
    TextView mTitleCollapsed;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailFragment() {
    }

    public static ArticleDetailFragment newInstance(long itemId) {
        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItemId = getArguments().getLong(ARG_ITEM_ID);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // In support library r8, calling initLoader for a fragment in a FragmentPagerAdapter in
        // the fragment's onCreate may cause the same LoaderManager to be dealt to multiple
        // fragments because their mIndex is -1 (haven't been added to the activity yet). Thus,
        // we do this in onActivityCreated.
        getLoaderManager().initLoader(0, null, this);
    }

    private void setUpHomeUp() {
        Activity activity = getActivity();
        if(activity != null && activity instanceof AppCompatActivity) {
            ((AppCompatActivity) activity).setSupportActionBar(mToolbar);
            ActionBar actionBar = ((AppCompatActivity) activity).getSupportActionBar();
            if(actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setDisplayShowTitleEnabled(false);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_article_detail, container, false);
        ButterKnife.bind(this, rootView);

        mCollapsingToolbar.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));

        mAppbarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                int ctheight = mCollapsingToolbar.getHeight();
                int minHeight = (int) (2.5 * ViewCompat.getMinimumHeight(mCollapsingToolbar));

                // Height is less than the toolbar's doubled size. Disappear main text
                if((ctheight + verticalOffset) < minHeight) {
                    mDetailInfoContainer.animate().alpha(0).setDuration(50);
                    mDetailInfoContainer.setVisibility(View.GONE);
                    mTitleCollapsed.animate().alpha(1).setDuration(300);
                    mTitleCollapsed.setVisibility(View.VISIBLE);
//                    mCollapsingToolbar.setTitleEnabled(true);
                } else {
                    mDetailInfoContainer.animate().alpha(1).setDuration(300);
                    mDetailInfoContainer.setVisibility(View.VISIBLE);
                    mTitleCollapsed.animate().alpha(0).setDuration(50);
                    mTitleCollapsed.setVisibility(View.GONE);
//                    mCollapsingToolbar.setTitleEnabled(false);
                }
            }
        });

//        mRootView.findViewById(R.id.share_fab).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
//                        .setType("text/plain")
//                        .setText("Some sample text")
//                        .getIntent(), getString(R.string.action_share)));
//            }
//        });

        // in onCreateView: adjust toolbar padding
        final int initialToolbarHeight = mToolbar.getLayoutParams().height;
        final int initialStatusBarHeight = getStatusBarHeight();
        mToolbar.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int[] locToolbar = new int[2];
                mToolbar.getLocationOnScreen(locToolbar);
                int yToolbar = locToolbar[1];
                int topPaddingToolbar = mToolbar.getPaddingTop();
                if (isAdded()) {
                    //normal case : system status bar padding on toolbar : yToolbar = initialStatusBarHeight && topPaddingToolbar = 0
                    //abnormal case : no system status bar padding on toolbar -> toolbar behind status bar => add custom padding
                    if (yToolbar != initialStatusBarHeight && topPaddingToolbar == 0) {
                        mToolbar.setPadding(0, initialStatusBarHeight, 0, 0);
                        mToolbar.getLayoutParams().height = initialToolbarHeight + initialStatusBarHeight;
                    }
                    //abnormal case : system status bar padding and custom padding on toolbar -> toolbar with padding too large => remove custom padding
                    else if (yToolbar == initialStatusBarHeight && topPaddingToolbar == initialStatusBarHeight) {
                        mToolbar.setPadding(0, 0, 0, 0);
                        mToolbar.getLayoutParams().height = initialToolbarHeight;
                    }
                }
            }
        });

//        setUpHomeUp();
        bindViews();

        return rootView;
    }

    private void bindViews() {

        mByLineView.setMovementMethod(new LinkMovementMethod());
        mBodyView.setTypeface(Typeface.createFromAsset(getResources().getAssets(), "Rosario-Regular.ttf"));

        if (mCursor != null) {
            String title = mCursor.getString(ArticleLoader.Query.TITLE);
//            mCollapsingToolbar.setTitle(title);
            mTitleCollapsed.setText(title);
            mTitleView.setText(title);

            mByLineView.setText(Html.fromHtml(
                    DateUtils.getRelativeTimeSpanString(
                            mCursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL).toString()
                            + " by <font color='#ffffff'>"
                            + mCursor.getString(ArticleLoader.Query.AUTHOR)
                            + "</font>"));

            mBodyView.setText(Html.fromHtml(mCursor.getString(ArticleLoader.Query.BODY)));

            String url = mCursor.getString(ArticleLoader.Query.PHOTO_URL);

            mPhotoView.setAspectRatio(mCursor.getFloat(ArticleLoader.Query.ASPECT_RATIO));
            int placeholderColor = mCursor.getInt(ArticleLoader.Query.COLOR_PLACEHOLDER);
            mCollapsingToolbar.setStatusBarScrimColor(placeholderColor);
            mCollapsingToolbar.setContentScrimColor(placeholderColor);
            mCollapsingToolbar.setBackgroundColor(placeholderColor);

            Glide.with(this).
                    load(url)
                    .asBitmap()
                    .into(mPhotoView);

        } else {
            mTitleView.setText("N/A");
            mByLineView.setText("N/A" );
            mBodyView.setText("N/A");
        }
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newInstanceForItemId(getActivity(), mItemId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (!isAdded()) {
            if (cursor != null) {
                cursor.close();
            }
            return;
        }

        mCursor = cursor;
        if (mCursor != null && !mCursor.moveToFirst()) {
            Log.e(FRAGMENT_TAG, "Error reading item detail cursor");
            mCursor.close();
            mCursor = null;
        }

        bindViews();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
        bindViews();
    }


    public void onBindToolbar() {
        Activity activity = getActivity();
        if(activity != null && activity instanceof OnArticleDetailFragment) {
            ((OnArticleDetailFragment) activity).onAttachToolbar(mToolbar, mItemId);
        }
    }

    public interface OnArticleDetailFragment {
        void onAttachToolbar(Toolbar mToolbar, long itemId);
    }
}
