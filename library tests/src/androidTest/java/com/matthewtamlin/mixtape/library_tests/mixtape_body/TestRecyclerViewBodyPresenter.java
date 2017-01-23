package com.matthewtamlin.mixtape.library_tests.mixtape_body;

import android.support.test.runner.AndroidJUnit4;


import com.matthewtamlin.mixtape.library.base_mvp.ListDataSource;
import com.matthewtamlin.mixtape.library.mixtape_body.RecyclerViewBody;
import com.matthewtamlin.mixtape.library.mixtape_body.RecyclerViewBodyPresenter;
import com.matthewtamlin.mixtape.library.caching.LibraryItemCache;
import com.matthewtamlin.mixtape.library.caching.LruLibraryItemCache;
import com.matthewtamlin.mixtape.library.data.DisplayableDefaults;
import com.matthewtamlin.mixtape.library.data.ImmutableDisplayableDefaults;
import com.matthewtamlin.mixtape.library.data.LibraryItem;
import com.matthewtamlin.mixtape.library.databinders.ArtworkBinder;
import com.matthewtamlin.mixtape.library.databinders.SubtitleBinder;
import com.matthewtamlin.mixtape.library.databinders.TitleBinder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

@RunWith(AndroidJUnit4.class)
public class TestRecyclerViewBodyPresenter extends TestDirectBodyPresenter {
	private TitleBinder titleBinder;
	private SubtitleBinder subtitleBinder;
	private ArtworkBinder artworkBinder;

	private RecyclerViewBodyPresenter presenter;

	@Before
	public void setup() {
		// Create binders
		final LibraryItemCache cache = new LruLibraryItemCache(1000, 1000, 1000);
		final DisplayableDefaults defaults = new ImmutableDisplayableDefaults(null, null, null);
		titleBinder = new TitleBinder(cache, defaults);
		subtitleBinder = new SubtitleBinder(cache, defaults);
		artworkBinder = new ArtworkBinder(cache, defaults, 1);

		// Create a presenter for testing
		presenter = createPresenter();

		super.setup();
	}

	@Test
	public void testGetTitleDataBinder() {
		assertThat("wrong title binder", presenter.getTitleDataBinder(), is(titleBinder));
	}

	@Test
	public void testGetSubtitleDataBinder() {
		assertThat("wrong subtitle binder", presenter.getSubtitleDataBinder(), is(subtitleBinder));
	}

	@Test
	public void testGetArtworkDataBinder() {
		assertThat("wrong artwork binder", presenter.getArtworkDataBinder(), is(artworkBinder));
	}

	@Override
	@SuppressWarnings("unchecked") // This is fine since it's a mock
	protected ListDataSource<LibraryItem> createMockDataSource() {
		return mock(ListDataSource.class);
	}

	@Override
	protected RecyclerViewBody createMockView() {
		return mock(RecyclerViewBody.class);
	}

	@Override
	protected RecyclerViewBodyPresenter<ListDataSource<LibraryItem>> createPresenter() {
		return new RecyclerViewBodyPresenter<>(titleBinder, subtitleBinder, artworkBinder);
	}
}
