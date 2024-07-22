package com.example.reciperover.Fragments;

import static com.example.reciperover.Activities.Login.TAG;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.reciperover.Activities.AddRecipe;
import com.example.reciperover.Activities.EditRecipe;
import com.example.reciperover.Activities.Login;
import com.example.reciperover.Activities.Settings;
import com.example.reciperover.Activities.ShowRecipeDetail;
import com.example.reciperover.Adapters.ShowRecipeAdp;
import com.example.reciperover.Models.RecipeDetail;
import com.example.reciperover.Models.User;
import com.example.reciperover.R;
import com.example.reciperover.databinding.FragmentProfileBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class Profile extends Fragment {
    FragmentProfileBinding binding;
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    private Uri imageUri;
    private StorageReference mStorageRef;
    String userID;
    RecipeDetail rD;
    private ShowRecipeAdp adp;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        mAuth = FirebaseAuth.getInstance();
        rD = new RecipeDetail();
        userID = mAuth.getCurrentUser().getUid();
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        adp = new ShowRecipeAdp(requireContext(), new ArrayList<>(), new ShowRecipeAdp.ItemClickListener() {
            @Override
            public void onItemClick(RecipeDetail recipeDetail) {
                toShowRecipeDetail(recipeDetail);
            }

            @Override
            public void onDeleteClick(RecipeDetail recipeDetail, int position) {
                showDeleteSnackbar(recipeDetail, position);
            }

            @Override
            public void onEditClick(RecipeDetail recipeDetail) {
                Intent intent = new Intent(getContext(), EditRecipe.class);
                intent.putExtra("Selected_Recipe", recipeDetail);
                startActivity(intent);
            }

            @Override
            public void onAddToFavoritesClick(RecipeDetail recipeDetail, int position) {

            }

        }, false, false);
        loadRecyclerData();
        binding.menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopMenu(v);
            }
        });

        if (currentUser != null) {
            mAuth.getCurrentUser().getUid();
            getUserDataFromFireStore();
        }
        saveImageToFireStore();
        binding.profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageDialog();
            }
        });
        binding.addRecipeTxt.setOnClickListener(v -> {
            startActivity(new Intent(getContext(),
                    AddRecipe.class));
        });
        //recycler---------------------------------------------------------------------------\

        return view;
    }

    private void showPopMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(requireContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.profile_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.logout) {
                    mAuth.signOut();
                    Intent intent = new Intent(getContext(), Login.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
                if (itemId == R.id.settings) {
                    startActivity(new Intent(getContext(), Settings.class));
                }
                return false;
            }
        });
        popupMenu.show();
    }

    private void openImageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Choose image from");
        String[] options = {"Gallery"};
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    openGallery();
                }
            }
        });
        builder.show();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                            imageUri = result.getData().getData();
                            getProfilePicUrl();
                        }

                    });


    private void getUserDataFromFireStore() {
        DocumentReference documentReference = db.collection("Users").document(userID);
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("TAG", "Error getting document" + error.getMessage());
                    return;
                }
                if (value != null && value.exists()) {
                    User user = value.toObject(User.class);
                    assert user != null;
                    if (user.getName() != null) {
                        binding.usernAme.setText(user.getName());
                    }
                } else {
                    Log.e("TAG", "Error getting document" + error.getMessage());
                }
            }
        });
    }

    void getProfilePicUrl() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        if (imageUri != null) {
            mStorageRef = storage.getReference().child("profile_pictures/" + userID + ".jpg");
            mStorageRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    mStorageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String imageUrl = uri.toString();
                            DocumentReference documentReference = db.collection("Users").document(userID);
                            documentReference.update("profilePicUrl", imageUrl).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    if (isAdded()) {
                                        Glide.with(Profile.this).load(imageUrl).into(binding.profilePic);
                                    }
                                    Log.d("TAG", "Profile picture Saved " + userID);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d("TAG", "Error in saving Profile Picture " + userID);
                                }
                            });
                        }
                    });
                }
            });
        }
    }

    private void saveImageToFireStore() {
        db.collection("Users").document(userID).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            User user = documentSnapshot.toObject(User.class);
                            assert user != null;
                            if (user.getProfilePicUrl() != null && !user.getProfilePicUrl().isEmpty()) {
                                Glide.with(Profile.this).load(user.getProfilePicUrl()).into(binding.profilePic);
                            }
                        }
                    }
                });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadRecyclerData() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.recipesRecycler.setLayoutManager(layoutManager);
        binding.recipesRecycler.setAdapter(adp);
        CollectionReference cR = db.collection("Recipes");
        Query query = cR.whereEqualTo("currentUserID", userID);
        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<RecipeDetail> rL = new ArrayList<>();
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                RecipeDetail recipeDetail = document.toObject(RecipeDetail.class);
                rL.add(recipeDetail);
            }
            adp.setRecipesDetail(rL);
            adp.notifyDataSetChanged();
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void toShowRecipeDetail(RecipeDetail recipeDetail) {
        Intent intent = new Intent(getContext(), ShowRecipeDetail.class);
        intent.putExtra("docID", recipeDetail.getRecipeDocID());
        startActivity(intent);
    }

    private void showDeleteSnackbar(RecipeDetail recipeDetail, int position) {
        Snackbar snackbar = Snackbar.make(requireView(), "Recipe Deleted", Snackbar.LENGTH_LONG)
                .setAction("Undo", v -> {
                    undoDelete(recipeDetail, position);
                });
        snackbar.show();
        // Delete the recipe from FireStore
        deleteRecipeFromFireStore(recipeDetail);
        adp.removeItem(position);
    }

    private void undoDelete(RecipeDetail recipeDetail, int position) {
        // restore the recipe in fireStore
        db.collection("Recipes").document(recipeDetail.getRecipeDocID())
                .set(recipeDetail)
                .addOnSuccessListener(aVoid -> {
                    // Refresh your RecyclerView or notify dataset changed
                    loadRecyclerData();
                    Log.d(TAG, "Recipe restored to FireStore");
                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Error restoring recipe to FireStore", e);
                });
    }

    private void deleteRecipeFromFireStore(RecipeDetail recipeDetail) {
        db.collection("Recipes").document(recipeDetail.getRecipeDocID())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Recipe Deleted From FireStore");
                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Error Deleting Recipe From FireStore", e);
                });
    }
}