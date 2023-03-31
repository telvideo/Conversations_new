package im.conversations.android.ui.fragment.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagingData;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import im.conversations.android.R;
import im.conversations.android.database.model.MessageWithContentReactions;
import im.conversations.android.databinding.FragmentChatBinding;
import im.conversations.android.ui.Activities;
import im.conversations.android.ui.NavControllers;
import im.conversations.android.ui.RecyclerViewScroller;
import im.conversations.android.ui.adapter.MessageAdapter;
import im.conversations.android.ui.adapter.MessageAdapterItems;
import im.conversations.android.ui.adapter.MessageComparator;
import im.conversations.android.ui.graphics.drawable.FlashBackgroundDrawable;
import im.conversations.android.ui.model.ChatViewModel;
import im.conversations.android.util.MainThreadExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChatFragment extends Fragment {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatFragment.class);

    private FragmentChatBinding binding;
    private ChatViewModel chatViewModel;
    private MessageAdapter messageAdapter;
    private RecyclerViewScroller recyclerViewScroller;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        this.binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chat, container, false);
        final long chatId = ChatFragmentArgs.fromBundle(requireArguments()).getChat();
        final ViewModelProvider viewModelProvider =
                new ViewModelProvider(this, getDefaultViewModelProviderFactory());
        this.chatViewModel = viewModelProvider.get(ChatViewModel.class);
        this.chatViewModel.setChatId(chatId);
        this.binding.setChatViewModel(this.chatViewModel);
        this.binding.setLifecycleOwner(getViewLifecycleOwner());
        final var linearLayoutManager = new LinearLayoutManager(requireContext());
        // linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        this.binding.messages.setLayoutManager(linearLayoutManager);
        this.recyclerViewScroller = new RecyclerViewScroller(this.binding.messages);
        this.messageAdapter = new MessageAdapter(new MessageComparator());
        this.messageAdapter.setOnNavigateToInReplyTo(this::onNavigateToInReplyTo);
        this.binding.messages.setAdapter(this.messageAdapter);

        this.chatViewModel.getMessages().observe(getViewLifecycleOwner(), this::submitPagingData);
        this.chatViewModel
                .isShowDateSeparators()
                .observe(getViewLifecycleOwner(), this::submitPagingData);
        this.binding.materialToolbar.setNavigationOnClickListener(
                view -> {
                    NavControllers.findNavController(requireActivity(), R.id.nav_host_fragment)
                            .popBackStack();
                });
        this.binding.messageLayout.setEndIconOnClickListener(
                v -> {
                    this.scrollToPositionToEnd();
                });
        Activities.setStatusAndNavigationBarColors(requireActivity(), binding.getRoot(), true);
        return this.binding.getRoot();
    }

    private void onNavigateToInReplyTo(long messageId) {
        this.scrollToMessageId(messageId);
    }

    private void submitPagingData(final Boolean isShowDateSeparators) {
        final var pagingData = this.chatViewModel.getMessages().getValue();
        if (pagingData == null) {
            LOGGER.info("PagingData not ready");
            return;
        }
        this.submitPagingData(pagingData, Boolean.TRUE.equals(isShowDateSeparators));
    }

    private void submitPagingData(final PagingData<MessageWithContentReactions> pagingData) {
        submitPagingData(
                pagingData,
                Boolean.TRUE.equals(this.chatViewModel.isShowDateSeparators().getValue()));
    }

    private void submitPagingData(
            final PagingData<MessageWithContentReactions> pagingData,
            final boolean insertSeparators) {
        if (insertSeparators) {
            messageAdapter.submitData(
                    getLifecycle(), MessageAdapterItems.insertSeparators(pagingData));
        } else {
            messageAdapter.submitData(getLifecycle(), MessageAdapterItems.of(pagingData));
        }
    }

    private void scrollToPositionToEnd() {
        this.recyclerViewScroller.scrollToPosition(0);
    }

    private void scrollToMessageId(final long messageId) {
        // TODO do not scroll if view is fully visible
        LOGGER.info("scrollToMessageId({})", messageId);
        this.chatViewModel.setShowDateSeparators(false);
        final var future = this.chatViewModel.getMessagePosition(messageId);
        Futures.addCallback(
                future,
                new FutureCallback<>() {
                    @Override
                    public void onSuccess(final @NonNull Integer position) {
                        recyclerViewScroller.scrollToPosition(
                                position,
                                () -> {
                                    chatViewModel.setShowDateSeparators(true);
                                    flashBackgroundAtPosition(position, messageId);
                                });
                    }

                    @Override
                    public void onFailure(@NonNull final Throwable throwable) {
                        LOGGER.info("Could not scroll to {}", messageId, throwable);
                        chatViewModel.setShowDateSeparators(true);
                    }
                },
                MainThreadExecutor.getInstance());
    }

    private void flashBackgroundAtPosition(final int position, final long messageId) {
        final var layoutManager = this.binding.messages.getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager llm) {
            final var view = llm.findViewByPosition(position);
            if (view == null) {
                return;
            }
            FlashBackgroundDrawable.flashBackground(view, messageId);
        }
    }
}
