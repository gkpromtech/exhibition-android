package ru.gkpromtech.exhibition.media;


import android.app.Activity;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ru.gkpromtech.exhibition.R;
import ru.gkpromtech.exhibition.utils.CircleImageView;

public class MediaFavoriteFragment extends Fragment {
    private Context context;
    private OnFragmentInteractionListener mListener;
    private String appDirectoryName;
    private File imageRoot;

    private List<String> items;

    private int mPhotoSize;
    private int mPhotoSpacing;
    private ImageBaseAdapter imageAdapter;

    public MediaFavoriteFragment() {
    }

    public interface OnFragmentInteractionListener {
        public void onNewPhotoClicked();
        public void onLongClicked(int index, View anchor);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_media_my, container, false);
        context = getActivity();

        appDirectoryName = getResources().getString(R.string.app_name);
        imageRoot = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), appDirectoryName);
        items = scanFolderFoImages(imageRoot.getAbsolutePath());

        CircleImageView button = (CircleImageView)view.findViewById(R.id.imageButtonPhoto);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onNewPhotoClicked();
            }
        });

        mPhotoSize = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
        mPhotoSpacing = getResources().getDimensionPixelSize(R.dimen.image_gallery_spacing);

        imageAdapter = new ImageBaseAdapter(getActivity());

        final GridView grid = (GridView)view.findViewById(R.id.gridView1);
        grid.setAdapter(imageAdapter);

        // get the view tree observer of the grid and set the height and numcols dynamically
        grid.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (imageAdapter.getNumColumns() == 0) {
                    final int numColumns = (int) Math.floor(grid.getWidth() / (mPhotoSize + mPhotoSpacing));
                    if (numColumns > 0) {
                        final int columnWidth = (grid.getWidth() / numColumns) - mPhotoSpacing;
                        imageAdapter.setNumColumns(numColumns);
                        imageAdapter.setItemHeight(columnWidth);

                    }
                }
            }
        });


        final Context context = getActivity();
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(context, FullImageActivity.class);
                i.putExtra("files", (Serializable) items);
                i.putExtra("index", position);
                startActivity(i);
            }
        });

        grid.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (mListener != null) {
                    mListener.onLongClicked(position, view);
                }
                return true;
            }
        });

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void addItem(String path) {
        items.add(path);
        imageAdapter.notifyDataSetChanged();
    }

    public boolean deleteItem(int index) {
        boolean res = removeFile(items.get(index));
        if (res == true) {
            items.remove(index);
            imageAdapter.notifyDataSetChanged();
        }

        return res;
    }

    private class ImageBaseAdapter extends BaseAdapter {
        private class Holder {
            int index;
            ImageView picture;
        }

        private LayoutInflater inflater;
        private int mItemHeight = 0;
        private int mNumColumns = 0;
        private RelativeLayout.LayoutParams mImageViewLayoutParams;

        public ImageBaseAdapter(Context context) {
            inflater = LayoutInflater.from(context);
            mImageViewLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT);
        }

        @Override
        public int getCount() {
            return items.size();
        }

        public void setNumColumns(int numColumns) {
            mNumColumns = numColumns;
        }

        public int getNumColumns() {
            return mNumColumns;
        }

        public void setItemHeight(int height) {
            if (height == mItemHeight) {
                return;
            }
            mItemHeight = height;
            mImageViewLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, mItemHeight);
            notifyDataSetChanged();
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            final Holder holder;
            String item = (String)getItem(position);

            if (view == null) {
                view = inflater.inflate(R.layout.layout_image_preview, viewGroup, false);
                final View video = view.findViewById(R.id.picture_video);
                video.setVisibility(View.INVISIBLE);

                holder = new Holder();
                holder.picture = (ImageView) view.findViewById(R.id.picture);
                view.setTag(holder);
            }
            else {
                holder = (Holder) view.getTag();
            }

            holder.index = position;
            holder.picture.setLayoutParams(mImageViewLayoutParams);

            // Check the height matches our calculated column width
            if (holder.picture.getLayoutParams().height != mItemHeight) {
                holder.picture.setLayoutParams(mImageViewLayoutParams);
            }

            try {
                Bitmap bitmap = getThumbnail(getActivity().getContentResolver(), item);
                holder.picture.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return view;
        }
    }

    private List<String> scanFolderFoImages(String folder) {
        List<String> paths = new ArrayList<String>();
        File directory = new File(folder);
        if (directory != null) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; ++i) {
                    paths.add(files[i].getAbsolutePath());
                }
            }
        }
        return paths;
    }

    private boolean removeFile(String path) {
        File file = new File(path);
        return file.delete();
    }

    public static Bitmap getThumbnail(ContentResolver cr, String path) throws Exception {
        Cursor ca = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[] { MediaStore.MediaColumns._ID }, MediaStore.MediaColumns.DATA + "=?", new String[] {path}, null);
        if (ca != null && ca.moveToFirst()) {
            int id = ca.getInt(ca.getColumnIndex(MediaStore.MediaColumns._ID));
            ca.close();
            return MediaStore.Images.Thumbnails.getThumbnail(cr, id, MediaStore.Images.Thumbnails.MINI_KIND, null );
        }
        ca.close();
        return null;
    }
}
