/*
 * Copyright (c) 2023, Daniel Gultsch
 *
 * This file is part of Conversations.
 *
 * Conversations is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Conversations is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Conversations.  If not, see <https://www.gnu.org/licenses/>.
 */

package im.conversations.android.ui.fragment.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagingData;
import com.google.android.material.elevation.SurfaceColors;
import com.google.android.material.search.SearchView;
import im.conversations.android.IDs;
import im.conversations.android.R;
import im.conversations.android.database.model.AccountIdentifier;
import im.conversations.android.database.model.ChatFilter;
import im.conversations.android.database.model.GroupIdentifier;
import im.conversations.android.databinding.FragmentOverviewBinding;
import im.conversations.android.ui.Activities;
import im.conversations.android.ui.Intents;
import im.conversations.android.ui.NavControllers;
import im.conversations.android.ui.activity.SettingsActivity;
import im.conversations.android.ui.activity.SetupActivity;
import im.conversations.android.ui.adapter.ChatOverviewAdapter;
import im.conversations.android.ui.adapter.ChatOverviewComparator;
import im.conversations.android.ui.model.OverviewViewModel;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OverviewFragment extends Fragment {

    private static final Logger LOGGER = LoggerFactory.getLogger(OverviewViewModel.class);

    private FragmentOverviewBinding binding;

    final OnBackPressedCallback drawerLayoutOnBackPressedCallback =
            new OnBackPressedCallback(false) {
                @Override
                public void handleOnBackPressed() {
                    binding.drawerLayout.close();
                }
            };
    final OnBackPressedCallback searchViewOnBackPressedCallback =
            new OnBackPressedCallback(false) {
                @Override
                public void handleOnBackPressed() {
                    binding.searchView.hide();
                }
            };

    private OverviewViewModel overviewViewModel;
    private ChatOverviewAdapter chatOverviewAdapter;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        this.binding =
                DataBindingUtil.inflate(inflater, R.layout.fragment_overview, container, false);
        final ViewModelProvider viewModelProvider =
                new ViewModelProvider(this, getDefaultViewModelProviderFactory());
        this.overviewViewModel = viewModelProvider.get(OverviewViewModel.class);
        binding.setLifecycleOwner(getViewLifecycleOwner());
        binding.searchBar.setNavigationOnClickListener(view -> binding.drawerLayout.open());
        binding.searchView.addTransitionListener(
                (searchView, previousState, newState) -> {
                    final var activity = requireActivity();
                    final var window = activity.getWindow();
                    if (newState == SearchView.TransitionState.SHOWN) {
                        window.setStatusBarColor(SurfaceColors.SURFACE_4.getColor(activity));
                    } else if (newState == SearchView.TransitionState.SHOWING
                            || newState == SearchView.TransitionState.HIDING) {
                        window.setStatusBarColor(SurfaceColors.SURFACE_1.getColor(activity));
                    } else {
                        window.setStatusBarColor(SurfaceColors.SURFACE_0.getColor(activity));
                    }
                    searchViewOnBackPressedCallback.setEnabled(
                            Arrays.asList(
                                            SearchView.TransitionState.SHOWN,
                                            SearchView.TransitionState.SHOWING)
                                    .contains(newState));
                });
        binding.navigationView.setNavigationItemSelectedListener(this::onNavigationItemSelected);
        if (this.overviewViewModel.getChatFilter() == null) {
            binding.navigationView.setCheckedItem(R.id.chats);
        }
        this.overviewViewModel
                .getAccounts()
                .observe(getViewLifecycleOwner(), this::onAccountsUpdated);
        this.overviewViewModel.getGroups().observe(getViewLifecycleOwner(), this::onGroupsUpdated);
        this.overviewViewModel
                .getChatFilterAvailable()
                .observe(getViewLifecycleOwner(), this::onChatFilterAvailable);
        this.configureDrawerLayoutToCloseOnBackPress();
        this.chatOverviewAdapter = new ChatOverviewAdapter(new ChatOverviewComparator());
        binding.chats.setAdapter(chatOverviewAdapter);
        this.overviewViewModel
                .getChats()
                .observe(
                        getViewLifecycleOwner(),
                        pagingData -> {
                            chatOverviewAdapter.submitData(getLifecycle(), pagingData);
                        });
        requireActivity()
                .getOnBackPressedDispatcher()
                .addCallback(getViewLifecycleOwner(), this.searchViewOnBackPressedCallback);
        this.chatOverviewAdapter.setOnChatSelectedListener(this::onChatSelected);
        Activities.setStatusAndNavigationBarColors(requireActivity(), binding.getRoot());
        return binding.getRoot();
    }

    private void onChatSelected(long chatId) {
        final var navController =
                NavControllers.findNavController(requireActivity(), R.id.nav_host_fragment);
        navController.navigate(OverviewFragmentDirections.overviewToChat(chatId));
    }

    @Override
    public void onResume() {
        super.onResume();
        // after rotation (or similar) the drawer layout might get opened in restoreInstanceState
        // therefor we need to check again if we need to enable the callback
        this.drawerLayoutOnBackPressedCallback.setEnabled(this.binding.drawerLayout.isOpen());
        this.searchViewOnBackPressedCallback.setEnabled(this.binding.searchView.isShowing());
    }

    private void configureDrawerLayoutToCloseOnBackPress() {
        this.binding.drawerLayout.addDrawerListener(
                new DrawerLayout.SimpleDrawerListener() {
                    @Override
                    public void onDrawerOpened(final View drawerView) {
                        super.onDrawerOpened(drawerView);
                        drawerLayoutOnBackPressedCallback.setEnabled(true);
                    }

                    @Override
                    public void onDrawerClosed(final View drawerView) {
                        super.onDrawerClosed(drawerView);
                        drawerLayoutOnBackPressedCallback.setEnabled(false);
                    }
                });
        requireActivity()
                .getOnBackPressedDispatcher()
                .addCallback(getViewLifecycleOwner(), this.drawerLayoutOnBackPressedCallback);
    }

    private boolean onNavigationItemSelected(final MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.chats) {
            setChatFilter(null);
            return true;
        }
        if (menuItem.getItemId() == R.id.add_account) {
            return startActivity(SetupActivity.class);
        }
        if (menuItem.getItemId() == R.id.settings) {
            return startActivity(SettingsActivity.class);
        }
        final var intent = menuItem.getIntent();
        if (intent == null) {
            return false;
        }
        setChatFilter(Intents.toChatFilter(intent));
        return true;
    }

    private boolean startActivity(final Class<? extends AppCompatActivity> activityClazz) {
        startActivity(new Intent(requireContext(), activityClazz));
        binding.drawerLayout.close();
        return false;
    }

    private void setChatFilter(final ChatFilter chatFilter) {
        if (Objects.equals(overviewViewModel.getChatFilter(), chatFilter)) {
            LOGGER.debug("Chat filter is already in correct state");
            binding.drawerLayout.close();
            return;
        }
        // this prevents animation between ChatFilter changes
        // TODO This was added primarily to fix the lack of 'scrolling to top' after filter changes
        // (if an item was in both); if we find a better solution we might as well bring back
        // animation
        chatOverviewAdapter.submitData(getLifecycle(), PagingData.empty());
        overviewViewModel.setChatFilter(chatFilter);
        binding.drawerLayout.close();
    }

    private void onChatFilterAvailable(final Boolean available) {
        final var menu = this.binding.navigationView.getMenu();
        final var chatsMenuItem = menu.findItem(R.id.chats);
        if (Boolean.TRUE.equals(available)) {
            chatsMenuItem.setTitle(R.string.all_chats);
        } else {
            chatsMenuItem.setTitle(R.string.chats);
        }
    }

    private void onGroupsUpdated(final List<GroupIdentifier> groups) {
        final var menu = this.binding.navigationView.getMenu();
        final var menuItemSpaces = menu.findItem(R.id.spaces);
        if (groups.isEmpty()) {
            menuItemSpaces.setVisible(false);
            return;
        }
        final var chatFilter = this.overviewViewModel.getChatFilter();
        menuItemSpaces.setVisible(true);
        final var subMenu = menuItemSpaces.getSubMenu();
        subMenu.clear();
        for (final GroupIdentifier group : groups) {
            final var menuItemSpace = subMenu.add(Menu.NONE, IDs.quickInt(), Menu.NONE, group.name);
            menuItemSpace.setCheckable(true);
            menuItemSpace.setIcon(R.drawable.ic_workspaces_24dp);
            menuItemSpace.setIntent(Intents.of(group));
            if (group.equals(chatFilter)) {
                this.binding.navigationView.setCheckedItem(menuItemSpace);
            }
        }
    }

    private void onAccountsUpdated(List<AccountIdentifier> accounts) {
        final var menu = this.binding.navigationView.getMenu();
        final var menuItemAccounts = menu.findItem(R.id.accounts);
        if (accounts.size() <= 1) {
            menuItemAccounts.setVisible(false);
            return;
        }
        final var chatFilter = this.overviewViewModel.getChatFilter();
        menuItemAccounts.setVisible(true);
        final var subMenu = menuItemAccounts.getSubMenu();
        subMenu.clear();
        for (final AccountIdentifier account : accounts) {
            final var menuItemAccount =
                    subMenu.add(Menu.NONE, IDs.quickInt(), Menu.NONE, account.address);
            menuItemAccount.setCheckable(true);
            menuItemAccount.setIcon(R.drawable.ic_person_24dp);
            menuItemAccount.setIntent(Intents.of(account));
            if (account.equals(chatFilter)) {
                this.binding.navigationView.setCheckedItem(menuItemAccount);
            }
        }
    }

    @Override
    public void onDestroyView() {
        nullReferences();
        super.onDestroyView();
    }

    private void nullReferences() {
        this.binding.chats.setAdapter(null);
        this.chatOverviewAdapter = null;
        this.binding = null;
    }
}
