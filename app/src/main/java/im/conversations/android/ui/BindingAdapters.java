package im.conversations.android.ui;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.databinding.BindingAdapter;
import androidx.lifecycle.LiveData;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.common.base.Supplier;
import im.conversations.android.R;
import im.conversations.android.database.model.ChatOverviewItem;
import im.conversations.android.database.model.Encryption;
import im.conversations.android.database.model.MessageWithContentReactions;
import im.conversations.android.database.model.Trust;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

public class BindingAdapters {

    private static final Duration SIX_HOURS = Duration.ofHours(6);
    private static final Duration THREE_MONTH = Duration.ofDays(90);

    @BindingAdapter("errorText")
    public static void setErrorText(
            final TextInputLayout textInputLayout, final LiveData<String> error) {
        textInputLayout.setError(error.getValue());
    }

    @BindingAdapter("editorAction")
    public static void setEditorAction(
            final TextInputEditText editText, final @NonNull Supplier<Boolean> callback) {
        editText.setOnEditorActionListener(
                (v, actionId, event) -> {
                    // event is null when using software keyboard
                    if (event == null || event.getAction() == KeyEvent.ACTION_UP) {
                        return Boolean.TRUE.equals(callback.get());
                    }
                    return true;
                });
    }

    private static boolean sameYear(final Instant a, final Instant b) {
        final ZoneId local = ZoneId.systemDefault();
        return LocalDateTime.ofInstant(a, local).getYear()
                == LocalDateTime.ofInstant(b, local).getYear();
    }

    private static boolean sameDay(final Instant a, final Instant b) {
        return a.truncatedTo(ChronoUnit.DAYS).equals(b.truncatedTo(ChronoUnit.DAYS));
    }

    @BindingAdapter("datetime")
    public static void setDatetime(final TextView textView, final Instant instant) {
        if (instant == null || instant.getEpochSecond() <= 0) {
            textView.setVisibility(View.GONE);
        } else {
            final Context context = textView.getContext();
            final Instant now = Instant.now();
            textView.setVisibility(View.VISIBLE);
            if (sameDay(instant, now) || now.minus(SIX_HOURS).isBefore(instant)) {
                textView.setText(
                        DateUtils.formatDateTime(
                                context, instant.toEpochMilli(), DateUtils.FORMAT_SHOW_TIME));
            } else if (sameYear(instant, now) || now.minus(THREE_MONTH).isBefore(instant)) {
                textView.setText(
                        DateUtils.formatDateTime(
                                context,
                                instant.toEpochMilli(),
                                DateUtils.FORMAT_SHOW_DATE
                                        | DateUtils.FORMAT_NO_YEAR
                                        | DateUtils.FORMAT_ABBREV_ALL));
            } else {
                textView.setText(
                        DateUtils.formatDateTime(
                                context,
                                instant.toEpochMilli(),
                                DateUtils.FORMAT_SHOW_DATE
                                        | DateUtils.FORMAT_NO_MONTH_DAY
                                        | DateUtils.FORMAT_ABBREV_ALL));
            }
        }
    }

    @BindingAdapter("time")
    public static void setTime(final TextView textView, final Instant instant) {
        if (instant == null || instant.getEpochSecond() <= 0) {
            textView.setVisibility(View.INVISIBLE);
        } else {
            final Context context = textView.getContext();
            final Instant now = Instant.now();
            textView.setVisibility(View.VISIBLE);
            textView.setText(
                    DateUtils.formatDateTime(
                            context, instant.toEpochMilli(), DateUtils.FORMAT_SHOW_TIME));
        }
    }

    @BindingAdapter("date")
    public static void setDate(final TextView textView, final Instant instant) {
        if (instant == null || instant.toEpochMilli() <= 0) {
            textView.setVisibility(View.INVISIBLE);
        } else {
            final Context context = textView.getContext();
            final Instant now = Instant.now();
            if (sameYear(instant, now) || now.minus(THREE_MONTH).isBefore(instant)) {
                textView.setVisibility(View.VISIBLE);
                textView.setText(
                        DateUtils.formatDateTime(
                                context,
                                instant.toEpochMilli(),
                                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_YEAR));
            } else {
                textView.setVisibility(View.VISIBLE);
                textView.setText(
                        DateUtils.formatDateTime(
                                context, instant.toEpochMilli(), DateUtils.FORMAT_SHOW_DATE));
            }
        }
    }

    @BindingAdapter("android:text")
    public static void setSender(final TextView textView, final ChatOverviewItem.Sender sender) {
        if (sender == null) {
            textView.setVisibility(View.GONE);
        } else {
            if (sender instanceof ChatOverviewItem.SenderYou) {
                textView.setText(
                        String.format("%s:", textView.getContext().getString(R.string.you)));
            } else if (sender instanceof ChatOverviewItem.SenderName senderName) {
                textView.setText(String.format("%s:", senderName.name));
            }
            textView.setVisibility(View.VISIBLE);
        }
    }

    @BindingAdapter("encryption")
    public static void setEncryption(
            final ImageView imageView,
            final MessageWithContentReactions.EncryptionTuple encryptionTuple) {
        if (encryptionTuple == null) {
            imageView.setVisibility(View.GONE);
            return;
        }
        final var encryption = encryptionTuple.encryption;
        final var trust = encryptionTuple.trust;
        if (encryption == null || encryption == Encryption.CLEARTEXT) {
            imageView.setVisibility(View.GONE);
        } else if (encryption == Encryption.OMEMO || encryption == Encryption.PGP) {
            if (trust == Trust.VERIFIED || trust == Trust.VERIFIED_X509) {
                imageView.setImageResource(R.drawable.ic_verified_user_24dp);
            } else {
                imageView.setImageResource(R.drawable.ic_lock_outline_24dp);
            }
            imageView.setVisibility(View.VISIBLE);
        } else if (encryption == Encryption.FAILURE) {
            imageView.setImageResource(R.drawable.ic_encryption_errorred_24dp);
            imageView.setVisibility(View.VISIBLE);
        } else {
            throw new IllegalArgumentException(String.format("Unknown encryption %s", encryption));
        }
    }

    @BindingAdapter("state")
    public static void setState(
            final ImageView imageView, final MessageWithContentReactions.State state) {
        if (state == null || state == MessageWithContentReactions.State.NONE) {
            imageView.setVisibility(View.INVISIBLE);
        } else {
            @DrawableRes
            final var drawableRes =
                    switch (state) {
                        case DELIVERED_TO_SERVER -> R.drawable.ic_check_24dp;
                        case DELIVERED, READ -> R.drawable.ic_done_all_24dp;
                        case ERROR -> R.drawable.ic_error_outline_24dp;
                        default -> throw new IllegalArgumentException(
                                String.format("State %s not implemented", state));
                    };
            imageView.setImageResource(drawableRes);
            if (state == MessageWithContentReactions.State.READ) {
                // the two color candidates are colorTertiary and colorPrimary
                // depending on the exact color scheme one might 'pop' more than the other
                imageView.setImageTintList(
                        MaterialColors.getColorStateListOrNull(
                                imageView.getContext(),
                                com.google.android.material.R.attr.colorPrimary));
            } else {
                imageView.setImageTintList(
                        MaterialColors.getColorStateListOrNull(
                                imageView.getContext(),
                                com.google.android.material.R.attr.colorOnSurface));
            }
            imageView.setVisibility(View.VISIBLE);
        }
    }
}
