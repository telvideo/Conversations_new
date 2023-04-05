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

package im.conversations.android.ui.fragment.setup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import im.conversations.android.R;
import im.conversations.android.databinding.FragmentTrustCertificateBinding;
import im.conversations.android.ui.NavControllers;

public class TrustCertificateFragment extends AbstractSetupFragment {

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        FragmentTrustCertificateBinding binding =
                DataBindingUtil.inflate(
                        inflater, R.layout.fragment_trust_certificate, container, false);
        binding.setSetupViewModel(setupViewModel);
        binding.setLifecycleOwner(getViewLifecycleOwner());
        requireActivity()
                .getOnBackPressedDispatcher()
                .addCallback(
                        getViewLifecycleOwner(),
                        new OnBackPressedCallback(true) {
                            @Override
                            public void handleOnBackPressed() {
                                setupViewModel.rejectTrustDecision();
                                NavControllers.findNavController(
                                                requireActivity(), R.id.nav_host_fragment)
                                        .navigateUp();
                            }
                        });
        return binding.getRoot();
    }
}
