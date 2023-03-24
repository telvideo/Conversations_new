package im.conversations.android.ui.model;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModelKt;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.PagingLiveData;
import im.conversations.android.database.model.ChatInfo;
import im.conversations.android.database.model.MessageWithContentReactions;
import im.conversations.android.repository.ChatRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChatViewModel extends AndroidViewModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatViewModel.class);

    private final ChatRepository chatRepository;
    private final MutableLiveData<Long> chatId = new MutableLiveData<>();
    private final LiveData<ChatInfo> chatInfo;
    private final LiveData<PagingData<MessageWithContentReactions>> messages;

    public ChatViewModel(@NonNull Application application) {
        super(application);
        this.chatRepository = new ChatRepository(application);
        this.chatInfo =
                Transformations.switchMap(
                        this.chatId,
                        chatId -> chatId == null ? null : chatRepository.getChatInfo(chatId));
        final var messages =
                Transformations.switchMap(
                        this.chatId,
                        chatId -> {
                            final Pager<Integer, MessageWithContentReactions> pager =
                                    new Pager<>(
                                            new PagingConfig(30),
                                            () -> chatRepository.getMessages(chatId));
                            return PagingLiveData.getLiveData(pager);
                        });
        final var viewModelScope = ViewModelKt.getViewModelScope(this);
        this.messages = PagingLiveData.cachedIn(messages, viewModelScope);
    }

    public void setChatId(final long chatId) {
        this.chatId.setValue(chatId);
    }

    public LiveData<String> getTitle() {
        return Transformations.map(
                this.chatInfo, chatInfo -> chatInfo == null ? null : chatInfo.name());
    }

    public LiveData<PagingData<MessageWithContentReactions>> getMessages() {
        return this.messages;
    }
}
