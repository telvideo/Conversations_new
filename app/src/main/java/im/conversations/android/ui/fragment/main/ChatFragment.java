package im.conversations.android.ui.fragment.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import im.conversations.android.R;
import im.conversations.android.databinding.FragmentChatBinding;
import im.conversations.android.ui.Activities;
import im.conversations.android.ui.NavControllers;
import im.conversations.android.ui.RecyclerViewScroller;
import im.conversations.android.ui.adapter.MessageAdapter;
import im.conversations.android.ui.adapter.MessageComparator;
import im.conversations.android.ui.model.ChatViewModel;
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
        this.binding.messages.setAdapter(this.messageAdapter);

        this.chatViewModel
                .getMessages()
                .observe(
                        getViewLifecycleOwner(),
                        pagingData -> messageAdapter.submitData(getLifecycle(), pagingData));
        this.binding.materialToolbar.setNavigationOnClickListener(
                view -> {
                    NavControllers.findNavController(requireActivity(), R.id.nav_host_fragment)
                            .popBackStack();
                });
        this.binding.addContent.setOnClickListener(
                v -> {
                    scrollToPosition(messageAdapter.getItemCount() - 1);
                });
        this.binding.messageLayout.setEndIconOnClickListener(
                v -> {
                    scrollToPosition(0);
                });
        Activities.setStatusAndNavigationBarColors(requireActivity(), binding.getRoot(), true);
        return this.binding.getRoot();
    }

    private void scrollToPosition(final int position) {
        LOGGER.info("scrollToPosition({})", position);
        this.recyclerViewScroller.scrollToPosition(position);
    }
}
