package im.conversations.android.ui.model;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import im.conversations.android.database.model.ChatInfo;
import im.conversations.android.repository.ChatRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChatViewModel extends AndroidViewModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatViewModel.class);

    private final ChatRepository chatRepository;
    private final MutableLiveData<Long> chatId = new MutableLiveData<>();
    private final LiveData<ChatInfo> chatInfo;

    public ChatViewModel(@NonNull Application application) {
        super(application);
        this.chatRepository = new ChatRepository(application);
        this.chatInfo =
                Transformations.switchMap(
                        this.chatId,
                        chatId -> chatId == null ? null : chatRepository.getChatInfo(chatId));
    }

    public void setChatId(final long chatId) {
        this.chatId.setValue(chatId);
    }

    public LiveData<String> getTitle() {
        return Transformations.map(
                this.chatInfo, chatInfo -> chatInfo == null ? null : chatInfo.name());
    }
}
