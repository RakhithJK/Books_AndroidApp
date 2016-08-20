package com.books.saumy.books;

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class BookView extends SplitPanel {

    public ViewStateEnum state = ViewStateEnum.books;
    protected String viewedPage;
    protected WebView view;
    protected float swipeOriginX, swipeOriginY;

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState)	{
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.activity_book_view, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle saved) {
        super.onActivityCreated(saved);
        view = (WebView) getView().findViewById(R.id.Viewport);

        // enable JavaScript for cool things to happen!
        view.getSettings().setJavaScriptEnabled(true);

        // ----- SWIPE PAGE
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (state == ViewStateEnum.books)
                    swipePage(v, event, 0);

                WebView view = (WebView) v;
                return view.onTouchEvent(event);
            }
        });

        // ----- NOTE & LINK
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Message msg = new Message();
                msg.setTarget(new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                        String url = msg.getData().getString(
                                getString(R.string.url));
                        if (url != null)
                            navigator.setNote(url, index);
                    }
                });
                view.requestFocusNodeHref(msg);

                return false;
            }
        });

        view.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                try {
                    navigator.setBookPage(url, index);
                } catch (Exception e) {
                    errorMessage(getString(R.string.error_LoadPage));
                }
                return true;
            }
        });

        loadPage(viewedPage);
    }

    public void loadPage(String path)
    {
        viewedPage = path;
        if(created)
            view.loadUrl(path);
    }

    // Change page
    protected void swipePage(View v, MotionEvent event, int book) {
        int action = MotionEventCompat.getActionMasked(event);

        switch (action) {
            case (MotionEvent.ACTION_DOWN):
                swipeOriginX = event.getX();
                swipeOriginY = event.getY();
                break;

            case (MotionEvent.ACTION_UP):
                int quarterWidth = (int) (screenWidth * 0.25);
                float diffX = swipeOriginX - event.getX();
                float diffY = swipeOriginY - event.getY();
                float absDiffX = Math.abs(diffX);
                float absDiffY = Math.abs(diffY);

                if ((diffX > quarterWidth) && (absDiffX > absDiffY)) {
                    try {
                        navigator.goToNextChapter(index);
                    } catch (Exception e) {
                        errorMessage(getString(R.string.error_cannotTurnPage));
                    }
                } else if ((diffX < -quarterWidth) && (absDiffX > absDiffY)) {
                    try {
                        navigator.goToPrevChapter(index);
                    } catch (Exception e) {
                        errorMessage(getString(R.string.error_cannotTurnPage));
                    }
                }
                break;
        }

    }

    @Override
    public void saveState(SharedPreferences.Editor editor) {
        super.saveState(editor);
        editor.putString("state"+index, state.name());
        editor.putString("page"+index, viewedPage);
    }

    @Override
    public void loadState(SharedPreferences preferences)
    {
        super.loadState(preferences);
        loadPage(preferences.getString("page"+index, ""));
        state = ViewStateEnum.valueOf(preferences.getString("state"+index, ViewStateEnum.books.name()));
    }
}
