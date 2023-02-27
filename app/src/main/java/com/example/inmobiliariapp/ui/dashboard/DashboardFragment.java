package com.example.inmobiliariapp.ui.dashboard;


import static com.example.inmobiliariapp.MainActivity.filtrar;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.annotation.GlideOption;
import com.example.inmobiliariapp.MainActivity;
import com.example.inmobiliariapp.R;
import com.example.inmobiliariapp.SharedViewModel;
import com.example.inmobiliariapp.Vivienda;
import com.example.inmobiliariapp.databinding.FragmentDashboardBinding;
import com.example.inmobiliariapp.databinding.NotificationsRowBinding;
import com.firebase.ui.auth.data.model.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private FirebaseUser authUser;

    private String enlace = "https://firebasestorage.googleapis.com/v0/b/inmobiliariapp-fcd38.appspot.com/o/images%2F";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);
        SharedViewModel sharedViewModel = new ViewModelProvider(
                requireActivity()
        ).get(SharedViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        sharedViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            authUser = user;

            if (user != null) {
                DatabaseReference base = FirebaseDatabase.getInstance().getReference();

                DatabaseReference users = base.child("users");
                DatabaseReference uid = users.child(authUser.getUid());
                DatabaseReference incidencies = uid.child("pisos");

                //https://firebasestorage.googleapis.com/v0/b/inmobiliariapp-fcd38.appspot.com/o/images%2Fb8a2911f-93fd-4767-8f4c-258a11fd2ad4?alt=media&token=
                IncidenciaAdapter adapter;
                //REHACER QUERY
                if(binding.search.getText().toString() == "" || binding.search.getText().toString() == "a"){
                    FirebaseRecyclerOptions<Vivienda> options = new FirebaseRecyclerOptions.Builder<Vivienda>()
                            .setQuery(incidencies, Vivienda.class)
                            .setLifecycleOwner(this)
                            .build();
                    adapter = new IncidenciaAdapter(options);

                    binding.rvIncidencies.setAdapter(adapter);

                }else{
                    Query query = incidencies.orderByChild("información").equalTo(filtrar);

                    FirebaseRecyclerOptions<Vivienda> options2 = new FirebaseRecyclerOptions.Builder<Vivienda>()
                            .setQuery(query, Vivienda.class)
                            .setLifecycleOwner(this)
                            .build();

                    adapter = new IncidenciaAdapter(options2);

                    binding.rvIncidencies.setAdapter(adapter);
                }
                binding.rvIncidencies.setLayoutManager(
                        new LinearLayoutManager(requireContext())
                );


                return ;
            }
        });

        binding.filtrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filtrar =  String.valueOf(binding.search.getText());
                Navigation.findNavController(v).navigate(R.id.action_navigation_dashboard_self2);
            }
        });

        View root = binding.getRoot();
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    class IncidenciaAdapter extends FirebaseRecyclerAdapter<Vivienda, IncidenciaAdapter.IncidenciaViewholder> {
        public IncidenciaAdapter(@NonNull FirebaseRecyclerOptions<Vivienda> options) {
            super(options);
        }

        @Override
        @GlideOption
        protected void onBindViewHolder(
                @NonNull IncidenciaViewholder holder, int position, @NonNull Vivienda model
        ) {

            Log.e("URLS", model.getUrl());

            try {
                Glide.with(getContext()).load(enlace+model.getUrl()+"?alt=media&token="+model.getUrl()).into(holder.binding.imvFotocasa);
            } catch (Exception e) {
                e.printStackTrace();
            }
               // Picasso.get().load(enlace+model.getUrl()).into(holder.binding.imvFotocasa);
                holder.binding.txtDescripcio.setText(model.getInformación());
                holder.binding.txtAdreca.setText(model.getDireccio());
        }

        @NonNull
        @Override
        public IncidenciaViewholder onCreateViewHolder(
                @NonNull ViewGroup parent, int viewType
        ) {
            return new IncidenciaViewholder(NotificationsRowBinding.inflate(
                    LayoutInflater.from(parent.getContext()),
                    parent, false));
        }

        class IncidenciaViewholder extends RecyclerView.ViewHolder {
            NotificationsRowBinding binding;

            public IncidenciaViewholder(NotificationsRowBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }

}
