/*
 * Copyright 2017 Matthew Tamlin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.matthewtamlin.mixtape.library.mixtape_body;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.PopupMenu.OnMenuItemClickListener;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.matthewtamlin.android_utilities.library.helpers.ThemeColorHelper;
import com.matthewtamlin.mixtape.library.R;
import com.matthewtamlin.mixtape.library.data.LibraryItem;
import com.matthewtamlin.mixtape.library.databinders.DataBinder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import timber.log.Timber;

import static com.matthewtamlin.java_utilities.checkers.NullChecker.checkNotNull;

/**
 * A RecyclerView backed partial-implementation of the BodyContract.View interface. This class binds
 * data to the UI using DataBinders, and delegates the appearance of the UI to subclasses.
 */
public abstract class RecyclerBodyView extends FrameLayout implements BodyView {
	/**
	 * All top reached listeners which are currently registered. This set must never contain null.
	 */
	private final Set<TopReachedListener> topReachedListeners = new HashSet<>();

	/**
	 * All library item selected listeners which are currently registered for callbacks. This set
	 * must never contain null.
	 */
	private final Set<LibraryItemSelectedListener> libraryItemSelectedListeners = new HashSet<>();

	/**
	 * All menu item selected listeners which are currently registered for callbacks. This set must
	 * never contain null.
	 */
	private final Set<MenuItemSelectedListener> menuItemSelectedListeners = new HashSet<>();

	/**
	 * The items to display in the recycler view. This member variable must never be null.
	 */
	private List<? extends LibraryItem> data = new ArrayList<>();

	/**
	 * The menu resource of the item specific contextual menus. Default is -1 as specified by
	 * interface.
	 */
	private int contextualMenuResourceId = -1;

	/**
	 * Binds title data to the view holders in the recycler view.
	 */
	private DataBinder<LibraryItem, TextView> titleDataBinder;

	/**
	 * Binds subtitle data to the view holders in the recycler view.
	 */
	private DataBinder<LibraryItem, TextView> subtitleDataBinder;

	/**
	 * Binds artwork data to the view holders in the recycler view.
	 */
	private DataBinder<LibraryItem, ImageView> artworkDataBinder;

	/**
	 * Displays the data list to the user.
	 */
	private RecyclerView recyclerView;

	/**
	 * A progress bar to show when data is being loaded.
	 */
	private ProgressBar loadingIndicator;

	/**
	 * Adapts the data list to the recycler view.
	 */
	private Adapter<BodyViewHolder> adapter;

	/**
	 * Constructs a new RecyclerViewBody.
	 *
	 * @param context
	 * 		the Context the body is attached to, not null
	 */
	public RecyclerBodyView(final Context context) {
		super(context);
		init();
	}

	/**
	 * Constructs a new RecyclerViewBody.
	 *
	 * @param context
	 * 		the Context the body is attached to, not null
	 * @param attrs
	 * 		configuration attributes, null allowed
	 */
	public RecyclerBodyView(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	/**
	 * Constructs a new RecyclerViewBody.
	 *
	 * @param context
	 * 		the Context the body is attached to, not null
	 * @param attrs
	 * 		configuration attributes, null allowed
	 * @param defStyleAttr
	 * 		an attribute in the current theme which supplies default attributes, pass 0	to ignore
	 */
	public RecyclerBodyView(final Context context,
			final AttributeSet attrs,
			final int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	@Override
	public List<? extends LibraryItem> getItems() {
		return data;
	}

	@Override
	public void setItems(final List<? extends LibraryItem> items) {
		data = items == null ? new ArrayList<LibraryItem>() : items;
		adapter.notifyDataSetChanged();
	}

	@Override
	public int getContextualMenuResource() {
		return contextualMenuResourceId;
	}

	@Override
	public void setContextualMenuResource(final int contextualMenuResourceId) {
		// The resource is not used until a contextual menu button is clicked, so just save it
		this.contextualMenuResourceId = contextualMenuResourceId;
	}

	@Override
	public void showItem(final int index) {
		recyclerView.smoothScrollToPosition(index);
	}

	@Override
	public void notifyItemsChanged() {
		adapter.notifyDataSetChanged();
	}

	@Override
	public void notifyItemAdded(final int index) {
		adapter.notifyItemInserted(index);
	}

	@Override
	public void notifyItemRemoved(final int index) {
		adapter.notifyItemRemoved(index);
	}

	@Override
	public void notifyItemModified(final int index) {
		adapter.notifyItemChanged(index);
	}

	@Override
	public void notifyItemMoved(final int initialIndex, final int finalIndex) {
		adapter.notifyItemMoved(initialIndex, finalIndex);
	}

	@Override
	public void showLoadingIndicator(final boolean show) {
		recyclerView.setVisibility(show ? INVISIBLE : VISIBLE);
		loadingIndicator.setVisibility(show ? VISIBLE : GONE);
	}

	@Override
	public boolean loadingIndicatorIsShown() {
		return (loadingIndicator.getVisibility() == VISIBLE);
	}

	@Override
	public void addLibraryItemSelectedListener(final LibraryItemSelectedListener listener) {
		if (listener != null) {
			libraryItemSelectedListeners.add(listener);
		}
	}

	@Override
	public void removeLibraryItemSelectedListener(final LibraryItemSelectedListener listener) {
		libraryItemSelectedListeners.remove(listener);
	}

	@Override
	public void addContextualMenuItemSelectedListener(final MenuItemSelectedListener listener) {
		if (listener != null) {
			menuItemSelectedListeners.add(listener);
		}
	}

	@Override
	public void removeContextualMenuItemSelectedListener(
			final MenuItemSelectedListener listener) {
		menuItemSelectedListeners.remove(listener);
	}

	/**
	 * @return the data binder used to bind titles to the UI, null if there is none
	 */
	public DataBinder<LibraryItem, TextView> getTitleDataBinder() {
		return titleDataBinder;
	}

	/**
	 * Sets the DataBinder to use when binding titles. Changing the data binder results in a full
	 * rebind of the recycler view. This method must be called on the UI thread.
	 *
	 * @param titleDataBinder
	 * 		the DataBinder to use for titles
	 */
	public void setTitleDataBinder(final DataBinder<LibraryItem, TextView> titleDataBinder) {
		// Use reference equality since object equality is hard to define for multithreaded objects
		if (this.titleDataBinder != titleDataBinder) {
			if (this.titleDataBinder != null) {
				this.titleDataBinder.cancelAll();
			}

			this.titleDataBinder = titleDataBinder;
			recyclerView.getAdapter().notifyDataSetChanged(); // Ensures the new data binder is used
		}
	}

	/**
	 * @return the data binder used to bind subtitles to the UI, null if there is none
	 */
	public DataBinder<LibraryItem, TextView> getSubtitleDataBinder() {
		return subtitleDataBinder;
	}

	/**
	 * Sets the DataBinder to use when binding subtitles. Changing the data binder results in a full
	 * rebind of the recycler view. This method must be called on the UI thread.
	 *
	 * @param subtitleDataBinder
	 * 		the DataBinder to use for subtitles
	 */
	public void setSubtitleDataBinder(final DataBinder<LibraryItem, TextView> subtitleDataBinder) {
		// Use reference equality since object equality is hard to define for multithreaded objects
		if (this.subtitleDataBinder != subtitleDataBinder) {
			if (this.subtitleDataBinder != null) {
				this.subtitleDataBinder.cancelAll();
			}

			this.subtitleDataBinder = subtitleDataBinder;
			recyclerView.getAdapter().notifyDataSetChanged(); // Ensures the new data binder is used
		}
	}

	/**
	 * @return the data binder used to bind artwork to the UI, null if there is none
	 */
	public DataBinder<LibraryItem, ImageView> getArtworkDataBinder() {
		return artworkDataBinder;
	}

	/**
	 * Sets the DataBinder to use when binding artwork. Changing the data binder results in a full
	 * rebind of the recycler view. This method must be called on the UI thread.
	 *
	 * @param artworkDataBinder
	 * 		the DataBinder to use for artwork
	 */
	public void setArtworkDataBinder(final DataBinder<LibraryItem, ImageView> artworkDataBinder) {
		// Use reference equality since object equality is hard to define for multithreaded objects
		if (this.artworkDataBinder != artworkDataBinder) {
			if (this.artworkDataBinder != null) {
				this.artworkDataBinder.cancelAll();
			}

			this.artworkDataBinder = artworkDataBinder;
			recyclerView.getAdapter().notifyDataSetChanged(); // Ensures the new data binder is used
		}
	}

	/**
	 * Sets the color of the loading indicator.
	 *
	 * @param color
	 * 		the color to use, as an ARGB hex code
	 */
	public void setLoadingIndicatorColor(final int color) {
		loadingIndicator.getIndeterminateDrawable().setColorFilter(color, android.graphics
				.PorterDuff.Mode.MULTIPLY);
	}

	/**
	 * @return the RecyclerView used to display the data
	 */
	public RecyclerView getRecyclerView() {
		return recyclerView;
	}

	/**
	 * Registers the supplied listener for top reached callbacks. If the supplied listener is null
	 * or is already registered, then the method exits normally.
	 *
	 * @param listener
	 * 		the listener to register
	 */
	public void addTopReachedListener(final TopReachedListener listener) {
		topReachedListeners.add(listener);
	}

	/**
	 * Unregisters the supplied listener from top reached callbacks. If the supplied listener is
	 * null or is not registered, then the method exits normally.
	 *
	 * @param listener
	 * 		the listener to unregister
	 */
	public void removeTopReachedListener(final TopReachedListener listener) {
		topReachedListeners.remove(listener);
	}

	/**
	 * Unregisters all TopReachedListener from this RecyclerViewListener. All listener which are
	 * currently registered will no longer be notified when this view is scrolled to the top.
	 */
	public void clearRegisteredTopReachedListeners() {
		topReachedListeners.clear();
	}

	/**
	 * Sets the color to use when displaying item titles in the UI.
	 *
	 * @param color
	 * 		the color to use, as an ARGB hex code
	 */
	public abstract void setTitleTextColor(final int color);

	/**
	 * Sets the color to use when displaying item subtitles in the UI.
	 *
	 * @param color
	 * 		the color to use, as an ARGB hex code
	 */
	public abstract void setSubtitleTextColor(final int color);

	/**
	 * Sets the color to use for the overflow menu buttons.
	 *
	 * @param color
	 * 		the color to use, as an ARGB hex code
	 */
	public abstract void setOverflowMenuButtonColor(final int color);

	/**
	 * Called when the RecyclerView is created to allow customisation before the adapter is set. The
	 * default implementation does nothing.
	 *
	 * @param recyclerView
	 * 		the RecyclerView which was created, not null
	 */
	protected void onRecyclerViewCreated(final RecyclerView recyclerView) {}

	/**
	 * Called each time data binding completes. The default implementation does nothing.
	 *
	 * @param viewHolder
	 * 		the view holder which data was bound to, not null
	 * @param data
	 * 		the data which was bound to the view holder, not null
	 */
	protected void onViewHolderBound(final BodyViewHolder viewHolder, final LibraryItem data) {}

	/**
	 * Called each time a new BodyViewHolder is required.
	 *
	 * @param parent
	 * 		the ViewGroup the new View will be added to when it is bound to an adapter position
	 * @return a new BodyViewHolder, not null
	 */
	protected abstract BodyViewHolder supplyNewBodyViewHolder(final ViewGroup parent);

	/**
	 * Initialises this view. This method should only be called from a constructor.
	 */
	private void init() {
		// Initialise overall view
		inflate(getContext(), R.layout.reyclerviewbody, this);
		recyclerView = (RecyclerView) findViewById(R.id.recyclerViewBody_recyclerView);
		loadingIndicator = (ProgressBar) findViewById(R.id.recyclerViewBody_progressIndicator);

		// Apply the accent color of the current theme to the loading indicator
		setLoadingIndicatorColor(ThemeColorHelper.getAccentColor(getContext(), Color.GRAY));

		// Configure the recycler view
		onRecyclerViewCreated(recyclerView);
		createAdapter();
		recyclerView.setAdapter(adapter);

		// When the view is scrolled to the top, notify registered listeners
		recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(RecyclerView recyclerView,
					int newState) {
				if (newState == RecyclerView.SCROLL_STATE_IDLE) {
					final LinearLayoutManager llm = (LinearLayoutManager) recyclerView
							.getLayoutManager();

					if (llm.findFirstCompletelyVisibleItemPosition() == 0) {
						for (TopReachedListener listener : topReachedListeners) {
							listener.onTopReached(RecyclerBodyView.this);
						}
					}
				}
			}
		});
	}

	/**
	 * Creates a new recycler view adapter but does not assign it to the recycler view.
	 */
	private void createAdapter() {
		adapter = new Adapter<BodyViewHolder>() {
			@Override
			public BodyViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
				return supplyNewBodyViewHolder(parent);
			}

			@Override
			public void onBindViewHolder(final BodyViewHolder holder, final int position) {
				final LibraryItem dataItem = data.get(holder.getAdapterPosition());

				if (titleDataBinder != null) {
					titleDataBinder.bind(holder.getTitleTextView(), dataItem);
				} else {
					Timber.w("No title data binder set, could not bind title.");
				}

				if (subtitleDataBinder != null) {
					subtitleDataBinder.bind(holder.getSubtitleTextView(), dataItem);
				} else {
					Timber.w("No subtitle data binder set, could not bind subtitle.");
				}

				if (artworkDataBinder != null) {
					artworkDataBinder.bind(holder.getArtworkImageView(), dataItem);
				} else {
					Timber.w("No artwork data binder set, could not bind artwork.");
				}

				holder.getRootView().setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(final View v) {
						for (final LibraryItemSelectedListener listener :
								libraryItemSelectedListeners) {
							listener.onLibraryItemSelected(RecyclerBodyView.this, dataItem);
						}
					}
				});

				final View overflowButton = holder.getContextualMenuButton();
				overflowButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(final View v) {
						// If the resource hasn't been set, inflating the menu will fail
						if (contextualMenuResourceId != -1) {
							showMenu(overflowButton, dataItem);
						}
					}
				});

				// Allow further customisation by subclasses
				onViewHolderBound(holder, dataItem);
			}

			@Override
			public int getItemCount() {
				return data == null ? 0 : data.size();
			}
		};
	}

	/**
	 * Shows a contextual popup menu anchored to the supplied view. Item selections are passed to
	 * the presenter.
	 *
	 * @param anchor
	 * 		the view to anchor the menu to, not null
	 * @param item
	 * 		the LibraryItem associated with the contextual menu, not null
	 */
	private void showMenu(final View anchor, final LibraryItem item) {
		checkNotNull(item, "item cannot be null.");
		checkNotNull(anchor, "overflowButton cannot be null.");

		final PopupMenu menu = new PopupMenu(getContext(), anchor);
		menu.inflate(contextualMenuResourceId);
		menu.show();

		// Propagate menu selections back to the presenter
		menu.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(final MenuItem menuItem) {
				for (final MenuItemSelectedListener listener : menuItemSelectedListeners) {
					listener.onContextualMenuItemSelected(RecyclerBodyView.this, item, menuItem);
				}

				return true;
			}
		});
	}

	/**
	 * Callbacks to be invoked when a RecyclerViewBody is scrolled to the top.
	 */
	public interface TopReachedListener {
		/**
		 * Invoked when a RecyclerViewBody is scrolled to the top.
		 *
		 * @param recyclerBodyView
		 * 		the RecyclerViewBody, not null
		 */
		void onTopReached(RecyclerBodyView recyclerBodyView);
	}
}